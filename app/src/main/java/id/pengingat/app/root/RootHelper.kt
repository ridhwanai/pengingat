package id.pengingat.app.root

import com.topjohnwu.superuser.Shell

/**
 * Jembatan perintah root (libsu) untuk Mode Paksa Level 3.
 * Semua fungsi BLOCKING — wajib dipanggil dari background thread / Dispatchers.IO.
 * Bila root tidak tersedia, fungsi mengembalikan false (aplikasi turun ke Level 2).
 */
object RootHelper {

    @Volatile
    private var rootCache: Boolean? = null

    /** Cek apakah shell root benar-benar didapat (uid=0). Hasil di-cache. */
    fun isRootAvailable(): Boolean {
        rootCache?.let { return it }
        val result = try {
            val out = Shell.cmd("id").exec()
            out.out.any { it.contains("uid=0") }
        } catch (e: Exception) {
            false
        }
        rootCache = result
        return result
    }

    /** Jalankan satu perintah lewat shell (root bila tersedia). true bila exit code 0. */
    fun exec(cmd: String): Boolean {
        return try {
            Shell.cmd(cmd).exec().isSuccess
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Beri semua izin pendukung ke aplikasi ini sendiri:
     * notifikasi, overlay, usage stats, accessibility (diaktifkan langsung),
     * dan whitelist device-idle agar tidak dibunuh sistem.
     */
    fun grantAllPermissions(pkg: String): Boolean {
        if (!isRootAvailable()) return false
        val accessibilityComponent = "$pkg/.service.WatchAccessibilityService"
        val cmds = listOf(
            "pm grant $pkg android.permission.POST_NOTIFICATIONS",
            "appops set $pkg SYSTEM_ALERT_WINDOW allow",
            "appops set $pkg GET_USAGE_STATS allow",
            "appops set $pkg android:get_usage_stats allow",
            "cmd deviceidle whitelist +$pkg",
            "settings put secure accessibility_enabled 1"
        )
        var ok = true
        for (c in cmds) {
            // Sebagian perintah boleh gagal tanpa membatalkan sisanya.
            if (!exec(c)) ok = false
        }
        // Tambahkan service accessibility kita ke daftar yang diaktifkan (jangan timpa yang lain).
        try {
            val current = Shell.cmd("settings get secure enabled_accessibility_services").exec()
                .out.firstOrNull()?.trim().orEmpty()
            val parts = current.split(":").filter { it.isNotBlank() }.toMutableSet()
            parts.add(accessibilityComponent)
            exec("settings put secure enabled_accessibility_services ${parts.joinToString(":")}")
        } catch (e: Exception) {
            ok = false
        }
        return ok
    }

    /** Tutup paksa satu aplikasi (dipakai tombol "Tutup & Kembali" di popup). */
    fun forceStop(pkg: String): Boolean = exec("am force-stop $pkg")

    /** Luncurkan komponen activity secara paksa, mis. "id.pengingat.app/.service.CheckinActivity". */
    fun launchComponent(flattenedComponent: String): Boolean =
        exec("am start -n $flattenedComponent")

    /** Hidupkan GuardService dari luar (dipakai jalur pemulihan). */
    fun startGuardService(): Boolean {
        val comp = "id.pengingat.app/.service.GuardService"
        return exec("am start-foreground-service -n $comp") || exec("am startservice -n $comp")
    }
}
