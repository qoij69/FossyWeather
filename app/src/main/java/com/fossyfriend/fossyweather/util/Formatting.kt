package com.fossyfriend.fossyweather.util

import com.fossyfriend.fossyweather.data.prefs.TempUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

fun formatTemp(celsius: Double, unit: TempUnit): String {
    val value = if (unit == TempUnit.FAHRENHEIT) celsius * 9 / 5 + 32 else celsius
    val symbol = if (unit == TempUnit.FAHRENHEIT) "°F" else "°C"
    return "${value.roundToInt()}$symbol"
}

fun formatTempShort(celsius: Double, unit: TempUnit): String {
    val value = if (unit == TempUnit.FAHRENHEIT) celsius * 9 / 5 + 32 else celsius
    return "${value.roundToInt()}°"
}

private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
private val hourFormatter = DateTimeFormatter.ofPattern("h a")
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
private val dayFormatter = DateTimeFormatter.ofPattern("EEE")
private val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

fun isoToHourLabel(iso: String): String = try {
    LocalDateTime.parse(iso, isoFormatter).format(hourFormatter)
} catch (e: DateTimeParseException) { iso }

fun isoToTimeLabel(iso: String): String = try {
    LocalDateTime.parse(iso, isoFormatter).format(timeFormatter)
} catch (e: DateTimeParseException) { iso }

fun isoDateToDayLabel(isoDate: String): String = try {
    java.time.LocalDate.parse(isoDate).format(dayFormatter).replace("Sun", "S☀️n")
} catch (e: Exception) { isoDate }

fun isoDateToFullLabel(isoDate: String): String = try {
    java.time.LocalDate.parse(isoDate).format(dateFormatter)
} catch (e: Exception) { isoDate }
