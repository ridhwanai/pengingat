package id.pengingat.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Catatan check-in satu hari.
 * [date] format "yyyy-MM-dd". [aman] null = belum diisi.
 * [attempt] = nomor percobaan (usaha ke-N) saat entri ini diisi.
 * [edited] = true bila entri yang sudah terisi diubah lagi (koreksi tercatat, tidak diam-diam).
 */
@Entity(tableName = "day_entries")
data class DayEntry(
    @PrimaryKey val date: String,
    val aman: Boolean? = null,
    val trigger: String? = null,
    val note: String? = null,
    val attempt: Int = 1,
    val edited: Boolean = false,
    val filledAt: Long? = null
)

/** Entri jurnal refleksi bebas, terikat tanggal. */
@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val text: String,
    val createdAt: Long
)

/**
 * Log setiap popup peringatan yang tampil.
 * [choice]: null = belum dipilih, "tutup" = Tutup & Kembali, "lanjut" = Saya Sadar, Lanjutkan.
 */
@Entity(tableName = "popup_logs")
data class PopupLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val shownAt: Long,
    val choice: String? = null,
    val resolvedAt: Long? = null
)

/**
 * Status popup peringatan yang SEDANG aktif dan belum diselesaikan.
 * Selalu satu baris (id = 1). Keberadaan baris ini = popup wajib tampil terus
 * (termasuk setelah reboot / proses dibunuh) sampai pengguna memilih di popup.
 */
@Entity(tableName = "pending_warning")
data class PendingWarning(
    @PrimaryKey val id: Int = 1,
    val packageName: String,
    val since: Long
)
