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

    SectionCard(title = "8-day forecast", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            days.forEach { day ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onDayClick(day) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            modifier = Modifier.width(36.dp).padding(start = 4.dp)
                        )
                    } else {
                        Spacer(Modifier.width(36.dp))
                    }
                    Text(
                        formatTempShort(day.tempMin, tempUnit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(32.dp)
                    )
                    val startFrac = ((day.tempMin - overallMin) / range).toFloat().coerceIn(0f, 1f)
                    val endFrac = ((day.tempMax - overallMin) / range).toFloat().coerceIn(0f, 1f)
                    TempRangeBar(startFrac, endFrac, modifier = Modifier.weight(1f).height(6.dp))
                    Text(
                        formatTempShort(day.tempMax, tempUnit),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
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
