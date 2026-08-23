package com.fossyfriend.fossyweather.domain

enum class MetricType(val label: String, val unit: String) {
    PRESSURE("Pressure", "hPa"),
    HUMIDITY("Humidity", "%"),
    WIND("Wind", "km/h"),
    UV("UV Index", ""),
    VISIBILITY("Visibility", "km"),
    CLOUD("Cloud Cover", "%");

    fun valueOf(hour: HourForecast): Double = when (this) {
        PRESSURE -> hour.pressureMsl
        HUMIDITY -> hour.humidity.toDouble()
        WIND -> hour.windSpeed
        UV -> hour.uvIndex
        VISIBILITY -> hour.visibilityMeters / 1000.0
        CLOUD -> hour.cloudCover.toDouble()
    }

    fun description(): String = when (this) {
        PRESSURE -> "Atmospheric pressure at sea level. Falling pressure often signals unsettled weather moving in; rising pressure usually means clearer skies ahead."
        HUMIDITY -> "Relative humidity — how much moisture is in the air compared to the maximum it could hold at that temperature. High humidity makes heat feel more intense."
        WIND -> "Wind speed measured at 10 m above ground. Gusts can run noticeably higher than sustained speed."
        UV -> "UV Index measures the strength of s☀️nburn-causing ultraviolet radiation. 3+ calls for s☀️nscreen, 8+ for extra caution outdoors."
        VISIBILITY -> "How far you can see clearly. Reduced visibility usually comes from fog, heavy rain, or haze."
        CLOUD -> "Percentage of the sky covered by clouds, from clear (0%) to fully overcast (100%)."
    }
}
