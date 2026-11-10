# Catatan Ikon

Ikon layanan Google (Gmail, Drive, dsb.) adalah merek dagang Google LLC dan **tidak**
tercakup oleh lisensi proyek ini. Ikon tersebut hanya dipakai kecil, tanpa diubah, untuk
menunjukkan layanan mana yang akan dibuka.

Cara menambah ikon: taruh file di `app/src/main/res/drawable/` dengan nama yang sama
dengan field `"icon"` di `app/src/main/assets/services.json` (mis. `ic_gmail.xml` atau `ic_gmail.png`).
Jika file tidak ada, aplikasi memakai ikon generik `ic_service_default`.

Sumber ikon resmi: https://about.google/brand-resource-center/
Pelajari aturannya sebelum memakai: jangan diubah, jangan dijadikan bagian logo aplikasi,
jangan menyiratkan afiliasi dengan Google.
