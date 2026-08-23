package com.fossyfriend.fossyweather.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.data.prefs.TempUnit
import com.fossyfriend.fossyweather.domain.DayForecast
import com.fossyfriend.fossyweather.domain.WeatherCode
import com.fossyfriend.fossyweather.util.formatTempShort
import com.fossyfriend.fossyweather.util.isoDateToDayLabel

@Composable
fun DailyForecastList(
    days: List<DayForecast>,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier,
    onDayClick: (DayForecast) -> Unit = {}
) {
    val overallMin = days.minOfOrNull { it.tempMin } ?: 0.0
    val overallMax = days.maxOfOrNull { it.tempMax } ?: 1.0
    val range = (overallMax - overallMin).coerceAtLeast(1.0)

    SectionCard(
        title = "8-day forecast",
        modifier = modifier,
        isOutlined = false,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column {
            days.forEachIndexed { index, day ->
                ListItem(
                    modifier = Modifier.clickable { onDayClick(day) },
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                isoDateToDayLabel(day.isoDate),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.width(48.dp)
                            )
                            Icon(
                                WeatherCode.icon(day.weatherCode),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            if (day.precipitationProbabilityMax > 10) {
                                Text(
                                    "${day.precipitationProbabilityMax}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                formatTempShort(day.tempMin, tempUnit),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val startFrac = ((day.tempMin - overallMin) / range).toFloat().coerceIn(0f, 1f)
                            val endFrac = ((day.tempMax - overallMin) / range).toFloat().coerceIn(0f, 1f)
                            TempRangeBar(startFrac, endFrac, modifier = Modifier.width(80.dp).height(6.dp).padding(horizontal = 8.dp))
                            Text(
                                formatTempShort(day.tempMax, tempUnit),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
                if (index < days.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TempRangeBar(startFrac: Float, endFrac: Float, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val barColor = MaterialTheme.colorScheme.primary
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val h = size.height
        drawRoundRect(
            color = trackColor,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2)
        )
        val startX = size.width * startFrac
        val endX = size.width * endFrac
        drawRoundRect(
            color = barColor,
            topLeft = androidx.compose.ui.geometry.Offset(startX, 0f),
            size = androidx.compose.ui.geometry.Size((endX - startX).coerceAtLeast(h), h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2)
        )
    }
}
