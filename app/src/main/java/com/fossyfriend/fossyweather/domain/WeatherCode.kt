package com.fossyfriend.fossyweather.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps WMO weather codes (used by Open-Meteo) to human descriptions and icons.
 * https://open-meteo.com/en/docs (WMO Weather interpretation codes)
 */
object WeatherCode {

    fun description(code: Int): String = when (code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing rain"
        71, 73, 75 -> "Snow fall"
        77 -> "Snow grains"
        80, 81, 82 -> "Rain showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Unknown"
    }

    fun icon(code: Int, isDay: Boolean = true): ImageVector = when (code) {
        0 -> if (isDay) Icons.Filled.WbSunny else Icons.Filled.NightsStay
        1, 2 -> if (isDay) Icons.Filled.WbCloudy else Icons.Filled.CloudQueue
        3 -> Icons.Filled.Cloud
        45, 48 -> Icons.Filled.FilterDrama
        51, 53, 55, 56, 57 -> Icons.Filled.Grain
        61, 63, 65, 66, 67, 80, 81, 82 -> Icons.Filled.Umbrella
        71, 73, 75, 77, 85, 86 -> Icons.Filled.AcUnit
        95, 96, 99 -> Icons.Filled.Thunderstorm
        else -> Icons.Filled.HelpOutline
    }
}
