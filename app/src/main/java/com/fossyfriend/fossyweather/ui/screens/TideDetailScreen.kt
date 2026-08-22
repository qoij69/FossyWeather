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
import com.fossyfriend.fossyweather.domain.MarinePoint
import com.fossyfriend.fossyweather.ui.components.LineChart
import com.fossyfriend.fossyweather.util.isoToTimeLabel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TideDetailScreen(marine: List<MarinePoint>, isCoastal: Boolean, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tide & Sea") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!isCoastal || marine.isEmpty()) {
            Column(
                Modifier.padding(padding).fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No marine data for this location — it looks inland.", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        val tideSeries = remember(marine) { marine.mapNotNull { it.seaLevelHeight?.toFloat() } }
        val waveSeries = remember(marine) { marine.mapNotNull { it.waveHeight?.toFloat() } }
        val current = marine.firstOrNull()

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row {
                            current?.waveHeight?.let {
                                Column(Modifier.weight(1f)) {
                                    Text("Wave height", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${"%.1f".format(it)} m", style = MaterialTheme.typography.headlineMedium)
                                }
                            }
                            current?.seaSurfaceTemp?.let {
                                Column(Modifier.weight(1f)) {
                                    Text("Sea temp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${it.roundToInt()}°C", style = MaterialTheme.typography.headlineMedium)
                                }
                            }
                        }
                    }
                }
            }

            if (tideSeries.size > 1) {
                item {
                    Card(shape = RoundedCornerShape(24.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Tide height — next 72h (model estimate)", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(12.dp))
                            LineChart(values = tideSeries, modifier = Modifier.fillMaxWidth().height(100.dp))
                        }
                    }
                }
            }

            if (waveSeries.size > 1) {
                item {
                    Card(shape = RoundedCornerShape(24.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Wave height — next 72h", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(12.dp))
                            LineChart(values = waveSeries, modifier = Modifier.fillMaxWidth().height(100.dp))
                        }
                    }
                }
            }

            item {
                Text(
                    "Tide data is modeled from Open-Meteo's Marine API and may differ from official local tide tables — treat it as an estimate, not a navigation source.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item { Text("Hour-by-hour", style = MaterialTheme.typography.titleMedium) }

            items(marine.take(48)) { point ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(isoToTimeLabel(point.isoTime), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        point.seaLevelHeight?.let { "${"%.2f".format(it)} m" } ?: "—",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
