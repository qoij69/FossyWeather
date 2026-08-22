package com.fossyfriend.fossyweather.domain

enum class PressureTrend(val label: String) {
    RISING("Rising"), FALLING("Falling"), STEADY("Steady")
}

object PressureAnalyzer {
    /** Simple trend & human read on sea-level pressure in hPa. */
    fun trend(first: Double, last: Double): PressureTrend {
        val delta = last - first
        return when {
            delta > 1.0 -> PressureTrend.RISING
            delta < -1.0 -> PressureTrend.FALLING
            else -> PressureTrend.STEADY
        }
    }

    fun condition(hpa: Double): String = when {
        hpa >= 1022 -> "High pressure — settled weather likely"
        hpa <= 1000 -> "Low pressure — unsettled weather likely"
        else -> "Normal pressure"
    }
}
