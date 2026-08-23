package com.fossyfriend.fossyweather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AirQualityResponse(
    val current: AirQualityCurrent? = null
)

@Serializable
data class AirQualityCurrent(
    @SerialName("us_aqi") val usAqi: Int? = null,
    @SerialName("pm2_5") val pm25: Double? = null,
    @SerialName("pm10") val pm10: Double? = null
)
