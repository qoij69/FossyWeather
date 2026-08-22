package com.fossyfriend.fossyweather.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.fossyfriend.fossyweather.domain.PlaceResult

private val Context.dataStore by preferencesDataStore(name = "foss_weather_prefs")

enum class TempUnit { CELSIUS, FAHRENHEIT }
enum class WindUnit(val label: String) { KMH("km/h"), MPH("mph"), MS("m/s") }
enum class PressureUnit(val label: String) { HPA("hPa"), INHG("inHg") }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class RefreshInterval(val minutes: Int, val label: String) {
    MANUAL(0, "Manual only"),
    FIFTEEN(15, "Every 15 min"),
    THIRTY(30, "Every 30 min"),
    SIXTY(60, "Every hour")
}

class UserPreferences(private val context: Context) {

    private object Keys {
        val TEMP_UNIT = stringPreferencesKey("temp_unit")
        val WIND_UNIT = stringPreferencesKey("wind_unit")
        val PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val REFRESH_INTERVAL = stringPreferencesKey("refresh_interval")
        val SHOW_TIDE_CARD = booleanPreferencesKey("show_tide_card")
        val SHOW_MOON_CARD = booleanPreferencesKey("show_moon_card")
        val SHOW_RADAR_BY_DEFAULT = booleanPreferencesKey("show_radar_default")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LAST_LAT = stringPreferencesKey("last_lat")
        val LAST_LON = stringPreferencesKey("last_lon")
        val LAST_NAME = stringPreferencesKey("last_name")
        val SAVED_LOCATIONS = stringPreferencesKey("saved_locations")

        // Widget settings
        val WIDGET_SHOW_LOCATION = booleanPreferencesKey("widget_show_location")
        val WIDGET_SHOW_HUMIDITY = booleanPreferencesKey("widget_show_humidity")
        val WIDGET_SHOW_WIND = booleanPreferencesKey("widget_show_wind")
        val WIDGET_SHOW_DESCRIPTION = booleanPreferencesKey("widget_show_description")
        val WIDGET_SHOW_UV = booleanPreferencesKey("widget_show_uv")
        val WIDGET_SHOW_VISIBILITY = booleanPreferencesKey("widget_show_visibility")
        val WIDGET_SHOW_PRESSURE = booleanPreferencesKey("widget_show_pressure")
        val WIDGET_FORECAST_DAYS = stringPreferencesKey("widget_forecast_days")
    }

    val tempUnit: Flow<TempUnit> = context.dataStore.data.map {
        if (it[Keys.TEMP_UNIT] == "F") TempUnit.FAHRENHEIT else TempUnit.CELSIUS
    }

    val windUnit: Flow<WindUnit> = context.dataStore.data.map {
        WindUnit.entries.find { u -> u.name == it[Keys.WIND_UNIT] } ?: WindUnit.KMH
    }

    val pressureUnit: Flow<PressureUnit> = context.dataStore.data.map {
        PressureUnit.entries.find { u -> u.name == it[Keys.PRESSURE_UNIT] } ?: PressureUnit.HPA
    }

    val useDynamicColor: Flow<Boolean> = context.dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: true }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        ThemeMode.entries.find { m -> m.name == it[Keys.THEME_MODE] } ?: ThemeMode.SYSTEM
    }

    val refreshInterval: Flow<RefreshInterval> = context.dataStore.data.map {
        RefreshInterval.entries.find { r -> r.name == it[Keys.REFRESH_INTERVAL] } ?: RefreshInterval.MANUAL
    }

    val showTideCard: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHOW_TIDE_CARD] ?: true }
    val showMoonCard: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHOW_MOON_CARD] ?: true }
    val showRadarByDefault: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHOW_RADAR_BY_DEFAULT] ?: true }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: false }

    val lastLocation: Flow<Triple<Double, Double, String>?> = context.dataStore.data.map { p ->
        val lat = p[Keys.LAST_LAT]?.toDoubleOrNull()
        val lon = p[Keys.LAST_LON]?.toDoubleOrNull()
        val name = p[Keys.LAST_NAME]
        if (lat != null && lon != null && name != null) Triple(lat, lon, name) else null
    }

    val savedLocations: Flow<List<PlaceResult>> = context.dataStore.data.map { p ->
        val json = p[Keys.SAVED_LOCATIONS]
        if (json != null) {
            runCatching { Json.decodeFromString<List<PlaceResult>>(json) }.getOrDefault(emptyList())
        } else emptyList()
    }

    val widgetShowLocation: Flow<Boolean> = context.dataStore.data.map { it[Keys.WIDGET_SHOW_LOCATION] ?: true }
    val widgetShowHumidity: Flow<Boolean> = context.dataStore.data.map { it[Keys.WIDGET_SHOW_HUMIDITY] ?: false }
    val widgetShowWind: Flow<Boolean> = context.dataStore.data.map { it[Keys.WIDGET_SHOW_WIND] ?: false }
    val widgetShowDescription: Flow<Boolean> = context.dataStore.data.map { it[Keys.WIDGET_SHOW_DESCRIPTION] ?: true }
    val widgetShowUv: Flow<Boolean> = context.dataStore.data.map { it[Keys.WIDGET_SHOW_UV] ?: false }
    val widgetShowVisibility: Flow<Boolean> = context.dataStore.data.map { it[Keys.WIDGET_SHOW_VISIBILITY] ?: false }
    val widgetShowPressure: Flow<Boolean> = context.dataStore.data.map { it[Keys.WIDGET_SHOW_PRESSURE] ?: false }
    val widgetForecastDays: Flow<Int> = context.dataStore.data.map { it[Keys.WIDGET_FORECAST_DAYS]?.toIntOrNull() ?: 3 }

    suspend fun setTempUnit(unit: TempUnit) {
        context.dataStore.edit { it[Keys.TEMP_UNIT] = if (unit == TempUnit.FAHRENHEIT) "F" else "C" }
    }

    suspend fun setWindUnit(unit: WindUnit) {
        context.dataStore.edit { it[Keys.WIND_UNIT] = unit.name }
    }

    suspend fun setPressureUnit(unit: PressureUnit) {
        context.dataStore.edit { it[Keys.PRESSURE_UNIT] = unit.name }
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setRefreshInterval(interval: RefreshInterval) {
        context.dataStore.edit { it[Keys.REFRESH_INTERVAL] = interval.name }
    }

    suspend fun setShowTideCard(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_TIDE_CARD] = enabled }
    }

    suspend fun setShowMoonCard(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_MOON_CARD] = enabled }
    }

    suspend fun setShowRadarByDefault(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_RADAR_BY_DEFAULT] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun saveLastLocation(lat: Double, lon: Double, name: String) {
        context.dataStore.edit {
            it[Keys.LAST_LAT] = lat.toString()
            it[Keys.LAST_LON] = lon.toString()
            it[Keys.LAST_NAME] = name
        }
    }

    suspend fun saveLocation(place: PlaceResult) {
        context.dataStore.edit { p ->
            val current = p[Keys.SAVED_LOCATIONS]?.let {
                runCatching { Json.decodeFromString<List<PlaceResult>>(it) }.getOrNull()
            } ?: emptyList()
            if (current.none { it.id == place.id }) {
                val updated = current + place
                p[Keys.SAVED_LOCATIONS] = Json.encodeToString(updated)
            }
        }
    }

    suspend fun removeLocation(id: Long) {
        context.dataStore.edit { p ->
            val current = p[Keys.SAVED_LOCATIONS]?.let {
                runCatching { Json.decodeFromString<List<PlaceResult>>(it) }.getOrNull()
            } ?: emptyList()
            val updated = current.filterNot { it.id == id }
            p[Keys.SAVED_LOCATIONS] = Json.encodeToString(updated)
        }
    }

    suspend fun setWidgetShowLocation(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIDGET_SHOW_LOCATION] = enabled }
    }

    suspend fun setWidgetShowHumidity(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIDGET_SHOW_HUMIDITY] = enabled }
    }

    suspend fun setWidgetShowWind(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIDGET_SHOW_WIND] = enabled }
    }

    suspend fun setWidgetShowDescription(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIDGET_SHOW_DESCRIPTION] = enabled }
    }

    suspend fun setWidgetShowUv(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIDGET_SHOW_UV] = enabled }
    }

    suspend fun setWidgetShowVisibility(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIDGET_SHOW_VISIBILITY] = enabled }
    }

    suspend fun setWidgetShowPressure(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIDGET_SHOW_PRESSURE] = enabled }
    }

    suspend fun setWidgetForecastDays(days: Int) {
        context.dataStore.edit { it[Keys.WIDGET_FORECAST_DAYS] = days.toString() }
    }
}
