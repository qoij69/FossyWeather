package com.fossyfriend.fossyweather.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fossyfriend.fossyweather.data.prefs.TempUnit
import com.fossyfriend.fossyweather.domain.WeatherBundle
import com.fossyfriend.fossyweather.domain.WeatherCode
import com.fossyfriend.fossyweather.util.formatTemp
import kotlin.random.Random

@Composable
fun HeroWeatherOverlay(
    bundle: WeatherBundle,
    tempUnit: TempUnit,
    onBack: () -> Unit
) {
    val ringCount = 14
    val segmentsPerRing = 4
    val totalSegments = ringCount * segmentsPerRing
    
    val infiniteTransition = rememberInfiniteTransition(label = "vortex_root")
    
    val segmentAnimations = List(totalSegments) { i ->
        val ringIndex = i / segmentsPerRing
        val segmentInRingIndex = i % segmentsPerRing
        
        // Vary speeds: Inner rings generally faster, plus segment randomness
        val baseDuration = 1800 + (ringIndex * 400)
        val duration = (baseDuration * (0.7f + (segmentInRingIndex * 0.3f))).toInt()
        
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "seg_rot_$i"
        )
    }

    // Fixed random properties for segments
    val segmentProps = remember {
        List(totalSegments) { i ->
            val ringIndex = i / segmentsPerRing
            val length = 15f + (ringIndex * 3f) + (Random.nextFloat() * 40f)
            val startOffset = Random.nextFloat() * 360f
            Pair(length, startOffset)
        }
    }

    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width, 0f)
            
            segmentAnimations.forEachIndexed { i, rotationState ->
                val ringIndex = i / segmentsPerRing
                val radius = size.width * (1.1f - (ringIndex.toFloat() / (ringCount + 4)))
                val (length, startOffset) = segmentProps[i]
                
                // Direction is one-way: Counter-Clockwise
                rotate(degrees = -(rotationState.value + startOffset), pivot = center) {
                    // Sweep gradient for the "fade out" trail effect
                    val trailBrush = Brush.sweepGradient(
                        0f to Color.Transparent,
                        length / 360f to contentColor.copy(alpha = 0.5f * (1f - ringIndex.toFloat() / ringCount)),
                        (length + 1f) / 360f to Color.Transparent,
                        center = center
                    )

                    drawArc(
                        brush = trailBrush,
                        startAngle = 0f,
                        sweepAngle = length + 5f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(
                            width = 12f - (ringIndex.toFloat() / ringCount * 8f),
                            cap = StrokeCap.Round
                        )
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = bundle.locationName,
                    style = MaterialTheme.typography.displaySmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = WeatherCode.description(bundle.current.weatherCode),
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }

            Text(
                text = formatTemp(bundle.current.temperature, tempUnit),
                style = TextStyle(
                    fontSize = 160.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = contentColor
                )
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoItem(label = "Feels Like", value = formatTemp(bundle.current.apparentTemperature, tempUnit), color = contentColor)
                    InfoItem(label = "Humidity", value = "${bundle.current.humidity}%", color = contentColor)
                    InfoItem(label = "Wind", value = "${bundle.current.windSpeed.toInt()} km/h", color = contentColor)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoItem(label = "UV Index", value = bundle.current.uvIndex.toInt().toString(), color = contentColor)
                    InfoItem(label = "AQI", value = bundle.current.aqi?.toString() ?: "--", color = contentColor)
                    InfoItem(label = "Pressure", value = "${bundle.current.pressureMsl.toInt()} hPa", color = contentColor)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoItem(label = "Visibility", value = "${(bundle.current.visibilityMeters / 1000).toInt()} km", color = contentColor)
                    InfoItem(label = "Cloud Cover", value = "${bundle.current.cloudCover}%", color = contentColor)
                    InfoItem(label = "Dew Point", value = "${bundle.current.dewPoint.toInt()}°", color = contentColor)
                }
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = contentColor)
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
    }
}
