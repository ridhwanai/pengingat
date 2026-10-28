package id.pengingat.app.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Watchdog internal aplikasi (TANPA modul): pekerjaan berkala WorkManager
 * (interval minimum platform 15 menit) yang memastikan GuardService hidup.
 * Dipadukan dengan BootReceiver & alarm restart di GuardService.onDestroy.
 */
class WatchdogWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        GuardService.start(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "pengingat_watchdog"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<WatchdogWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
