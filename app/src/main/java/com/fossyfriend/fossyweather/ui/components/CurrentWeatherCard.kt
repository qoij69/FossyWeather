package com.fossyfriend.fossyweather.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.data.prefs.TempUnit
import com.fossyfriend.fossyweather.domain.CurrentWeather
import com.fossyfriend.fossyweather.domain.WeatherCode
import com.fossyfriend.fossyweather.util.formatTemp
import kotlin.random.Random

@Composable
fun CurrentWeatherCard(
    locationName: String,
    current: CurrentWeather,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_icon_pulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    val cardDrift by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_drift"
    )

    Card(
        modifier = modifier.fillMaxWidth()
            .graphicsLayer {
                translationY = cardDrift
                rotationZ = cardDrift * 0.2f
            }
            .let { m ->
                if (onClick != null) m.clickable(onClick = onClick) else m
            },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))) {
            WeatherAnimation(weatherCode = current.weatherCode)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Icon(
                    imageVector = WeatherCode.icon(current.weatherCode, current.isDay),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp).scale(iconScale),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = formatTemp(current.temperature, tempUnit),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = WeatherCode.description(current.weatherCode),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Feels like ${formatTemp(current.apparentTemperature, tempUnit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                if (onClick != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Tap for more",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherAnimation(weatherCode: Int) {
    val isRain = weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82)
    val isSnow = weatherCode in listOf(71, 73, 75, 77, 85, 86)
    val isCloudy = weatherCode in listOf(1, 2, 3, 45, 48)
    val isClear = weatherCode == 0

    if (isRain || isSnow) {
        ParticleAnimation(isSnow = isSnow)
    } else if (isCloudy) {
        CloudAnimation()
    } else if (isClear) {
        SunAnimation()
    }
}

@Composable
private fun ParticleAnimation(isSnow: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_progress"
    )

    val particles = remember {
        List(70) {
            Offset(Random.nextFloat(), Random.nextFloat())
        }
    }

    val color = if (isSnow) Color.White.copy(alpha = 0.5f) else Color(0xFF90CAF9).copy(alpha = 0.4f)
    val length = if (isSnow) 5f else 30f
    val stroke = if (isSnow) 6f else 4f
    val windSkew = if (isSnow) 0.15f else 0.08f

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val x = ((p.x + progress * windSkew) % 1f) * size.width
            val y = ((p.y + progress) % 1f) * size.height
            if (isSnow) {
                drawCircle(color = color, radius = stroke * Random.nextFloat().coerceAtLeast(0.5f), center = Offset(x, y))
            } else {
                drawLine(
                    color = color,
                    start = Offset(x, y),
                    end = Offset(x + (size.width * windSkew), y + length),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun CloudAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val xOffset by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_x"
    )

    val color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.05f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = color,
            radius = size.width * 0.3f,
            center = Offset(size.width * xOffset, size.height * 0.2f)
        )
        drawCircle(
            color = color,
            radius = size.width * 0.25f,
            center = Offset(size.width * (xOffset + 0.2f), size.height * 0.3f)
        )
    }
}

@Composable
private fun SunAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "sun")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_rotation"
    )

    val color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.04f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width * 0.85f, size.height * 0.15f)
        val radius = size.width * 0.45f
        drawCircle(color = color, radius = radius, center = center)
        
        // Ray simulation
        for (i in 0 until 12) {
            val angle = (rotation + i * 30) * (Math.PI / 180).toFloat()
            val start = Offset(
                center.x + (radius * 0.7f) * kotlin.math.cos(angle),
                center.y + (radius * 0.7f) * kotlin.math.sin(angle)
            )
            val end = Offset(
                center.x + (radius * 1.3f) * kotlin.math.cos(angle),
                center.y + (radius * 1.3f) * kotlin.math.sin(angle)
            )
            drawLine(color = color, start = start, end = end, strokeWidth = 12f, cap = StrokeCap.Round)
        }
    }
}
