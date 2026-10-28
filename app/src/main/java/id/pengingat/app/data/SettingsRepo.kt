package id.pengingat.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Satu perubahan pelemah perlindungan yang menunggu antrean Mode Komitmen (24 jam).
 * [kind]: "countdown" | "cooldown" | "unwatch".
 * payload: untuk countdown/cooldown = nilai baru (String angka); untuk unwatch = nama paket.
 */
data class PendingChange(
    val id: String,
    val desc: String,
    val applyAt: Long,
    val kind: String,
    val payload: String
)

data class SettingsState(
    val fajrOffsetMin: Int = 0,
    val prayerDelayMin: Int = 30,
    val popupCountdownSec: Int = 60,
    val popupCooldownMin: Int = 10,
    val watchedApps: Set<String> = emptySet(),
    val onboarded: Boolean = false,
    val attemptNo: Int = 1,
    val favorites: Set<String> = emptySet(),
    val pendingChanges: List<PendingChange> = emptyList()
)

class SettingsRepo(private val context: Context) {

    private object Keys {
        val FAJR_OFFSET = intPreferencesKey("fajr_offset_min")
        val PRAYER_DELAY = intPreferencesKey("prayer_delay_min")
        val POPUP_COUNTDOWN = intPreferencesKey("popup_countdown_sec")
        val POPUP_COOLDOWN = intPreferencesKey("popup_cooldown_min")
        val WATCHED_APPS = stringSetPreferencesKey("watched_apps")
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val ATTEMPT_NO = intPreferencesKey("attempt_no")
        val FAVORITES = stringSetPreferencesKey("favorites")
        val PENDING_CHANGES = stringPreferencesKey("pending_changes_json")
    }

    val state: Flow<SettingsState> = context.settingsDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            SettingsState(
                fajrOffsetMin = prefs[Keys.FAJR_OFFSET] ?: 0,
                prayerDelayMin = prefs[Keys.PRAYER_DELAY] ?: 30,
                popupCountdownSec = prefs[Keys.POPUP_COUNTDOWN] ?: 60,
                popupCooldownMin = prefs[Keys.POPUP_COOLDOWN] ?: 10,
                watchedApps = prefs[Keys.WATCHED_APPS] ?: emptySet(),
                onboarded = prefs[Keys.ONBOARDED] ?: false,
                attemptNo = prefs[Keys.ATTEMPT_NO] ?: 1,
                favorites = prefs[Keys.FAVORITES] ?: emptySet(),
                pendingChanges = pendingFromJson(prefs[Keys.PENDING_CHANGES])
            )
        }

    suspend fun setFajrOffset(min: Int) {
        context.settingsDataStore.edit { it[Keys.FAJR_OFFSET] = min }
    }

    suspend fun setPrayerDelay(min: Int) {
        context.settingsDataStore.edit { it[Keys.PRAYER_DELAY] = min }
    }

    suspend fun setPopupCountdown(sec: Int) {
        context.settingsDataStore.edit { it[Keys.POPUP_COUNTDOWN] = sec }
    }

    suspend fun setPopupCooldown(min: Int) {
        context.settingsDataStore.edit { it[Keys.POPUP_COOLDOWN] = min }
    }

    suspend fun setWatchedApps(apps: Set<String>) {
        context.settingsDataStore.edit { it[Keys.WATCHED_APPS] = apps }
    }

    suspend fun setOnboarded(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.ONBOARDED] = value }
    }

    suspend fun setAttemptNo(value: Int) {
        context.settingsDataStore.edit { it[Keys.ATTEMPT_NO] = value }
    }

    /** Tandai / lepas favorit berdasarkan sumber ayat (mis. "QS. Az-Zumar [39]: 53"). */
    suspend fun toggleFavorite(source: String) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES] ?: emptySet()
            prefs[Keys.FAVORITES] = if (source in current) current - source else current + source
        }
    }

    suspend fun addPendingChange(change: PendingChange) {
        context.settingsDataStore.edit { prefs ->
            val current = pendingFromJson(prefs[Keys.PENDING_CHANGES])
                .filterNot { it.id == change.id }
            prefs[Keys.PENDING_CHANGES] = pendingToJson(current + change)
        }
    }

    suspend fun cancelPendingChange(id: String) {
        context.settingsDataStore.edit { prefs ->
            val current = pendingFromJson(prefs[Keys.PENDING_CHANGES])
            prefs[Keys.PENDING_CHANGES] = pendingToJson(current.filterNot { it.id == id })
        }
    }

    /**
     * Terapkan semua PendingChange yang sudah jatuh tempo lalu hapus dari antrean:
     * countdown/cooldown mengubah nilai pengaturan, unwatch menghapus paket dari watchedApps.
     */
    suspend fun applyDueChanges(now: Long = System.currentTimeMillis()) {
        context.settingsDataStore.edit { prefs ->
            val all = pendingFromJson(prefs[Keys.PENDING_CHANGES])
            if (all.isEmpty()) return@edit
            val due = all.filter { it.applyAt <= now }
            if (due.isEmpty()) return@edit
            var watched = prefs[Keys.WATCHED_APPS] ?: emptySet()
            for (change in due) {
                when (change.kind) {
                    "countdown" -> change.payload.toIntOrNull()?.let { prefs[Keys.POPUP_COUNTDOWN] = it }
                    "cooldown" -> change.payload.toIntOrNull()?.let { prefs[Keys.POPUP_COOLDOWN] = it }
                    "unwatch" -> watched = watched - change.payload
                }
            }
            prefs[Keys.WATCHED_APPS] = watched
            prefs[Keys.PENDING_CHANGES] = pendingToJson(all.filter { it.applyAt > now })
        }
    }

    companion object {
        fun pendingToJson(list: List<PendingChange>): String {
            val arr = JSONArray()
            for (p in list) {
                arr.put(
                    JSONObject()
                        .put("id", p.id)
                        .put("desc", p.desc)
                        .put("applyAt", p.applyAt)
                        .put("kind", p.kind)
                        .put("payload", p.payload)
                )
            }
            return arr.toString()
        }

        fun pendingFromJson(json: String?): List<PendingChange> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                val arr = JSONArray(json)
                val out = ArrayList<PendingChange>(arr.length())
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    out += PendingChange(
                        id = o.optString("id"),
                        desc = o.optString("desc"),
                        applyAt = o.optLong("applyAt"),
                        kind = o.optString("kind"),
                        payload = o.optString("payload")
                    )
                }
                out
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
