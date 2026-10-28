# Pengingat

Aplikasi Android pribadi: pencatat progres berhenti dari maksiat dengan
alarm Subuh (Kulon Progo, kriteria Kemenag), check-in paksa Level 3 (root),
popup peringatan persisten untuk aplikasi pantauan, widget streak + refleksi
Qur'ani, dan Mode Komitmen 24 jam. 100% offline — aplikasi tidak meminta
izin INTERNET. Spesifikasi lengkap: lihat PRD (PRD-Aplikasi-Pengingat.md,
disertakan terpisah oleh pembuat).

## Build (GitHub Actions)

1. Buat repo GitHub, push seluruh isi folder ini.
2. (Sangat disarankan) Buat keystore tetap dan simpan sebagai Secrets
   agar APK antar-build bisa saling menimpa tanpa uninstall:
   - Buat keystore: `keytool -genkeypair -v -keystore release.jks -alias pengingat -keyalg RSA -keysize 2048 -validity 10000`
   - Secrets repo: `KEYSTORE_B64` (isi: `base64 -w0 release.jks`),
     `KEYSTORE_PASSWORD`, `KEY_ALIAS` (= pengingat), `KEY_PASSWORD`.
   - Tanpa secrets ini, build tetap jalan memakai debug signing —
     tapi tanda tangan bisa berbeda antar-runner.
3. Tab **Actions** → **Build APK** → **Run workflow**. Unduh artifact
   **Pengingat-APK**.
4. Instal APK di HP (sideload), buka aplikasi, selesaikan Onboarding, dan
   **beri izin root ke aplikasi Pengingat** saat KernelSU memintanya
   (atau dari KernelSU Manager → Superuser → Pengingat). Tidak ada modul
   yang perlu di-flash — aplikasi ini berdiri sendiri.

## Struktur

- `app/` — aplikasi Kotlin + Jetpack Compose (data/, ui/, service/, widget/, root/)
- `.github/workflows/build-apk.yml` — build CI

## Catatan konten

Teks ayat di `app/src/main/assets/quotes.json` mengikuti terjemahan Kemenag
sebaik yang diketahui penulis dan **wajib diverifikasi final** ke mushaf
Kemenag sebelum dianggap rilis final (aturan PRD §5).
