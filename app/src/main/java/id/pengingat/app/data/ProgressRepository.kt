package id.pengingat.app.data

import kotlinx.coroutines.flow.Flow

/** Akses catatan progres harian & jurnal di atas Room. */
class ProgressRepository(private val db: AppDatabase) {

    private val dayDao: DayEntryDao = db.dayEntryDao()
    private val journalDao: JournalDao = db.journalDao()

    suspend fun entry(date: String): DayEntry? = dayDao.get(date)

    fun entriesFlow(): Flow<List<DayEntry>> = dayDao.flowAll()

    /**
     * Simpan check-in satu tanggal. Bila entri lama untuk tanggal itu SUDAH terisi
     * dan sekarang diubah, hasilnya ditandai edited = true (koreksi tidak diam-diam).
     */
    suspend fun recordCheckin(
        date: String,
        aman: Boolean,
        trigger: String?,
        note: String?,
        attempt: Int
    ) {
        val old = dayDao.get(date)
        val wasFilled = old?.aman != null
        dayDao.upsert(
            DayEntry(
                date = date,
                aman = aman,
                trigger = trigger,
                note = note,
                attempt = attempt,
                edited = (old?.edited == true) || wasFilled,
                filledAt = System.currentTimeMillis()
            )
        )
    }

    fun journalFlow(): Flow<List<JournalEntry>> = journalDao.flowAll()

    suspend fun addJournal(date: String, text: String) {
        journalDao.insert(
            JournalEntry(
                date = date,
                text = text,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteJournal(id: Long) {
        journalDao.delete(id)
    }
}
