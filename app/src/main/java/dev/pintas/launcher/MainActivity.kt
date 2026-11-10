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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private const val PREFS = "pintas"
private const val KEY_TIPS_DISABLED = "tips_disabled"
private fun tipSeenKey(id: String) = "tip_seen_$id"

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
    var pendingTip by remember { mutableStateOf<Service?>(null) }

    fun onServiceClick(service: Service) {
        val showTip = service.tip.isNotEmpty() &&
            !prefs.getBoolean(KEY_TIPS_DISABLED, false) &&
            !prefs.getBoolean(tipSeenKey(service.id), false)
        if (showTip) pendingTip = service else openUrl(context, service.url)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        modifier = Modifier.safeDrawingPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(services, key = { it.id }) { service ->
            ServiceItem(service) { onServiceClick(service) }
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
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
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
