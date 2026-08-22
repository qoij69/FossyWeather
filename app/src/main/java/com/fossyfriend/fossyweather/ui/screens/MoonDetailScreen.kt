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
import com.fossyfriend.fossyweather.domain.MoonPhaseCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoonDetailScreen(onBack: () -> Unit) {
    val today = remember { LocalDate.now() }
    val current = remember { MoonPhaseCalculator.calculate(today) }
    val next14Days = remember { (0..13).map { today.plusDays(it.toLong()) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moon") },
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
                        Text(current.phaseName, style = MaterialTheme.typography.headlineMedium)
                        Text("${current.illuminationPercent}% illuminated", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Next new moon: ${current.nextNewMoon.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Next full moon: ${current.nextFullMoon.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Calculated on-device from the synodic month cycle — no network needed, always available.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Text("Next 14 days", style = MaterialTheme.typography.titleMedium) }

            items(next14Days) { date ->
                val info = remember(date) { MoonPhaseCalculator.calculate(date) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(date.format(DateTimeFormatter.ofPattern("EEE, MMM d")), style = MaterialTheme.typography.bodyMedium)
                    Text(info.phaseName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${info.illuminationPercent}%", style = MaterialTheme.typography.bodyLarge)
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
