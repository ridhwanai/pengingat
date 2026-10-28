# STATUS SOURCE — Pengingat (per 28 Oktober 2026, revisi v1.2)

## Revisi v1.2 (28 Okt — perbaikan dari log build Actions pertama)
- Build pertama GAGAL di `:app:checkReleaseAarMetadata`: 44 isu yang semuanya satu akar — pustaka yang dipakai (Compose BOM 2024.02, Glance 1.0.0, WorkManager 2.9.0, Room 2.6.1, lifecycle 2.7.0) mewajibkan **compileSdk ≥ 34**, sedangkan proyek diset 33.
- Perbaikan: `compileSdk = 34` + `targetSdk = 34` di `app/build.gradle.kts`, dan GuardService di Manifest diberi `foregroundServiceType="specialUse"` + property subtype (syarat targetSdk 34 di Android 14). minSdk tetap 29; aplikasi tetap berjalan di HP user (Android 13).
- Build BELUM mencapai tahap kompilasi Kotlin pada run pertama itu — run berikutnya yang membuktikan tahap selanjutnya.

## Revisi v1.1 (28 Okt, permintaan user)
- **Modul KernelSU DICABUT TOTAL** — ini murni aplikasi; root diberikan ke aplikasi oleh user. Folder ksu-module dihapus, workflow tidak lagi membuild modul. Watchdog kini internal aplikasi (WorkManager 15 mnt + BootReceiver + alarm restart).
- **Karya seni anime ditambahkan** (4 gambar, total ±400 KB di APK): `bg_home.jpg` (fajar + bulan sabit + masjid + sawah terasering), `bg_fajr.jpg` (matahari terbit Subuh), `bg_warning.jpg` (seberkas cahaya menembus awan gelap ke masjid), `bg_widget.jpg` (lanskap untuk widget) — semuanya pemandangan TANPA karakter, sesuai §5.
- **Efek UI (ringan)**: SceneryBackground + tirai gradasi + cahaya emas bernapas pelan, kartu kaca semi-transparan, **StreakRing** animasi (busur emas + angka menghitung naik), transisi fade antar-tab, widget memakai gambar latar + lapisan tirai.

## Status: LENGKAP sesuai PRD v2.0 (siap push & build di GitHub Actions)

Semua bagian sudah ditulis:
- **Fondasi**: Gradle (AGP 8.2.2 / Kotlin 1.9.22 / Compose BOM 2024.02.00 / Room 2.6.1), Manifest lengkap (TANPA izin INTERNET), ikon, tema.
- **Data layer** (`data/`): Room (DayEntry, JournalEntry, PopupLog, PendingWarning), SettingsRepo + Mode Komitmen 24 jam, ProgressRepository, StreakCalculator, QuoteRepository + 60 entri ayat, FajrCalculator (Subuh offline Kulon Progo, kriteria Kemenag −20°, **terverifikasi numerik**: 2026-10-28 → 03:52 WIB).
- **UI** (`ui/`): tema navy-emas, Beranda, Check-in + refleksi tergelincir, Kalender/Statistik, Jurnal, Perpustakaan (filter tag + favorit), Pengaturan (offset Subuh, jeda popup dengan antrean Mode Komitmen, pemilih aplikasi pantauan, kesehatan sistem, ekspor JSON), Onboarding 6 langkah izin.
- **Service** (`service/`): AlarmScheduler (alarm eksak Subuh & check-in), AlarmReceiver, FajrAlarmActivity (tahap bangun + "Sholat Dulu"), CheckinActivity (tahap check-in paksa), GuardService (penjaga: pulihkan popup tiap menit + eskalasi check-in 15 menit), WatchAccessibilityService (deteksi aplikasi pantauan + cooldown), WarningActivity (popup persisten, Back mati, hitung mundur "Lanjutkan"), BootReceiver (jadwal ulang + pulihkan popup pasca-boot), NotifHelper.
- **Root** (`root/RootHelper.kt`): libsu — cek root, auto-grant izin + aktifkan accessibility, force-stop, luncurkan komponen.
- **Widget** (`widget/`): Glance 4×2 (streak + kutipan harian + Subuh besok + check-in cepat) & 2×2 (angka streak).
- **Watchdog internal aplikasi (BUKAN modul — keputusan user 28 Okt, PRD v2.1)**: WorkManager `WatchdogWorker` berkala 15 menit (dijadwalkan dari PengingatApp), BootReceiver, dan alarm restart 1 menit di `GuardService.onDestroy` (ACTION_RESTART). Root diberikan ke APLIKASI oleh user via KernelSU Manager — tidak ada file modul apa pun.
- **CI** (`.github/workflows/build-apk.yml`): workflow_dispatch, JDK 17, Gradle 8.4, keystore tetap dari Secrets (bila ada), artifact APK + zip modul. YAML tervalidasi.

## Pemeriksaan yang SUDAH dilakukan di sini
- Keseimbangan kurung semua file Kotlin (setelah string/komentar disisihkan): seimbang.
- Semua kelas yang dideklarasikan di Manifest ada file/kelasnya.
- API data layer dibaca ulang dan semua pemanggilan dari UI/service disesuaikan persis (termasuk nilai choice popup huruf kecil "tutup"/"lanjut" sesuai DAO).

## Yang BELUM terbukti (jujur)
- **Kompilasi Kotlin/Gradle belum dijalankan** — lingkungan ini tidak punya JDK/Android SDK. Build GitHub Actions pertamalah pembuktiannya; bila gagal, kirim log Actions-nya untuk diperbaiki (pola yang sama seperti proyek AZenith).
- Terjemahan ayat di quotes.json mengikuti Kemenag sebaik yang diketahui — wajib verifikasi final ke mushaf Kemenag (PRD §5). Jumlah entri 60 dari target rilis ≥100 (ekspansi konten menyusul).
