# PRD — Pengingat
### Aplikasi Pencatat Progres & Pengingat Berhenti dari Maksiat

| | |
|---|---|
| **Nama aplikasi** | **Pengingat** |
| **Versi dokumen** | **2.1** — 28 Oktober 2026 (revisi v2.0: modul KernelSU dicabut — murni aplikasi yang diberi izin root; karya seni anime resmi ditambahkan; riwayat perubahan di §10) |
| **Platform** | Android — perangkat utama: Redmi Note 9, LineageOS 20 / Android 13, root KernelSU Next |
| **Sifat** | Aplikasi pribadi, sideload (bukan Play Store), 100% offline & lokal |
| **Cara build** | Muse menulis source code → pengguna push ke GitHub → **GitHub Actions** build APK (pola yang sama seperti build Iconify) |
| **Lokasi acuan waktu Subuh** | Kulon Progo, Daerah Istimewa Yogyakarta (koordinat acuan Kab. Kulon Progo/Wates: ± 7,86° LS, 110,16° BT, WIB UTC+7) |

---

## 1. Ringkasan Produk

**Pengingat** adalah pendamping pribadi untuk membantu pemiliknya **konsisten berhenti dari perbuatan maksiat** yang ingin ia tinggalkan, sekaligus membangunkannya untuk **sholat Subuh**. Lingkaran hariannya:

1. **Saat Subuh tiba** (mengikuti waktu Subuh Kulon Progo, berubah tiap hari), aplikasi berbunyi sebagai alarm bangun Subuh **dan** memaksa check-in: *"Kemarin aman kan?"* — paksaan **Level 3 (root)** sampai check-in terisi.
2. Jawaban **Aman / Tidak aman** menjadi catatan progres; streak, kalender, dan statistik memperlihatkan perjalanan.
3. Sepanjang hari, **widget** menampilkan streak + teks refleksi Qur'ani yang berganti-ganti.
4. Jika pengguna membuka aplikasi yang ia tandai sendiri sebagai pintu maksiat, **popup peringatan layar penuh muncul dan TIDAK BISA HILANG** — tidak dengan tombol Back, Home, maupun mematikan HP. Popup hanya hilang bila pengguna menekan salah satu pilihan yang tersedia di popup itu.

Prinsip nada: **tegas dan lembut sekaligus, sesuai konteksnya** (aturan teks di §5). Tegas saat menggoda ditahan dan saat alarm membangunkan; lembut saat pengguna tergelincir dan butuh bangkit. Sistemnya keras, kata-katanya penuh rahmat — tidak ada teks yang mempermalukan atau membuat putus asa.

## 2. Tujuan & Ukuran Keberhasilan

| Tujuan | Ukuran |
|---|---|
| Bangun & sholat Subuh tepat waktu | Alarm Subuh berbunyi sesuai jadwal hitung; pengguna menekan aksi bangun |
| Check-in pagi tidak pernah terlewat | Check-in terisi ≥ 95% hari per bulan |
| Progres terlihat & memotivasi | Streak berjalan, streak terbaik, % hari aman per bulan |
| Momen godaan terputus | Popup persisten tampil; rasio "urung membuka" vs "tetap lanjut" tercatat |
| Data aman & privat | 0 data keluar perangkat; aplikasi tanpa izin INTERNET |

## 3. Pengguna

Satu orang: pemilik perangkat (root, LOS 20). Tidak ada akun, cloud, atau pengguna lain — sehingga aplikasi sah memakai kemampuan root & izin tingkat lanjut atas keputusan pemilik perangkat sendiri.

## 4. Fitur Utama

### 4.1 Alarm Subuh + Check-in Paksa (Fitur Inti)

**Waktu pemicu — mengikuti Subuh, bukan jam tetap:**
- Waktu Subuh dihitung **di dalam aplikasi, sepenuhnya offline**, dari perhitungan astronomis matahari dengan **kriteria Kemenag RI** (sudut fajar 20° di bawah ufuk) untuk koordinat Kulon Progo (§ Lokasi). Waktu berubah beberapa menit dari hari ke hari; aplikasi menampilkan waktu Subuh **besok** di Beranda agar pengguna bisa bersiap.
- Akurasi target: ±2 menit dari jadwal Kemenag setempat (jadwal resmi selalu menang bila pengguna mengoreksi manual — tersedia penyesuaian offset menit di Pengaturan).
- Setelah check-in hari ini selesai, alarm otomatis dijadwalkan ke Subuh **hari berikutnya**; BootReceiver menjadwalkan ulang setelah reboot; service penjaga (root) memastikan alarm tidak hilang karena aplikasi dibunuh sistem.

**Alur pagi (dua tahap):**
1. **Tahap Bangun (saat Subuh):** layar penuh menutupi layar kunci + nada alarm berbunyi: *"Subuh telah tiba — waktunya sholat."* + ayat (mis. QS. Al-Isra: 78). Dua pilihan:
   - **"Sholat Dulu"** → alarm berhenti; check-in ditunda **30 menit** (dapat diatur 15–60 menit), lalu Tahap 2 dimulai.
   - **"Isi Check-in Sekarang"** → langsung ke Tahap 2.
2. **Tahap Check-in (memaksa, Level 3):** layar check-in *"Kemarin aman kan?"* muncul dengan eskalasi:
   - Notifikasi *full-screen intent* + notifikasi *ongoing* yang tidak bisa di-swipe;
   - Pengingat berulang tiap 15 menit bila belum dijawab;
   - **Root:** service penjaga meluncurkan layar check-in secara paksa bila melewati batas tunda, dan menjaga proses tetap hidup dari pembunuhan LMK/LOS.
   - Paksaan berhenti total hari itu **hanya** setelah check-in terisi.

**Isi check-in:**
- Jawaban **Aman** → apresiasi singkat + teks refleksi acak bertema syukur/istiqamah + streak +1.
- Jawaban **Tidak aman** → layar refleksi lembut: pemicu (pilihan cepat + catatan bebas opsional), ayat harapan & taubat (mis. QS. Az-Zumar: 53), tombol *"Saya sudah bertaubat, mulai lagi hari ini."* Streak berjalan reset ke 0; **streak terbaik & total hari aman tidak hilang**; penghitung percobaan bertambah.

### 4.2 Pencatatan Progres

- **Streak:** hari bersih beruntun, streak terbaik, total hari aman, milestone hari ke-1, 3, 7, 14, 30, 60, 90, 180, 365 dengan layar perayaan elegan.
- **Kalender:** penanda per hari (aman / tidak aman / kosong); tap untuk melihat entri; koreksi entri lama tercatat sebagai edit (tidak bisa diam-diam).
- **Statistik:** grafik 7/30 hari, % aman bulan ini vs lalu, pemicu tersering.
- **Jurnal refleksi:** catatan bebas kapan pun dari menu (manual saja — **tidak ada pengingat malam**; fitur itu dicabut di v2.0, lihat §10).

### 4.3 Popup Peringatan Persisten (Fitur Inti)

- Pengguna memilih sendiri **daftar aplikasi pantauan** di dalam aplikasi (tidak ada daftar bawaan; daftar ini tidak pernah keluar dari HP dan tidak perlu diberitahukan kepada siapa pun).
- Saat aplikasi pantauan terdeteksi di depan (Accessibility Service utama, Usage Stats cadangan), **popup layar penuh** tampil:
  - *"Berhenti sejenak. Allah selalu melihatmu."*
  - Ayat tegas konteks godaan (mis. QS. Gafir: 19 — Allah mengetahui pandangan mata yang khianat; QS. An-Nur: 30; QS. Qaf: 16) + refleksi 1 kalimat.
  - Streak ditampilkan: *"Kamu sudah bersih sekian hari…"*
  - **Hanya dua pilihan, dan hanya keduanya yang bisa menghilangkan popup:**
    - **Tutup & Kembali** (selalu aktif, tombol utama) → aplikasi pantauan di-force-stop via root, pengguna diarahkan ke layar refleksi Pengingat.
    - **Saya Sadar, Lanjutkan** → aktif setelah hitung mundur wajib **60 detik** (rentang atur 30–300 detik) selama ayat ditampilkan; pilihan ini tercatat di log.
- **Persistensi (syarat mutlak v2.0):**
  - Status "popup sedang aktif untuk aplikasi X" **disimpan persisten** di basis data — bukan sekadar jendela sesaat.
  - Tombol **Back tidak berfungsi** di popup; tombol **Home/Recents tidak menghilangkannya**: jendela overlay layar penuh digambar ulang & service penjaga (foreground + watchdog root) mengembalikannya ke depan selama status aktif belum diselesaikan dengan pilihan.
  - **Bila HP dimatikan/direboot saat popup aktif:** BootReceiver aplikasi menjalankan GuardService saat boot (diperkuat WatchdogWorker berkala); segera setelah boot selesai & kunci layar pertama dibuka, aplikasi memeriksa status persisten dan **popup tampil kembali** sebelum pengguna sempat memakai aplikasi pantauan lagi.
  - Bila proses aplikasi mati/dibunuh sistem, lapisan pemulihan internal aplikasi (alarm restart saat service dihancurkan + WatchdogWorker WorkManager tiap 15 menit) menghidupkan GuardService kembali beserta popup-nya.
  - *Batas jujur (dicatat agar tidak ada janji palsu):* persistensi ini berlaku selama aplikasi masih terpasang dan izin root-nya tidak dicabut. Uninstall lewat jalur resmi, safe mode, atau factory reset berada di luar jangkauan aplikasi mana pun — itu kewenangan pemilik perangkat. Mode Komitmen (§4.5) membuat jalur-jalur pelemahan itu lambat & disadari, bukan instan.
- **Anti-spam:** setelah "Lanjutkan" dipilih, popup tidak muncul lagi untuk aplikasi yang sama selama masa jeda (default 10 menit).

### 4.4 Widget Layar Utama

- **4×2 (utama):** angka streak besar, tanggal, kutipan refleksi acak (Arab + terjemahan + sumber), tombol segarkan kutipan, tombol check-in cepat, waktu Subuh besok.
- **2×2 (ringkas):** angka streak + status hari ini.
- Kutipan berganti otomatis tiap hari & saat tombol segarkan ditekan. Teknologi: Jetpack Glance.

### 4.5 Mode Komitmen (Anti-Bypass)

- Perubahan yang **melemahkan** perlindungan — menurunkan paksaan, memperpendek jeda popup, menghapus aplikasi pantauan, mematikan Mode Paksa — masuk antrean dan **baru berlaku 24 jam kemudian**; membatalkannya selalu bisa dengan satu tap.
- Perubahan yang **menguatkan** selalu berlaku seketika. Menonaktifkan Mode Komitmen itu sendiri juga tunduk pada aturan 24 jam.

### 4.6 Privasi

- 100% lokal (Room + DataStore), **tanpa izin INTERNET, tanpa akun, tanpa analitik, tanpa iklan**.
- **Tanpa kunci aplikasi** (keputusan v2.0: tidak perlu PIN/biometrik).
- Ekspor/impor manual file JSON dari Pengaturan untuk backup.
- Nama "Pengingat" & ikon elegan netral — tidak mencolok di depan orang lain.

## 5. Aturan Teks & Konten (Refleksi Qur'ani)

Semua teks di dalam aplikasi — alarm, check-in, popup, widget, perpustakaan — tunduk pada aturan ini:

1. **Sumbernya Al-Qur'an** dengan terjemahan **Kemenag verbatim**; setiap kutipan selalu menampilkan sumber (nama surah + nomor surah:ayat). **Tidak ada ayat karangan, tidak ada teks yang diklaim sebagai ayat padahal parafrasa.** Hadis tidak dipakai di MVP; bila kelak ditambahkan, wajib berstatus sahih/hasan dengan sumber kitab & nomor yang terverifikasi.
2. **Teks refleksi adalah tulisan motivasi**, bukan tafsir — tidak pernah mengklaim makna ayat di luar yang umum diketahui, dan tidak memuat klaim keagamaan yang tidak bersumber.
3. **Dua nada, sesuai konteks:**
   - **Tegas** — untuk popup godaan & alarm: singkat, jelas, mengingatkan bahwa Allah Maha Melihat (mis. QS. Gafir: 19, QS. Qaf: 16, QS. An-Nur: 30, QS. Al-Isra: 32).
   - **Lembut** — untuk pagi hari, setelah tergelincir, & widget: harapan, rahmat, ajakan bangkit (mis. QS. Az-Zumar: 53, QS. Al-Furqan: 70, QS. Ali 'Imran: 135, QS. Ar-Ra'd: 28, QS. Asy-Syarh: 5–6).
4. **Jumlah:** basis data lokal **≥ 100 entri** di rilis pertama; tiap entri: teks Arab, terjemahan Kemenag, sumber, refleksi 1–3 kalimat, tag konteks (`subuh`, `check-in`, `godaan`, `bangkit`, `widget`, `acak`).

## 6. Desain UI/UX

- **Gaya:** elegan & mewah bernuansa anime — tema gelap navy-hitam, aksen emas gradien halus + sentuhan emerald, kartu glassmorphism bertepi emas tipis, glow lembut.
- **Sentuhan anime = pemandangan** (langit fajar/senja ala film anime, siluet masjid, bintang) — **bukan gambar tokoh**, disengaja agar visual tidak menjadi pemicu; tidak ada gambar aurat dalam bentuk apa pun. Latar Beranda bahkan bisa mengikuti waktu: langit fajar saat Subuh.
- **Tipografi:** Arab dengan font indah terbaca (Amiri/Scheherazade), judul serif elegan, isi sans bersih. Bahasa UI: Bahasa Indonesia.
- **Gerakan:** transisi halus, angka streak menghitung naik, partikel cahaya emas di milestone. Prioritas tetap ringan di Redmi Note 9 (animasi bisa dimatikan).
- **Layar:** Onboarding (panduan izin langkah-demi-langkah) · Beranda · Alarm Subuh (layar penuh) · Check-in · Refleksi Tergelincir · Kalender · Statistik · Jurnal · Perpustakaan Refleksi · Popup Peringatan (layar penuh persisten) · Pengaturan (Waktu & Lokasi Subuh · Level Paksaan · Daftar Pantauan · Mode Komitmen · Konten · Kesehatan Sistem).

## 7. Spesifikasi Teknis

| Aspek | Pilihan |
|---|---|
| Bahasa / UI | Kotlin, Jetpack Compose (Material 3, tema kustom) |
| Widget | Jetpack Glance |
| Data | Room (entri harian, jurnal, log popup, **status popup persisten**) + DataStore (pengaturan, antrean Mode Komitmen) |
| Waktu Subuh | Perhitungan astronomis lokal (algoritma posisi matahari; kriteria Kemenag: fajar −20°), koordinat Kulon Progo, tanpa internet; offset koreksi manual ± menit |
| Penjadwalan | AlarmManager exact alarm + BootReceiver + service penjaga |
| Deteksi aplikasi | AccessibilityService (utama) + UsageStatsManager (cadangan) |
| Popup | Overlay `TYPE_APPLICATION_OVERLAY` layar penuh + activity layar-penuh `showWhenLocked`; Back dinonaktifkan; status persisten + gambar ulang oleh service |
| Root (Level 3 — default & wajib aktif) | Perintah `su` (libsu) dari DALAM aplikasi (izin root diberikan user ke aplikasi via KernelSU Manager): auto-grant izin saat onboarding, force-stop aplikasi pantauan, luncurkan layar paksa, jaga proses. Watchdog & pemulihan popup pasca-boot adalah mekanisme internal aplikasi (GuardService START_STICKY + BootReceiver + alarm restart + WatchdogWorker). **TANPA modul KernelSU/Magisk apa pun (keputusan v2.1)** |
| Fallback | Bila root hilang/ditolak: turun otomatis ke perilaku Level 2 (notifikasi ongoing + berulang) + peringatan permanen di Beranda & Kesehatan Sistem |
| Target | minSdk 29, targetSdk 33 (Android 13 / LOS 20) |

**Izin:** `POST_NOTIFICATIONS`, `USE_EXACT_ALARM`/`SCHEDULE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, `FOREGROUND_SERVICE`, `RECEIVE_BOOT_COMPLETED`, `SYSTEM_ALERT_WINDOW`, `PACKAGE_USAGE_STATS`, Accessibility Service, akses root, pengecualian optimasi baterai. **Tidak diminta: `INTERNET`.**

## 8. Alur Build — Source Code & GitHub Actions (bukan Kaggle)

1. Muse menulis **source code lengkap** proyek Gradle Kotlin/Compose (satu aplikasi, tanpa modul).
2. Pengguna membuat repo GitHub (usulan: `ridhwanai/Pengingat`), push source, dan membuka tab **Actions**.
3. Workflow `.github/workflows/build-apk.yml` berjalan manual (**workflow_dispatch**, seperti workflow Iconify) atau tiap push:
   - Setup JDK 17 (Temurin) di runner Ubuntu;
   - Install Android SDK (platform API 33 + build-tools) via sdkmanager;
   - **Signing konsisten:** keystore tetap disimpan sebagai GitHub Secrets (base64) dan dipakai di setiap build — agar APK hasil build berikutnya bisa **menimpa instal tanpa uninstall** (menghindari masalah tanda tangan debug berganti-ganti antar-runner);
   - `./gradlew assembleRelease` (fallback `assembleDebug` bertanda tangan keystore yang sama);
   - Upload APK sebagai **artifact** Actions untuk diunduh & dipasang di Redmi Note 9.
4. Satu-satunya artifact build adalah APK; tidak ada berkas modul yang di-flash.

## 9. Ruang Lingkup

### MVP (v1.0 aplikasi) — wajib ada
- [ ] Alarm Subuh (hitung lokal Kulon Progo, kriteria Kemenag) 2 tahap: bangun/sholat → check-in paksa Level 3
- [ ] Check-in Aman/Tidak aman + refleksi tergelincir + streak/kalender/statistik dasar
- [ ] Popup peringatan **persisten** (anti Back/Home, pulih setelah reboot/mati, pulih setelah proses dibunuh) + watchdog internal aplikasi
- [ ] Widget 4×2 & 2×2 (streak, refleksi acak, Subuh besok, check-in cepat)
- [ ] Perpustakaan refleksi ≥ 100 entri terverifikasi (aturan §5)
- [ ] Mode Komitmen 24 jam
- [ ] Onboarding panduan izin + auto-grant via root
- [ ] Ekspor/impor JSON

### Di luar lingkup
- Pengingat/jurnal malam terjadwal (dicabut v2.0), kunci aplikasi PIN/biometrik (dicabut v2.0), build via Kaggle (dicabut v2.0), akun/cloud, iOS, Play Store, deteksi situs peramban (calon fase berikutnya bila diminta).

## 10. Riwayat Perubahan v1.0 → v2.0 (keputusan pengguna, 28 Okt 2026)

| # | Perubahan |
|---|---|
| 1 | Nama aplikasi: ~~AMAN~~ → **Pengingat** |
| 2 | Lingkungan build: ~~Kaggle Notebook~~ → **source code oleh Muse + GitHub Actions** |
| 3 | Waktu notifikasi: ~~jam tetap 05:00~~ → **mengikuti waktu Subuh Kulon Progo**, sekaligus alarm bangun sholat Subuh (2 tahap) |
| 4 | Level paksaan: diputuskan **Level 3 (root)** sebagai default wajib |
| 9 (v2.1) | **Modul KernelSU DICABUT** — Pengingat adalah aplikasi saja yang diberi izin root oleh user; watchdog menjadi mekanisme internal aplikasi. Karya seni latar anime (4 gambar pemandangan tanpa karakter) resmi menjadi bagian UI & widget |
| 5 | Popup: ditambah syarat **persisten total** — tidak hilang oleh Back/Home/reboot/HP mati; hanya hilang lewat pilihan di popup |
| 6 | Pengingat malam: **dicabut** (tidak dipakai; jurnal tetap ada tapi manual) |
| 7 | Kunci aplikasi: **dicabut** (tidak perlu) |
| 8 | Teks: dipertegas — semua teks **refleksi sesuai Al-Qur'an yang benar**, dua nada **tegas & lembut** sesuai konteks (§5) |

---
*Tidak ada keputusan terbuka yang menghalangi pembangunan. Default yang dipakai: jeda hitung mundur "Lanjutkan" 60 detik, tunda check-in setelah "Sholat Dulu" 30 menit, masa jeda anti-spam popup 10 menit, offset Subuh 0 menit — semuanya dapat diubah di Pengaturan (perubahan pelemah tunduk pada Mode Komitmen).*
