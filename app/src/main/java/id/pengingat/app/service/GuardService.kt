package id.pengingat.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import id.pengingat.app.PengingatApp
import id.pengingat.app.data.FajrCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Service penjaga (foreground, START_STICKY) — tulang punggung Mode Paksa Level 3:
 * 1. Memulihkan popup persisten yang belum selesai (setiap start & tiap menit).
 * 2. Menerapkan perubahan Mode Komitmen yang jatuh tempo.
 * 3. Eskalasi check-in: setelah Subuh, bila check-in kemarin belum terisi,
 *    layar check-in diluncurkan ulang tiap 15 menit sampai terisi.
 * Pemulihan tanpa modul: BootReceiver, alarm restart (onDestroy), dan
 * WatchdogWorker (WorkManager berkala) menghidupkan service ini lagi —
 * aplikasi berdiri sendiri, root diberikan ke aplikasi oleh pengguna.
 */
class GuardService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var lastEscalationAt: Long = 0L

    @Volatile
    private var loopStarted: Boolean = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotifHelper.createChannels(this)
        startForeground(NotifHelper.NOTIF_ID_GUARD, NotifHelper.guardNotification(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch { restorePendingWarning() }
        if (!loopStarted) {
            loopStarted = true
            scope.launch { guardLoop() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        // Jadwalkan kebangkitan diri sendiri ±1 menit lagi (best effort).
        try {
            AlarmScheduler.scheduleGuardRestart(this)
        } catch (e: Exception) {
            // WatchdogWorker adalah lapisan cadangan berikutnya.
        }
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun restorePendingWarning() {
        try {
            val pending = PengingatApp.instance.container.db.popupDao().getPending()
            if (pending != null) launchWarning()
        } catch (e: Exception) {
            // Coba lagi di putaran berikutnya.
        }
    }

    private suspend fun guardLoop() {
        while (true) {
            try {
                val container = PengingatApp.instance.container
                container.settings.applyDueChanges()

                // 1) Popup persisten selalu didahulukan.
                val pending = container.db.popupDao().getPending()
                if (pending != null) {
                    launchWarning()
                } else {
                    // 2) Eskalasi check-in pagi.
                    maybeEscalateCheckin(container)
                }
            } catch (e: Exception) {
                // Putaran berikutnya mencoba lagi.
            }
            delay(60_000)
        }
    }

    private suspend fun maybeEscalateCheckin(container: id.pengingat.app.data.AppContainer) {
        val settings = container.settings.state.first()
        if (!settings.onboarded) return

        val today = LocalDate.now()
        val fajrToday = FajrCalculator.fajrDateTime(today, settings.fajrOffsetMin)
        if (LocalDateTime.now().isBefore(fajrToday)) return // belum Subuh

        val yesterday = today.minusDays(1).toString()
        val entry = container.progress.entry(yesterday)
        if (entry?.aman != null) {
            // Sudah terisi: pastikan notifikasi check-in dibersihkan.
            NotifHelper.cancel(this, NotifHelper.NOTIF_ID_CHECKIN)
            return
        }

        if (checkinShowing) return
        val now = System.currentTimeMillis()
        if (now - lastEscalationAt < 15L * 60_000L) return
        lastEscalationAt = now

        NotifHelper.show(this, NotifHelper.NOTIF_ID_CHECKIN, NotifHelper.checkinNotification(this))
        try {
            val i = Intent(this, CheckinActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(i)
        } catch (e: Exception) {
            // Notifikasi full-screen di atas menjadi jalur tampilnya.
        }
    }

    private fun launchWarning() {
        try {
            val i = Intent(this, WarningActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(i)
        } catch (e: Exception) {
            // Dicoba lagi pada putaran guard berikutnya (tiap menit).
        }
    }

    companion object {
        /** true saat CheckinActivity sedang tampil (mencegah peluncuran ganda). */
        @Volatile
        var checkinShowing: Boolean = false

        fun start(context: Context) {
            try {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, GuardService::class.java)
                )
            } catch (e: Exception) {
                try {
                    context.startService(Intent(context, GuardService::class.java))
                } catch (ignored: Exception) {
                    // WatchdogWorker & alarm restart akan mencoba lagi berikutnya.
                }
            }
        }
    }
}
