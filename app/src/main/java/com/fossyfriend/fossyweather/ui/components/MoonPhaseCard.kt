package com.fossyfriend.fossyweather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.domain.MoonPhaseInfo
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun MoonPhaseCard(moon: MoonPhaseInfo, sunrise: String, sunset: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    SectionCard(
        title = "S☀️n & Moon",
        modifier = modifier,
        onClick = onClick,
        isOutlined = false,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoonDisc(illumination = moon.illuminationPercent, waxing = moon.ageDays < 14.77, modifier = Modifier.size(64.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(moon.phaseName, style = MaterialTheme.typography.titleMedium)
                Text("${moon.illuminationPercent}% illuminated", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Next full moon: ${moon.nextFullMoon.format(DateTimeFormatter.ofPattern("MMM d"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SunTimeColumn(label = "S☀️nrise", timeIso = sunrise)
            SunTimeColumn(label = "S☀️nset", timeIso = sunset)
        }
    }
}

@Composable
private fun SunTimeColumn(label: String, timeIso: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(com.fossyfriend.fossyweather.util.isoToTimeLabel(timeIso), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun MoonDisc(illumination: Int, waxing: Boolean, modifier: Modifier = Modifier) {
    val litColor = MaterialTheme.colorScheme.primary
    val darkColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)
        // Base dark disc
        drawCircle(color = darkColor, radius = radius, center = center)

        // Lit portion approximated as a circle clipped to a lens shape based on illumination fraction.
        val frac = illumination / 100f
        if (frac > 0.001f) {
            val litPath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
            }
            clipPath(litPath) {
                val terminatorOffsetX = radius * (1 - 2 * frac)
                val startAngleSide = if (waxing) center.x + terminatorOffsetX else center.x - terminatorOffsetX
                if (waxing) {
                    drawRect(
                        color = litColor,
                        topLeft = Offset(startAngleSide, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                    )
                } else {
                    drawRect(
                        color = litColor,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(startAngleSide - (center.x - radius), radius * 2)
                    )
                }
                drawCircle(color = litColor, radius = radius, center = center, alpha = 0f) // no-op to keep clip valid
            }
        }
    }
}
