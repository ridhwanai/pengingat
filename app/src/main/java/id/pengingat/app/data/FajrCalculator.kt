package id.pengingat.app.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Perhitungan waktu Subuh (Fajr) offline dengan algoritma posisi matahari standar
 * (Julian day → anomali rata-rata → bujur ekliptika → deklinasi & equation of time
 * → hour angle pada sudut fajar → solar noon lokal).
 *
 * Kriteria default: Kemenag RI — sudut fajar 20° di bawah ufuk,
 * lokasi Kulon Progo, Yogyakarta (lat −7.858, lng 110.157, UTC+7).
 */
object FajrCalculator {

    const val DEFAULT_LAT = -7.858
    const val DEFAULT_LNG = 110.157
    const val DEFAULT_TZ_HOURS = 7.0
    const val DEFAULT_FAJR_ANGLE = 20.0

    /** Fallback bila perhitungan gagal di luar domain: 04:35. */
    const val FALLBACK_MINUTES = 275

    /**
     * Menit sejak tengah malam lokal untuk waktu fajar pada [date].
     * Hasil di-clamp ke 0..1439. Bila hour angle di luar domain acos, pakai fallback.
     */
    fun fajrMinutes(
        date: LocalDate,
        lat: Double = DEFAULT_LAT,
        lng: Double = DEFAULT_LNG,
        tzHours: Double = DEFAULT_TZ_HOURS,
        fajrAngleDeg: Double = DEFAULT_FAJR_ANGLE
    ): Int {
        return try {
            val jd = julianDay(date.year, date.monthValue, date.dayOfMonth)
            val t = (jd - 2451545.0) / 36525.0 // abad Julian sejak J2000

            // Bujur rata-rata geometris matahari (derajat, dinormalisasi 0..360)
            val l0 = norm360(280.46646 + t * (36000.76983 + 0.0003032 * t))
            // Anomali rata-rata (derajat)
            val m = 357.52911 + t * (35999.05029 - 0.0001537 * t)
            val mRad = rad(m)
            // Eksentrisitas orbit
            val ecc = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
            // Persamaan pusat
            val c = sin(mRad) * (1.914602 - t * (0.004817 + 0.000014 * t)) +
                sin(2 * mRad) * (0.019993 - 0.000101 * t) +
                sin(3 * mRad) * 0.000289
            // Bujur sejati & bujur tampak (derajat)
            val trueLong = l0 + c
            val omega = 125.04 - 1934.136 * t
            val lambda = trueLong - 0.00569 - 0.00478 * sin(rad(omega))
            // Kemiringan ekliptika (derajat)
            val eps0 = 23.0 + (26.0 + (21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))) / 60.0) / 60.0
            val eps = eps0 + 0.00256 * cos(rad(omega))
            // Deklinasi matahari (derajat)
            val decl = deg(asin(sin(rad(eps)) * sin(rad(lambda))))
            // Equation of time (menit)
            val epsRad = rad(eps)
            val y = tan(epsRad / 2) * tan(epsRad / 2)
            val l0Rad = rad(l0)
            val eqTime = 4.0 * deg(
                y * sin(2 * l0Rad) -
                    2.0 * ecc * sin(mRad) +
                    4.0 * ecc * y * sin(mRad) * cos(2 * l0Rad) -
                    0.5 * y * y * sin(4 * l0Rad) -
                    1.25 * ecc * ecc * sin(2 * mRad)
            )
            // Tengah hari matahari dalam menit lokal
            val solarNoon = 720.0 - 4.0 * lng - eqTime + tzHours * 60.0
            // Hour angle saat matahari berada fajrAngleDeg di bawah ufuk
            val latRad = rad(lat)
            val declRad = rad(decl)
            val cosH = (sin(rad(-fajrAngleDeg)) - sin(latRad) * sin(declRad)) /
                (cos(latRad) * cos(declRad))
            if (cosH < -1.0 || cosH > 1.0) return FALLBACK_MINUTES
            val hourAngleDeg = deg(acos(cosH))
            val fajr = solarNoon - 4.0 * hourAngleDeg
            if (fajr.isNaN()) return FALLBACK_MINUTES
            fajr.toInt().coerceIn(0, 1439)
        } catch (e: Exception) {
            FALLBACK_MINUTES
        }
    }

    /** Waktu fajar sebagai LocalDateTime pada [date], digeser [offsetMin] (koreksi manual pengguna). */
    fun fajrDateTime(date: LocalDate, offsetMin: Int = 0): LocalDateTime {
        val minutes = fajrMinutes(date)
        val base = LocalDateTime.of(date, LocalTime.of(minutes / 60, minutes % 60))
        return base.plusMinutes(offsetMin.toLong())
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        return floor(365.25 * (y + 4716)) +
            floor(30.6001 * (m + 1)) +
            day + b - 1524.5
    }

    private fun rad(deg: Double): Double = deg * Math.PI / 180.0

    private fun deg(rad: Double): Double = rad * 180.0 / Math.PI

    private fun norm360(v: Double): Double {
        var r = v % 360.0
        if (r < 0) r += 360.0
        return r
    }
}
