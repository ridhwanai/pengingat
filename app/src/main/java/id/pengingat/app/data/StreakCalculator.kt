package id.pengingat.app.data

import java.time.LocalDate
import java.time.YearMonth

/**
 * Perhitungan streak & statistik murni dari daftar DayEntry.
 * Tanggal DayEntry berformat "yyyy-MM-dd" (ISO), sama dengan LocalDate.toString().
 * Entri dengan aman == null dianggap belum terisi.
 */
object StreakCalculator {

    private fun byDate(entries: List<DayEntry>): Map<String, DayEntry> =
        entries.associateBy { it.date }

    /**
     * Streak berjalan: hitung mundur dari KEMARIN (today - 1) —
     * hari ini sendiri belum dinilai sampai check-in besok pagi.
     * Berhenti pada hari pertama yang aman == false, belum terisi, atau hilang.
     */
    fun currentStreak(entries: List<DayEntry>, today: LocalDate): Int {
        val map = byDate(entries)
        var streak = 0
        var day = today.minusDays(1)
        while (true) {
            val e = map[day.toString()] ?: break
            if (e.aman == true) {
                streak++
                day = day.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    /** Rentetan hari aman berurutan terpanjang dalam seluruh riwayat (tanggal harus berurutan). */
    fun bestStreak(entries: List<DayEntry>): Int {
        val filled = entries
            .filter { it.aman != null }
            .sortedBy { it.date }
        var best = 0
        var run = 0
        var prevDate: LocalDate? = null
        for (e in filled) {
            val date = runCatching { LocalDate.parse(e.date) }.getOrNull()
            if (e.aman == true) {
                run = if (date != null && prevDate != null && date == prevDate.plusDays(1)) run + 1 else 1
                if (run > best) best = run
            } else {
                run = 0
            }
            prevDate = date
        }
        return best
    }

    fun totalAman(entries: List<DayEntry>): Int = entries.count { it.aman == true }

    fun filledCount(entries: List<DayEntry>): Int = entries.count { it.aman != null }

    /** Persentase hari aman dari hari TERISI dalam satu bulan (0 bila belum ada yang terisi). */
    fun percentAmanInMonth(entries: List<DayEntry>, yearMonth: YearMonth): Int {
        val inMonth = entries.filter { e ->
            val d = runCatching { LocalDate.parse(e.date) }.getOrNull()
            d != null && YearMonth.from(d) == yearMonth && e.aman != null
        }
        if (inMonth.isEmpty()) return 0
        val aman = inMonth.count { it.aman == true }
        return aman * 100 / inMonth.size
    }
}
