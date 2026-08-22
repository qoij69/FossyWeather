package com.fossyfriend.fossyweather.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.domain.DayForecast
import com.fossyfriend.fossyweather.domain.HourForecast
import com.fossyfriend.fossyweather.domain.PressureAnalyzer
import com.fossyfriend.fossyweather.ui.components.LineChart
import com.fossyfriend.fossyweather.util.isoDateToDayLabel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PressureDetailScreen(
    currentPressure: Double,
    hourly: List<HourForecast>,
    daily: List<DayForecast>,
    onBack: () -> Unit
) {
    val fullSeries = remember(hourly) { hourly.map { it.pressureMsl.toFloat() } }
    val next24 = hourly.take(24)
    val trend = remember(next24) {
        if (next24.size > 1) PressureAnalyzer.trend(next24.first().pressureMsl, next24.last().pressureMsl) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pressure") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("${currentPressure.roundToInt()} hPa", style = MaterialTheme.typography.displayMedium)
                    trend?.let {
                        Text(it.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(PressureAnalyzer.condition(currentPressure), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Pressure over the next ${fullSeries.size / 24} days", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    if (fullSeries.size > 1) {
                        LineChart(values = fullSeries, modifier = Modifier.fillMaxWidth().height(120.dp))
                    }
                }
            }

            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("What pressure trends mean", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Rising pressure (\"high\") usually brings settled, clearer weather. " +
                            "Falling pressure (\"low\") often precedes clouds, wind, or rain. " +
                            "Rapid drops can signal a storm system approaching.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
