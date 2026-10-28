package id.pengingat.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import id.pengingat.app.PengingatApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Setelah reboot / aplikasi diperbarui:
 * jadwalkan ulang Subuh, hidupkan penjaga, dan pulihkan popup persisten
 * yang belum diselesaikan sebelum HP mati (syarat PRD §4.3).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        GuardService.start(context)
        AlarmScheduler.scheduleNextFajr(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pending = PengingatApp.instance.container.db.popupDao().getPending()
                if (pending != null) {
                    val i = Intent(context, WarningActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    try {
                        context.startActivity(i)
                    } catch (e: Exception) {
                        // Layar masih terkunci: GuardService akan mengulang percobaan tiap menit.
                    }
                }
            } catch (e: Exception) {
                // Abaikan: GuardService tetap bertugas memulihkan.
            } finally {
                pendingResult.finish()
            }
        }
    }
}
