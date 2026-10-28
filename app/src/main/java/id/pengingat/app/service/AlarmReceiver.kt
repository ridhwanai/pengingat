package id.pengingat.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Penerima alarm eksak: membuka layar yang sesuai + notifikasi layar penuh cadangan. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotifHelper.createChannels(context)
        when (intent.action) {
            AlarmScheduler.ACTION_FAJR -> {
                GuardService.start(context)
                NotifHelper.show(context, NotifHelper.NOTIF_ID_FAJR, NotifHelper.fajrNotification(context))
                startActivity(context, FajrAlarmActivity::class.java)
            }
            AlarmScheduler.ACTION_CHECKIN -> {
                GuardService.start(context)
                NotifHelper.show(context, NotifHelper.NOTIF_ID_CHECKIN, NotifHelper.checkinNotification(context))
                startActivity(context, CheckinActivity::class.java)
            }
            AlarmScheduler.ACTION_RESTART -> {
                GuardService.start(context)
            }
        }
    }

    private fun startActivity(context: Context, target: Class<*>) {
        try {
            val i = Intent(context, target).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(i)
        } catch (e: Exception) {
            // Pembatasan background activity start: notifikasi full-screen di atas
            // adalah jalur cadangan yang akan menampilkan layarnya.
        }
    }
}
