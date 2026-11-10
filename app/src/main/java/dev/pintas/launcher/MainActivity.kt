package dev.pintas.launcher

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private const val PREFS = "pintas"
private const val KEY_TIPS_DISABLED = "tips_disabled"
private const val KEY_CATEGORY = "category"
private const val ALL = "All"

private fun tipSeenKey(id: String) = "tip_seen_$id"

/** Warna ikon per kategori (nada sedang, terbaca di mode terang maupun gelap). */
private val categoryColors = mapOf(
    "Main" to Color(0xFF7E57C2),
    "Comms" to Color(0xFF42A5F5),
    "Work" to Color(0xFF26A69A),
    "Media" to Color(0xFFEF5350),
    "Maps" to Color(0xFF66BB6A),
    "Info" to Color(0xFFFFA726),
    "Edu" to Color(0xFF8D6E63),
    "Business" to Color(0xFF78909C),
)
private val defaultIconColor = Color(0xFF9E9E9E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val services = loadServices(this)
        setContent {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LauncherScreen(services)
                }
            }
        }
    }
}

@Composable
fun LauncherScreen(services: List<Service>) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    val categories = remember(services) { listOf(ALL) + services.map { it.category }.distinct() }
    var selected by remember {
        mutableStateOf(prefs.getString(KEY_CATEGORY, ALL)?.takeIf { it in categories } ?: ALL)
    }
    var pendingTip by remember { mutableStateOf<Service?>(null) }
    val shown = remember(selected, services) {
        if (selected == ALL) services else services.filter { it.category == selected }
    }

    fun onServiceClick(service: Service) {
        val showTip = service.tip.isNotEmpty() &&
            !prefs.getBoolean(KEY_TIPS_DISABLED, false) &&
            !prefs.getBoolean(tipSeenKey(service.id), false)
        if (showTip) pendingTip = service else openUrl(context, service.url)
    }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        CategoryDropdown(categories, selected) { choice ->
            selected = choice
            prefs.edit().putString(KEY_CATEGORY, choice).apply()
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 88.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(shown, key = { it.id }) { service ->
                ServiceItem(service) { onServiceClick(service) }
            }
        }
    }

    pendingTip?.let { service ->
        AlertDialog(
            onDismissRequest = { pendingTip = null },
            title = { Text(service.name) },
            text = { Text(service.tip) },
            confirmButton = {
                TextButton(onClick = {
                    prefs.edit().putBoolean(tipSeenKey(service.id), true).apply()
                    pendingTip = null
                    openUrl(context, service.url)
                }) { Text(stringResource(R.string.tip_open)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    prefs.edit().putBoolean(KEY_TIPS_DISABLED, true).apply()
                    pendingTip = null
                    openUrl(context, service.url)
                }) { Text(stringResource(R.string.tip_disable)) }
            },
        )
    }
}

@Composable
fun CategoryDropdown(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$selected ▾")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        expanded = false
                        onSelect(category)
                    },
                )
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun ServiceItem(service: Service, onClick: () -> Unit) {
    val context = LocalContext.current
    // Cari drawable berdasarkan nama; kalau tidak ada, pakai ikon generik.
    val iconRes = remember(service.icon) {
        val id = context.resources.getIdentifier(service.icon, "drawable", context.packageName)
        if (id != 0) id else R.drawable.ic_service_default
    }
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = categoryColors[service.category] ?: defaultIconColor,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = service.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

/** Membuka URL lewat Chrome Custom Tabs; jika tidak tersedia, pakai browser apa pun. */
fun openUrl(context: Context, url: String) {
    val uri = Uri.parse(url)
    try {
        CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, uri)
    } catch (e: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e2: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_browser, Toast.LENGTH_SHORT).show()
        }
    }
}
