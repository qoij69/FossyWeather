package com.fossyfriend.fossyweather.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.domain.MarinePoint
import kotlin.math.roundToInt

/**
 * Shows sea-level height (tide) plus wave/swell data from Open-Meteo's Marine API.
 * Open-Meteo derives sea_level_height_msl from a global tidal model, so this is
 * an approximation best suited to open-coast locations, not precise harbor tide tables.
 */
@Composable
fun TideCard(marine: List<MarinePoint>, isCoastal: Boolean, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    SectionCard(title = "Tide & Sea", modifier = modifier, onClick = onClick) {
        if (!isCoastal || marine.isEmpty()) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Filled.Waves, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(
                    "No tide/marine data — this location looks inland.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@SectionCard
        }

        val tideSeries = marine.mapNotNull { it.seaLevelHeight?.toFloat() }
        val current = marine.firstOrNull()

        Row {
            current?.waveHeight?.let {
                MiniStat(label = "Wave height", value = "${"%.1f".format(it)} m")
            }
            Spacer(Modifier.width(20.dp))
            current?.seaSurfaceTemp?.let {
                MiniStat(label = "Sea temp", value = "${it.roundToInt()}°C")
            }
        }
        if (tideSeries.size > 1) {
            Spacer(Modifier.height(14.dp))
            Text("Tide height (72h, model estimate)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            LineChart(values = tideSeries, modifier = Modifier.fillMaxWidth().height(60.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Tide data is modeled from Open-Meteo's Marine API and may differ from official local tide tables.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
