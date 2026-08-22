package com.fossyfriend.fossyweather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponse(
    val results: List<GeoResult>? = null
)

@Serializable
data class GeoResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null,
    val timezone: String? = null
)
