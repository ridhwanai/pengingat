package id.pengingat.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import id.pengingat.app.R

/** Pembuat channel & notifikasi untuk alarm Subuh, check-in, dan service penjaga. */
object NotifHelper {

    const val CHANNEL_FAJR = "subuh"
    const val CHANNEL_CHECKIN = "checkin"
    const val CHANNEL_GUARD = "penjaga"

    const val NOTIF_ID_FAJR = 2001
    const val NOTIF_ID_CHECKIN = 2002
    const val NOTIF_ID_GUARD = 1

    fun createChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_FAJR, "Alarm Subuh", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Alarm bangun Subuh + check-in pagi" }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_CHECKIN, "Check-in Pagi", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Pengingat check-in yang belum terisi" }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_GUARD, "Penjaga Pengingat", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Notifikasi senyap service penjaga" }
        )
    }

    private fun fullScreenIntent(context: Context, target: Class<*>): PendingIntent {
        val intent = Intent(context, target).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            target.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun fajrNotification(context: Context): Notification {
        val pi = fullScreenIntent(context, FajrAlarmActivity::class.java)
        return NotificationCompat.Builder(context, CHANNEL_FAJR)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Subuh telah tiba")
            .setContentText("Waktunya sholat Subuh. Setelah itu, isi check-in: kemarin aman kan?")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pi, true)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
    }

    fun checkinNotification(context: Context): Notification {
        val pi = fullScreenIntent(context, CheckinActivity::class.java)
        return NotificationCompat.Builder(context, CHANNEL_CHECKIN)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Check-in belum terisi")
            .setContentText("Kemarin aman kan? Isi check-in pagi ini untuk menyelesaikan alarm.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pi, true)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    fun guardNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_GUARD)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Pengingat aktif menjaga")
            .setContentText("Alarm Subuh, check-in, dan popup peringatan dijaga tetap hidup.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    fun show(context: Context, id: Int, notification: Notification) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id, notification)
    }

    fun cancel(context: Context, id: Int) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(id)
    }
}
