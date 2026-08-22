package com.fossyfriend.fossyweather.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.domain.HourForecast
import com.fossyfriend.fossyweather.domain.MetricType
import com.fossyfriend.fossyweather.ui.components.LineChart
import com.fossyfriend.fossyweather.util.isoToHourLabel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricDetailScreen(
    metric: MetricType,
    hourly: List<HourForecast>,
    onBack: () -> Unit
) {
    val relevant = remember(hourly, metric) { hourly.take(48) }
    val current = relevant.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(metric.label) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        current?.let {
                            Text(
                                "${formatValue(metric, metric.valueOf(it))} ${metric.unit}",
                                style = MaterialTheme.typography.displayMedium
                            )
                        }
                        Text("Now", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Text(metric.description(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Next 48 hours", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        val values = relevant.map { metric.valueOf(it).toFloat() }
                        if (values.size > 1) {
                            LineChart(values = values, modifier = Modifier.fillMaxWidth().height(100.dp))
                        }
                    }
                }
            }

            item {
                Text("Hour-by-hour", style = MaterialTheme.typography.titleMedium)
            }

            items(relevant) { hour ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(isoToHourLabel(hour.isoTime), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${formatValue(metric, metric.valueOf(hour))} ${metric.unit}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

private fun formatValue(metric: MetricType, value: Double): String = when (metric) {
    MetricType.VISIBILITY -> "%.1f".format(value)
    else -> value.roundToInt().toString()
}
