package com.fossyfriend.fossyweather.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.data.prefs.TempUnit
import com.fossyfriend.fossyweather.domain.HourForecast
import com.fossyfriend.fossyweather.domain.WeatherCode
import com.fossyfriend.fossyweather.util.formatTempShort
import com.fossyfriend.fossyweather.util.isoToHourLabel

@Composable
fun HourlyForecastRow(
    hours: List<HourForecast>,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier,
    onHourClick: (HourForecast) -> Unit = {}
) {
    SectionCard(title = "Hourly forecast", modifier = modifier) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            items(hours.take(24)) { hour ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onHourClick(hour) }
                ) {
                    Text(isoToHourLabel(hour.isoTime), style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(6.dp))
                    Icon(
                        WeatherCode.icon(hour.weatherCode),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(formatTempShort(hour.temperature, tempUnit), style = MaterialTheme.typography.titleMedium)
                    if (hour.precipitationProbability > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.WaterDrop,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                "${hour.precipitationProbability}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.extraLarge,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    elevation: CardElevation = CardDefaults.cardElevation(),
    isOutlined: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier.fillMaxWidth().let { m ->
        if (onClick != null) m.clickable(onClick = onClick) else m
    }

    if (isOutlined) {
        OutlinedCard(
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
            border = CardDefaults.outlinedCardBorder(enabled = true),
            elevation = elevation
        ) {
            SectionContent(title, trailing, onClick, content)
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = elevation
        ) {
            SectionContent(title, trailing, onClick, content)
        }
    }
}

@Composable
private fun SectionContent(
    title: String,
    trailing: (@Composable () -> Unit)?,
    onClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                trailing?.invoke()
                if (onClick != null) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = "Open details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        content()
    }
}
