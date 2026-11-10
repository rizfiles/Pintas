[README.md](https://github.com/user-attachments/files/34625177/README.md)
# Pintas

Peluncur ringan di Android yang mengumpulkan tautan ke layanan Google dalam satu layar.
Cocok untuk HP dengan penyimpanan dan RAM terbatas: tidak perlu memasang aplikasi satu per satu.

- Tanpa izin (permission) apa pun
- Tanpa iklan, tanpa pelacak, tanpa login sendiri
- Tautan dibuka lewat Chrome Custom Tabs, sehingga login ditangani browser
- Daftar layanan ada di `app/src/main/assets/services.json`, mudah ditambah lewat pull request

## Membangun
1. Buka folder ini di Android Studio, tunggu Gradle sync.
2. Jalankan di emulator atau perangkat (`Run > app`).
3. Jika `gradlew` belum ada: jalankan `gradle wrapper` atau buat lewat Android Studio.

## Penafian
Proyek ini tidak berafiliasi dengan Google. Gmail, Google Drive, YouTube, dan nama produk
lain adalah merek dagang Google LLC. Lihat `ICONS.md`.

## Lisensi
Pilih lisensi (MIT/Apache-2.0/GPL) dan tambahkan file `LICENSE`.
