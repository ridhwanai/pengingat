package id.pengingat.app.data

import android.content.Context
import org.json.JSONObject
import java.time.LocalDate
import kotlin.random.Random

/**
 * Satu entri motivasi/refleksi Qur'ani.
 * [source] selalu berisi rujukan, mis. "QS. Az-Zumar [39]: 53".
 * [tags] hanya dari: subuh, checkin, godaan, bangkit, widget, acak, syukur.
 */
data class Quote(
    val id: Int,
    val arab: String,
    val translation: String,
    val source: String,
    val reflection: String,
    val tags: List<String>
)

/**
 * Memuat assets/quotes.json sekali (lazy, thread-safe) lalu menyajikan
 * kutipan per tag, kutipan harian yang deterministik, dan kutipan acak ber-seed.
 */
class QuoteRepository(private val context: Context) {

    @Volatile
    private var cache: List<Quote>? = null

    fun all(): List<Quote> {
        cache?.let { return it }
        return synchronized(this) {
            cache ?: load().also { cache = it }
        }
    }

    fun byTag(tag: String): List<Quote> = all().filter { tag in it.tags }

    /**
     * Kutipan untuk satu tanggal — deterministik (tanggal yang sama selalu
     * menghasilkan kutipan yang sama), dari kolam [tag] bila diberikan.
     */
    fun dailyQuote(date: LocalDate, tag: String? = null): Quote {
        val pool = poolFor(tag)
        val idx = Math.floorMod(date.toEpochDay(), pool.size.toLong()).toInt()
        return pool[idx]
    }

    /** Kutipan acak yang dapat direproduksi lewat [seed] (mis. dari waktu tampil widget). */
    fun random(tag: String? = null, seed: Long): Quote {
        val pool = poolFor(tag)
        return pool[Random(seed).nextInt(pool.size)]
    }

    private fun poolFor(tag: String?): List<Quote> {
        val all = all()
        check(all.isNotEmpty()) { "quotes.json kosong atau gagal dimuat" }
        if (tag == null) return all
        val tagged = all.filter { tag in it.tags }
        return if (tagged.isNotEmpty()) tagged else all
    }

    private fun load(): List<Quote> {
        val text = context.assets.open("quotes.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(text)
        val arr = root.getJSONArray("quotes")
        val out = ArrayList<Quote>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val tagsArr = o.getJSONArray("tags")
            val tags = ArrayList<String>(tagsArr.length())
            for (j in 0 until tagsArr.length()) tags += tagsArr.getString(j)
            out += Quote(
                id = o.getInt("id"),
                arab = o.getString("arab"),
                translation = o.getString("translation"),
                source = o.getString("source"),
                reflection = o.getString("reflection"),
                tags = tags
            )
        }
        return out
    }
}
