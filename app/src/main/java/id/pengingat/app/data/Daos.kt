package id.pengingat.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DayEntryDao {

    @Query("SELECT * FROM day_entries WHERE date = :date LIMIT 1")
    suspend fun get(date: String): DayEntry?

    @Query("SELECT * FROM day_entries ORDER BY date ASC")
    fun flowAll(): Flow<List<DayEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DayEntry)
}

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC, id DESC")
    fun flowAll(): Flow<List<JournalEntry>>

    @Insert
    suspend fun insert(entry: JournalEntry): Long

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface PopupDao {

    @Insert
    suspend fun insertLog(log: PopupLog): Long

    @Query("UPDATE popup_logs SET choice = :choice, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun updateResolution(id: Long, choice: String, resolvedAt: Long)

    /** Log terbuka (belum ada pilihan) terakhir untuk satu paket — untuk ditandai selesai. */
    @Query("SELECT * FROM popup_logs WHERE packageName = :pkg AND resolvedAt IS NULL ORDER BY shownAt DESC LIMIT 1")
    suspend fun latestOpenLog(pkg: String): PopupLog?

    /** Log terakhir di mana pengguna memilih "lanjut" untuk paket ini (dasar masa jeda anti-spam). */
    @Query("SELECT * FROM popup_logs WHERE packageName = :pkg AND choice = 'lanjut' AND resolvedAt IS NOT NULL ORDER BY resolvedAt DESC LIMIT 1")
    suspend fun lastResolvedLanjut(pkg: String): PopupLog?

    @Query("SELECT * FROM popup_logs ORDER BY shownAt DESC")
    fun flowAllLogs(): Flow<List<PopupLog>>

    // --- Pending warning (popup aktif persisten, satu baris) ---

    @Query("SELECT * FROM pending_warning WHERE id = 1 LIMIT 1")
    suspend fun getPending(): PendingWarning?

    @Query("SELECT * FROM pending_warning WHERE id = 1 LIMIT 1")
    fun flowPending(): Flow<PendingWarning?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPending(warning: PendingWarning)

    @Query("DELETE FROM pending_warning WHERE id = 1")
    suspend fun deletePending()
}
