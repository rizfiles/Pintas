[ICONS.md](https://github.com/user-attachments/files/34625141/ICONS.md)
# Catatan Ikon

Semua ikon layanan di aplikasi ini adalah **Material Symbols** (gaya Outlined) buatan Google,
berlisensi **Apache License 2.0**: https://github.com/google/material-design-icons
Ikon itu bebas dipakai dan didistribusikan ulang sesuai lisensi tersebut.

Aplikasi ini **tidak memakai logo atau ikon produk resmi Google**. Nama produk (Gmail, Drive,
YouTube, dsb.) adalah merek dagang Google LLC dan hanya disebut untuk menunjukkan layanan
mana yang akan dibuka. Proyek ini tidak berafiliasi dengan Google.

## Mengganti atau menambah ikon
Taruh file di `app/src/main/res/drawable/` dengan nama yang sama dengan field `"icon"`
di `app/src/main/assets/services.json` (mis. `ic_gmail.xml`). Jika file tidak ada,
aplikasi memakai ikon generik `ic_service_default`.

Warna ikon ditentukan per kategori di `MainActivity.kt` (`categoryColors`).
