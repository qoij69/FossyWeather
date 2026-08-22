package com.fossyfriend.fossyweather.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.floor

/**
 * Pure, offline astronomical moon-phase calculation.
 * No network call needed — uses the synodic month constant relative to a known new moon.
 */
data class MoonPhaseInfo(
    val phaseName: String,
    val illuminationPercent: Int,
    val ageDays: Double,
    val nextNewMoon: LocalDate,
    val nextFullMoon: LocalDate
)

object MoonPhaseCalculator {

    private const val SYNODIC_MONTH = 29.530588853
    // A known new moon reference: 2000-01-06 18:14 UTC
    private val REFERENCE_NEW_MOON = LocalDateTime.of(2000, 1, 6, 18, 14).toEpochSecond(ZoneOffset.UTC)

    fun calculate(date: LocalDate = LocalDate.now()): MoonPhaseInfo {
        val noonEpoch = date.atTime(12, 0).toEpochSecond(ZoneOffset.UTC)
        val daysSinceRef = (noonEpoch - REFERENCE_NEW_MOON) / 86400.0
        var age = daysSinceRef % SYNODIC_MONTH
        if (age < 0) age += SYNODIC_MONTH

        val phaseFraction = age / SYNODIC_MONTH
        val illumination = ((1 - kotlin.math.cos(2 * Math.PI * phaseFraction)) / 2 * 100).toInt()

        val name = when {
            age < 1.84566 -> "New Moon"
            age < 5.53699 -> "Waxing Crescent"
            age < 9.22831 -> "First Quarter"
            age < 12.91963 -> "Waxing Gibbous"
            age < 16.61096 -> "Full Moon"
            age < 20.30228 -> "Waning Gibbous"
            age < 23.99361 -> "Last Quarter"
            age < 27.68493 -> "Waning Crescent"
            else -> "New Moon"
        }

        val daysUntilNewMoon = SYNODIC_MONTH - age
        val nextNewMoon = date.plusDays(floor(daysUntilNewMoon).toLong() + 1)

        val daysUntilFull = if (age < 14.765294) {
            14.765294 - age
        } else {
            SYNODIC_MONTH - age + 14.765294
        }
        val nextFullMoon = date.plusDays(floor(daysUntilFull).toLong() + 1)

        return MoonPhaseInfo(
            phaseName = name,
            illuminationPercent = illumination,
            ageDays = age,
            nextNewMoon = nextNewMoon,
            nextFullMoon = nextFullMoon
        )
    }
}
