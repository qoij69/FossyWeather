package com.fossyfriend.fossyweather.domain

import kotlinx.serialization.Serializable

data class CurrentWeather(
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val isDay: Boolean,
    val weatherCode: Int,
    val cloudCover: Int,
    val pressureMsl: Double,
    val surfacePressure: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val windGusts: Double,
    val uvIndex: Double,
    val visibilityMeters: Double
)

data class HourForecast(
    val isoTime: String,
    val temperature: Double,
    val precipitationProbability: Int,
    val weatherCode: Int,
    val pressureMsl: Double,
    val uvIndex: Double,
    val humidity: Int,
    val windSpeed: Double,
    val cloudCover: Int,
    val visibilityMeters: Double
)

data class DayForecast(
    val isoDate: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double,
    val sunrise: String,
    val sunset: String,
    val uvIndexMax: Double,
    val precipitationProbabilityMax: Int,
    val windSpeedMax: Double,
    val precipitationSum: Double
)

data class MarinePoint(
    val isoTime: String,
    val waveHeight: Double?,
    val wavePeriod: Double?,
    val seaSurfaceTemp: Double?,
    val seaLevelHeight: Double?
)

data class WeatherBundle(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather,
    val hourly: List<HourForecast>,
    val daily: List<DayForecast>,
    val marine: List<MarinePoint>,
    val isCoastal: Boolean
)

@Serializable
data class PlaceResult(
    val id: Long,
    val name: String,
    val admin1: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String
        get() = listOfNotNull(name, admin1, country).joinToString(", ")
}
