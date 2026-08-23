package com.fossyfriend.fossyweather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.domain.HourForecast
import com.fossyfriend.fossyweather.domain.PressureAnalyzer
import kotlin.math.roundToInt

@Composable
fun PressureTrendCard(hours: List<HourForecast>, currentPressure: Double, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val next24 = hours.take(24)
    if (next24.isEmpty()) return
    val trend = PressureAnalyzer.trend(next24.first().pressureMsl, next24.last().pressureMsl)

    SectionCard(
        title = "Pressure",
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("${currentPressure.roundToInt()} hPa", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(12.dp))
            Text(trend.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Text(
            PressureAnalyzer.condition(currentPressure),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        LineChart(
            values = next24.map { it.pressureMsl.toFloat() },
            modifier = Modifier.fillMaxWidth().height(70.dp)
        )
    }
}

@Composable
fun LineChart(values: List<Float>, modifier: Modifier = Modifier) {
    if (values.size < 2) return
    val lineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val min = values.min()
    val max = values.max()
    val range = (max - min).takeIf { it > 0.01f } ?: 1f

    Canvas(modifier = modifier) {
        val stepX = size.width / (values.size - 1)
        val path = Path()
        val fillPath = Path()
        values.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - ((v - min) / range) * size.height
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(size.width, size.height)
        fillPath.close()
        drawPath(fillPath, color = fillColor)
        drawPath(path, color = lineColor, style = Stroke(width = 5f, cap = StrokeCap.Round))
    }
}
