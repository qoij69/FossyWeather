package com.fossyfriend.fossyweather.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.domain.CurrentWeather
import kotlin.math.roundToInt

@Composable
fun WindCard(current: CurrentWeather, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Air, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Wind", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    WindStat(label = "Speed", value = "${current.windSpeed.roundToInt()} km/h", icon = Icons.Filled.Air)
                    Spacer(Modifier.height(8.dp))
                    WindStat(label = "Gusts", value = "${current.windGusts.roundToInt()} km/h", icon = Icons.Filled.Cyclone)
                }
                
                val rotation by animateFloatAsState(targetValue = current.windDirection.toFloat(), label = "wind_dir")
                AssistChip(
                    onClick = {},
                    label = { Text(windDirectionLabel(current.windDirection)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp).rotate(rotation)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    }
}

@Composable
private fun WindStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun windDirectionLabel(deg: Int): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val idx = (((deg % 360) + 22.5) / 45.0).toInt() % 8
    return "${dirs[idx]} ($deg°)"
}
