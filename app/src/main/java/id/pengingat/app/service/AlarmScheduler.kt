package id.pengingat.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import id.pengingat.app.PengingatApp
import id.pengingat.app.data.FajrCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Penjadwal alarm eksak:
 * - ACTION_FAJR: Subuh berikutnya (dihitung offline, kriteria Kemenag, Kulon Progo)
 * - ACTION_CHECKIN: check-in ditunda (setelah "Sholat Dulu")
 */
object AlarmScheduler {

    const val ACTION_FAJR = "id.pengingat.app.ACTION_FAJR"
    const val ACTION_CHECKIN = "id.pengingat.app.ACTION_CHECKIN"
    const val ACTION_RESTART = "id.pengingat.app.ACTION_RESTART_GUARD"

    private const val REQ_FAJR = 1001
    private const val REQ_CHECKIN = 1002
    private const val REQ_RESTART = 1003

    private fun pendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setExact(context: Context, triggerAtMillis: Long, pi: PendingIntent) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } catch (e: SecurityException) {
            // Izin alarm eksak dicabut: jatuh ke alarm tidak eksak agar tetap berbunyi.
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    private fun fajrOffsetMin(context: Context): Int = try {
        runBlocking { PengingatApp.instance.container.settings.state.first().fajrOffsetMin }
    } catch (e: Exception) {
        0
    }

    /** Jadwalkan alarm Subuh berikutnya (hari ini bila belum lewat, else besok). */
    fun scheduleNextFajr(context: Context) {
        val offset = fajrOffsetMin(context)
        val now = LocalDateTime.now()
        var target = FajrCalculator.fajrDateTime(LocalDate.now(), offset)
        if (!target.isAfter(now)) {
            target = FajrCalculator.fajrDateTime(LocalDate.now().plusDays(1), offset)
        }
        val millis = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        setExact(context, millis, pendingIntent(context, ACTION_FAJR, REQ_FAJR))
    }

    /** Jadwalkan tahap check-in beberapa menit dari sekarang (setelah "Sholat Dulu"). */
    fun scheduleCheckinIn(context: Context, delayMinutes: Int) {
        val trigger = System.currentTimeMillis() + delayMinutes.toLong() * 60_000L
        setExact(context, trigger, pendingIntent(context, ACTION_CHECKIN, REQ_CHECKIN))
    }

    /** Jadwalkan kebangkitan GuardService ±1 menit lagi (dipanggil saat service dihancurkan). */
    fun scheduleGuardRestart(context: Context) {
        val trigger = System.currentTimeMillis() + 60_000L
        setExact(context, trigger, pendingIntent(context, ACTION_RESTART, REQ_RESTART))
    }
}
