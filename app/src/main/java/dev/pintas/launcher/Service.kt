package dev.pintas.launcher

import android.content.Context
import org.json.JSONArray

/** Satu layanan di daftar. [icon] adalah nama file drawable (tanpa ekstensi), boleh kosong. */
data class Service(
    val id: String,
    val name: String,
    val url: String,
    val icon: String,
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
            icon = o.optString("icon", ""),
        )
    }.filter { it.url.startsWith("https://") } // hanya HTTPS
}
