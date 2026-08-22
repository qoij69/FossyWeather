package com.fossyfriend.fossyweather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarineResponse(
    val latitude: Double,
    val longitude: Double,
    val hourly: MarineHourly? = null
)

@Serializable
data class MarineHourly(
    val time: List<String>,
    @SerialName("wave_height") val waveHeight: List<Double?> = emptyList(),
    @SerialName("wave_period") val wavePeriod: List<Double?> = emptyList(),
    @SerialName("sea_surface_temperature") val seaSurfaceTemp: List<Double?> = emptyList(),
    @SerialName("sea_level_height_msl") val seaLevelHeightMsl: List<Double?> = emptyList()
)
