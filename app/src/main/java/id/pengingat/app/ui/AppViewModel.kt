package id.pengingat.app.ui

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.pengingat.app.PengingatApp
import id.pengingat.app.data.DayEntry
import id.pengingat.app.data.JournalEntry
import id.pengingat.app.data.PendingChange
import id.pengingat.app.data.Quote
import id.pengingat.app.data.SettingsState
import id.pengingat.app.root.RootHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.util.UUID

/** Status kesehatan sistem untuk layar Pengaturan & Onboarding. */
data class SystemStatus(
    val root: Boolean = false,
    val accessibility: Boolean = false,
    val overlay: Boolean = false,
    val usageAccess: Boolean = false,
    val notifications: Boolean = false,
    val batteryWhitelisted: Boolean = false,
    val checked: Boolean = false
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val app: PengingatApp = application as PengingatApp
    val container = app.container

    val settings: StateFlow<SettingsState> = container.settings.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    val entries: StateFlow<List<DayEntry>> = container.progress.entriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val journal: StateFlow<List<JournalEntry>> = container.progress.journalFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val quotes: List<Quote> by lazy { container.quotes.all() }

    private val _systemStatus = MutableStateFlow(SystemStatus())
    val systemStatus: StateFlow<SystemStatus> = _systemStatus

    private val _rootGrantResult = MutableStateFlow<Boolean?>(null)
    val rootGrantResult: StateFlow<Boolean?> = _rootGrantResult

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult

    /** Tanggal yang dinilai check-in pagi ini: kemarin. */
    fun yesterday(): LocalDate = LocalDate.now().minusDays(1)

    fun entryFor(date: LocalDate): DayEntry? =
        entries.value.firstOrNull { it.date == date.toString() }

    /**
     * Simpan check-in untuk kemarin.
     * Bila tidak aman: nomor percobaan berikutnya naik (tersimpan untuk check-in setelah ini).
     */
    fun doCheckin(aman: Boolean, trigger: String?, note: String?) {
        viewModelScope.launch {
            val attempt = settings.value.attemptNo
            container.progress.recordCheckin(
                date = yesterday().toString(),
                aman = aman,
                trigger = trigger,
                note = note,
                attempt = attempt
            )
            if (!aman) {
                container.settings.setAttemptNo(attempt + 1)
            }
        }
    }

    fun addJournal(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            container.progress.addJournal(LocalDate.now().toString(), text.trim())
        }
    }

    fun deleteJournal(id: Long) {
        viewModelScope.launch { container.progress.deleteJournal(id) }
    }

    fun toggleFavorite(source: String) {
        viewModelScope.launch { container.settings.toggleFavorite(source) }
    }

    // --- Pengaturan langsung (menguatkan / netral) ---

    fun setFajrOffset(min: Int) {
        viewModelScope.launch { container.settings.setFajrOffset(min) }
    }

    fun setPrayerDelay(min: Int) {
        viewModelScope.launch { container.settings.setPrayerDelay(min) }
    }

    fun setPopupCountdownDirect(sec: Int) {
        viewModelScope.launch { container.settings.setPopupCountdown(sec) }
    }

    fun setPopupCooldownDirect(min: Int) {
        viewModelScope.launch { container.settings.setPopupCooldown(min) }
    }

    fun watchApp(pkg: String) {
        viewModelScope.launch {
            container.settings.setWatchedApps(settings.value.watchedApps + pkg)
        }
    }

    // --- Mode Komitmen: perubahan pelemah berlaku 24 jam kemudian ---

    fun requestWeakening(desc: String, kind: String, payload: String) {
        viewModelScope.launch {
            container.settings.addPendingChange(
                PendingChange(
                    id = UUID.randomUUID().toString(),
                    desc = desc,
                    applyAt = System.currentTimeMillis() + 24L * 60 * 60 * 1000,
                    kind = kind,
                    payload = payload
                )
            )
        }
    }

    fun cancelPending(id: String) {
        viewModelScope.launch { container.settings.cancelPendingChange(id) }
    }

    fun applyDueChanges() {
        viewModelScope.launch { container.settings.applyDueChanges() }
    }

    fun finishOnboarding() {
        viewModelScope.launch { container.settings.setOnboarded(true) }
    }

    // --- Sistem ---

    fun refreshSystemStatus() {
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            val root = withContext(Dispatchers.IO) { RootHelper.isRootAvailable() }
            _systemStatus.value = SystemStatus(
                root = root,
                accessibility = isAccessibilityEnabled(ctx),
                overlay = Settings.canDrawOverlays(ctx),
                usageAccess = hasUsageAccess(ctx),
                notifications = areNotificationsEnabled(ctx),
                batteryWhitelisted = isBatteryWhitelisted(ctx),
                checked = true
            )
        }
    }

    /** Tombol onboarding/pengaturan: minta root memberi semua izin pendukung. */
    fun grantViaRoot() {
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                RootHelper.grantAllPermissions(ctx.packageName)
            }
            _rootGrantResult.value = ok
            refreshSystemStatus()
        }
    }

    /** Ekspor entri + jurnal ke file JSON di folder aplikasi (dibaca pengguna sendiri). */
    fun exportData() {
        val ctx = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val root = JSONObject()
                val arrEntries = org.json.JSONArray()
                for (e in entries.value) {
                    arrEntries.put(
                        JSONObject()
                            .put("date", e.date)
                            .put("aman", e.aman)
                            .put("trigger", e.trigger ?: JSONObject.NULL)
                            .put("note", e.note ?: JSONObject.NULL)
                            .put("attempt", e.attempt)
                            .put("edited", e.edited)
                            .put("filledAt", e.filledAt ?: JSONObject.NULL)
                    )
                }
                root.put("entries", arrEntries)
                val arrJournal = org.json.JSONArray()
                for (j in journal.value) {
                    arrJournal.put(
                        JSONObject()
                            .put("date", j.date)
                            .put("text", j.text)
                            .put("createdAt", j.createdAt)
                    )
                }
                root.put("journal", arrJournal)
                val dir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
                val file = File(dir, "pengingat-ekspor.json")
                file.writeText(root.toString(2))
                _exportResult.value = file.absolutePath
            } catch (e: Exception) {
                _exportResult.value = "Gagal ekspor: ${e.message}"
            }
        }
    }

    private fun isAccessibilityEnabled(ctx: Context): Boolean {
        val enabled = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val mine = "${ctx.packageName}/.service.WatchAccessibilityService"
        val mineFull = "${ctx.packageName}/${ctx.packageName}.service.WatchAccessibilityService"
        return enabled.split(":").any { it.equals(mine, true) || it.equals(mineFull, true) }
    }

    private fun hasUsageAccess(ctx: Context): Boolean {
        val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            ctx.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun areNotificationsEnabled(ctx: Context): Boolean {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        return nm.areNotificationsEnabled()
    }

    private fun isBatteryWhitelisted(ctx: Context): Boolean {
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        return pm.isIgnoringBatteryOptimizations(ctx.packageName)
    }

    companion object {
        /** Intent membuka halaman pengaturan accessibility sistem. */
        fun accessibilitySettingsIntent(): Intent =
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
