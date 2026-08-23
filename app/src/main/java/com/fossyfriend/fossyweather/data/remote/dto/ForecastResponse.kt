package com.fossyfriend.fossyweather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @SerialName("current") val current: CurrentBlock? = null,
    @SerialName("hourly") val hourly: HourlyBlock? = null,
    @SerialName("daily") val daily: DailyBlock? = null
)

@Serializable
data class CurrentBlock(
    val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("relative_humidity_2m") val humidity: Int,
    @SerialName("apparent_temperature") val apparentTemperature: Double,
    @SerialName("is_day") val isDay: Int,
    val precipitation: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("cloud_cover") val cloudCover: Int,
    @SerialName("pressure_msl") val pressureMsl: Double,
    @SerialName("surface_pressure") val surfacePressure: Double,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    @SerialName("wind_direction_10m") val windDirection: Int,
    @SerialName("wind_gusts_10m") val windGusts: Double,
    @SerialName("uv_index") val uvIndex: Double? = null,
    val visibility: Double? = null,
    @SerialName("dew_point_2m") val dewPoint: Double? = null
)

@Serializable
data class HourlyBlock(
    val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double> = emptyList(),
    @SerialName("precipitation_probability") val precipitationProbability: List<Int> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int> = emptyList(),
    @SerialName("pressure_msl") val pressureMsl: List<Double> = emptyList(),
    @SerialName("visibility") val visibility: List<Double> = emptyList(),
    @SerialName("uv_index") val uvIndex: List<Double> = emptyList(),
    @SerialName("relative_humidity_2m") val humidity: List<Int> = emptyList(),
    @SerialName("wind_speed_10m") val windSpeed: List<Double> = emptyList(),
    @SerialName("cloud_cover") val cloudCover: List<Int> = emptyList(),
    @SerialName("dew_point_2m") val dewPoint: List<Double> = emptyList()
)

@Serializable
data class DailyBlock(
    val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int> = emptyList(),
    @SerialName("temperature_2m_max") val tempMax: List<Double> = emptyList(),
    @SerialName("temperature_2m_min") val tempMin: List<Double> = emptyList(),
    val sunrise: List<String> = emptyList(),
    val sunset: List<String> = emptyList(),
    @SerialName("uv_index_max") val uvIndexMax: List<Double> = emptyList(),
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int> = emptyList(),
    @SerialName("wind_speed_10m_max") val windSpeedMax: List<Double> = emptyList(),
    @SerialName("precipitation_sum") val precipitationSum: List<Double> = emptyList()
)
