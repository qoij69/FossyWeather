package com.fossyfriend.fossyweather.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.domain.CurrentWeather
import com.fossyfriend.fossyweather.domain.MetricType
import kotlin.math.roundToInt

@Composable
fun UvAqiGrid(current: CurrentWeather, onMetricClick: (MetricType) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ExpressiveDetailCard(
            icon = Icons.Filled.WbSunny,
            label = "UV Index",
            value = uvLabel(current.uvIndex),
            progress = (current.uvIndex / 12f).toFloat().coerceIn(0f, 1f),
            progressColor = uvColor(current.uvIndex),
            onClick = { onMetricClick(MetricType.UV) },
            modifier = Modifier.weight(1f)
        )
        ExpressiveDetailCard(
            icon = Icons.Filled.Air,
            label = "AQI",
            value = current.aqi?.let { aqiLabel(it) } ?: "--",
            progress = current.aqi?.let { (it / 300f).toFloat().coerceIn(0f, 1f) },
            progressColor = current.aqi?.let { aqiColor(it) } ?: MaterialTheme.colorScheme.primary,
            onClick = { /* AQI detail if exists */ },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MoistureGrid(current: CurrentWeather, onMetricClick: (MetricType) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SmallFilledCard(
            icon = Icons.Filled.WaterDrop,
            label = "Humidity",
            value = "${current.humidity}%",
            onClick = { onMetricClick(MetricType.HUMIDITY) },
            modifier = Modifier.weight(1f)
        )
        SmallFilledCard(
            icon = Icons.Filled.Thermostat,
            label = "Dew Point",
            value = "${current.dewPoint.roundToInt()}°",
            onClick = { },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun VisibilityCloudGrid(current: CurrentWeather, onMetricClick: (MetricType) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SmallOutlinedCard(
            icon = Icons.Filled.Visibility,
            label = "Visibility",
            value = "${(current.visibilityMeters / 1000).roundToInt()} km",
            onClick = { onMetricClick(MetricType.VISIBILITY) },
            modifier = Modifier.weight(1f)
        )
        SmallOutlinedCard(
            icon = Icons.Filled.Cloud,
            label = "Cloud Cover",
            value = "${current.cloudCover}%",
            onClick = { onMetricClick(MetricType.CLOUD) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExpressiveDetailCard(
    icon: ImageVector,
    label: String,
    value: String,
    progress: Float?,
    progressColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
            progress?.let {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { it },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun SmallFilledCard(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SmallOutlinedCard(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun uvColor(uv: Double): androidx.compose.ui.graphics.Color {
    return when {
        uv < 3 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        uv < 6 -> androidx.compose.ui.graphics.Color(0xFFFFEB3B)
        uv < 8 -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        uv < 11 -> androidx.compose.ui.graphics.Color(0xFFF44336)
        else -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
    }
}

private fun aqiColor(aqi: Int): androidx.compose.ui.graphics.Color {
    return when {
        aqi <= 50 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        aqi <= 100 -> androidx.compose.ui.graphics.Color(0xFFFFEB3B)
        aqi <= 150 -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        aqi <= 200 -> androidx.compose.ui.graphics.Color(0xFFF44336)
        else -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
    }
}

private fun aqiLabel(aqi: Int): String {
    val level = when {
        aqi <= 50 -> "Good"
        aqi <= 100 -> "Moderate"
        aqi <= 150 -> "Unhealthy (SG)"
        aqi <= 200 -> "Unhealthy"
        aqi <= 300 -> "Very Unhealthy"
        else -> "Hazardous"
    }
    return "$aqi · $level"
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

