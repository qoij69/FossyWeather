package com.fossyfriend.fossyweather.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.domain.CurrentWeather
import com.fossyfriend.fossyweather.domain.MetricType
import com.fossyfriend.fossyweather.domain.PressureAnalyzer
import kotlin.math.roundToInt

data class DetailItem(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val caption: String? = null,
    val metric: MetricType? = null
)

@Composable
fun DetailsGrid(current: CurrentWeather, onMetricClick: (MetricType) -> Unit, modifier: Modifier = Modifier) {
    val items = listOf(
        DetailItem(Icons.Filled.WaterDrop, "Humidity", "${current.humidity}%", metric = MetricType.HUMIDITY),
        DetailItem(
            Icons.Filled.Speed, "Pressure", "${current.pressureMsl.roundToInt()} hPa",
            PressureAnalyzer.condition(current.pressureMsl), metric = MetricType.PRESSURE
        ),
        DetailItem(Icons.Filled.Air, "Wind", "${current.windSpeed.roundToInt()} km/h", metric = MetricType.WIND),
        DetailItem(Icons.Filled.Cyclone, "Gusts", "${current.windGusts.roundToInt()} km/h", metric = MetricType.WIND),
        DetailItem(Icons.Filled.WbSunny, "UV Index", uvLabel(current.uvIndex), metric = MetricType.UV),
        DetailItem(Icons.Filled.Visibility, "Visibility", "${(current.visibilityMeters / 1000).roundToInt()} km", metric = MetricType.VISIBILITY),
        DetailItem(Icons.Filled.Cloud, "Cloud Cover", "${current.cloudCover}%", metric = MetricType.CLOUD),
        DetailItem(Icons.Filled.Explore, "Wind Dir.", windDirectionLabel(current.windDirection), metric = MetricType.WIND)
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    DetailCard(item, onClick = { item.metric?.let(onMetricClick) }, modifier = Modifier.weight(1f))
                }
                if (rowItems.size < 2) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailCard(item: DetailItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable(enabled = item.metric != null, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(item.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(item.value, style = MaterialTheme.typography.titleLarge)
            item.caption?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun uvLabel(uv: Double): String {
    val level = when {
        uv < 3 -> "Low"
        uv < 6 -> "Moderate"
        uv < 8 -> "High"
        uv < 11 -> "Very High"
        else -> "Extreme"
    }
    return "${uv.roundToInt()} · $level"
}

private fun windDirectionLabel(deg: Int): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val idx = (((deg % 360) + 22.5) / 45.0).toInt() % 8
    return "${dirs[idx]} ($deg°)"
}
