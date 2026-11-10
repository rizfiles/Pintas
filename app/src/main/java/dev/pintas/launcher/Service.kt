package dev.pintas.launcher

import android.content.Context
import org.json.JSONArray

/**
 * Satu layanan di daftar.
 * [category] = nama kategori untuk dropdown.
 * [icon]     = nama file drawable (tanpa ekstensi), boleh kosong.
 * [tip]      = petunjuk singkat yang ditampilkan sekali sebelum layanan pertama kali dibuka, boleh kosong.
 */
data class Service(
    val id: String,
    val name: String,
    val url: String,
    val category: String,
    val icon: String,
    val tip: String,
)

/** Membaca daftar layanan dari assets/services.json (tanpa library tambahan). */
fun loadServices(context: Context): List<Service> {
    val text = context.assets.open("services.json").bufferedReader().use { it.readText() }
    val array = JSONArray(text)
    return List(array.length()) { i ->
        val o = array.getJSONObject(i)
        Service(
            id = o.getString("id"),
            name = o.getString("name"),
            url = o.getString("url"),
            category = o.optString("category", "Other"),
            icon = o.optString("icon", ""),
            tip = o.optString("tip", ""),
        )
    }.filter { it.url.startsWith("https://") } // hanya HTTPS
}
