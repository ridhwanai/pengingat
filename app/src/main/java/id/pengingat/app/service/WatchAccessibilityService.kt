package id.pengingat.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import id.pengingat.app.PengingatApp
import id.pengingat.app.data.PendingWarning
import id.pengingat.app.data.PopupLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Deteksi real-time aplikasi pantauan yang dibuka (PRD §4.3).
 * Hanya mencocokkan nama paket dengan daftar pantauan pengguna;
 * isi layar TIDAK dibaca (canRetrieveWindowContent=false di konfigurasi).
 */
class WatchAccessibilityService : AccessibilityService() {

    @Volatile
    private var lastCheckAt: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        // Throttle: batasi kerja berat maksimal 1x per detik.
        val now = System.currentTimeMillis()
        if (now - lastCheckAt < 1_000) return
        lastCheckAt = now

        try {
            val container = PengingatApp.instance.container
            val handled = runBlocking {
                val settings = container.settings.state.first()
                if (pkg !in settings.watchedApps) return@runBlocking false

                val popupDao = container.db.popupDao()
                if (popupDao.getPending() != null) return@runBlocking true // popup sudah aktif

                // Masa jeda anti-spam setelah user memilih "lanjut".
                val lastLanjut = popupDao.lastResolvedLanjut(pkg)
                val resolvedAt = lastLanjut?.resolvedAt
                if (resolvedAt != null &&
                    now - resolvedAt < settings.popupCooldownMin.toLong() * 60_000L
                ) {
                    return@runBlocking true
                }

                popupDao.insertLog(
                    PopupLog(packageName = pkg, shownAt = now, choice = null, resolvedAt = null)
                )
                popupDao.insertPending(
                    PendingWarning(id = 1, packageName = pkg, since = now)
                )
                true
            }
            if (handled) {
                val i = Intent(this, WarningActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(i)
                GuardService.start(this)
            }
        } catch (e: Exception) {
            // Service tidak boleh crash: deteksi berikutnya akan mencoba lagi.
        }
    }

    override fun onInterrupt() {
        // Tidak ada umpan balik yang perlu dihentikan.
    }
}
