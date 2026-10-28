package id.pengingat.app

import android.app.Application
import id.pengingat.app.data.AppContainer
import id.pengingat.app.service.WatchdogWorker

/**
 * Application class: pemilik AppContainer (dependency manual, tanpa framework DI)
 * agar semua komponen (Activity, Service, Receiver, Widget) memakai
 * repository & database yang sama.
 */
class PengingatApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        instance = this
        // Watchdog internal: pastikan GuardService dipulihkan berkala tanpa modul apa pun.
        try {
            WatchdogWorker.enqueue(this)
        } catch (e: Exception) {
            // WorkManager belum siap di proses ini; akan dicoba lagi pada start berikutnya.
        }
    }

    companion object {
        @Volatile
        lateinit var instance: PengingatApp
            private set
    }
}
