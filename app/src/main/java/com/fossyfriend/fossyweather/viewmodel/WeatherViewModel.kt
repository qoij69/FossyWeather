package com.fossyfriend.fossyweather.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fossyfriend.fossyweather.data.location.LocationProvider
import com.fossyfriend.fossyweather.data.prefs.PressureUnit
import com.fossyfriend.fossyweather.data.prefs.RefreshInterval
import com.fossyfriend.fossyweather.data.prefs.TempUnit
import com.fossyfriend.fossyweather.data.prefs.ThemeMode
import com.fossyfriend.fossyweather.data.prefs.UserPreferences
import com.fossyfriend.fossyweather.data.prefs.WindUnit
import com.fossyfriend.fossyweather.data.repository.WeatherRepository
import com.fossyfriend.fossyweather.domain.MoonPhaseCalculator
import com.fossyfriend.fossyweather.domain.MoonPhaseInfo
import com.fossyfriend.fossyweather.domain.PlaceResult
import com.fossyfriend.fossyweather.domain.WeatherBundle
import com.fossyfriend.fossyweather.widget.CurrentWeatherWidget
import com.fossyfriend.fossyweather.widget.ForecastWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed interface UiState {
    data object Loading : UiState
    data object NeedsPermission : UiState
    data class Error(val message: String) : UiState
    data class Success(
        val bundle: WeatherBundle,
        val moonPhase: MoonPhaseInfo,
        val tempUnit: TempUnit,
        val lastUpdated: Long = System.currentTimeMillis()
    ) : UiState
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository()
    private val locationProvider = LocationProvider(application)
    val prefs = UserPreferences(application)

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<PlaceResult>>(emptyList())
    val searchResults: StateFlow<List<PlaceResult>> = _searchResults.asStateFlow()

    val tempUnit: StateFlow<TempUnit> = prefs.tempUnit.stateIn(viewModelScope, SharingStarted.Eagerly, TempUnit.CELSIUS)
    val windUnit: StateFlow<WindUnit> = prefs.windUnit.stateIn(viewModelScope, SharingStarted.Eagerly, WindUnit.KMH)
    val pressureUnit: StateFlow<PressureUnit> = prefs.pressureUnit.stateIn(viewModelScope, SharingStarted.Eagerly, PressureUnit.HPA)
    val useDynamicColor: StateFlow<Boolean> = prefs.useDynamicColor.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val themeMode: StateFlow<ThemeMode> = prefs.themeMode.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
    val refreshInterval: StateFlow<RefreshInterval> = prefs.refreshInterval.stateIn(viewModelScope, SharingStarted.Eagerly, RefreshInterval.MANUAL)
    val showTideCard: StateFlow<Boolean> = prefs.showTideCard.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val showMoonCard: StateFlow<Boolean> = prefs.showMoonCard.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val showRadarByDefault: StateFlow<Boolean> = prefs.showRadarByDefault.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val notificationsEnabled: StateFlow<Boolean> = prefs.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Saved locations
    val savedLocations: StateFlow<List<PlaceResult>> = prefs.savedLocations.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val _selectedLocation = MutableStateFlow<PlaceResult?>(null)
    val selectedLocation: StateFlow<PlaceResult?> = _selectedLocation.asStateFlow()

    // Widget settings
    val widgetShowLocation: StateFlow<Boolean> = prefs.widgetShowLocation.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val widgetShowHumidity: StateFlow<Boolean> = prefs.widgetShowHumidity.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val widgetShowWind: StateFlow<Boolean> = prefs.widgetShowWind.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val widgetShowDescription: StateFlow<Boolean> = prefs.widgetShowDescription.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val widgetShowUv: StateFlow<Boolean> = prefs.widgetShowUv.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val widgetShowVisibility: StateFlow<Boolean> = prefs.widgetShowVisibility.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val widgetShowPressure: StateFlow<Boolean> = prefs.widgetShowPressure.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val widgetForecastDays: StateFlow<Int> = prefs.widgetForecastDays.stateIn(viewModelScope, SharingStarted.Eagerly, 3)

    fun loadWeatherFromDeviceLocation() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val location = withTimeoutOrNull(12_000) {
                    locationProvider.getLastKnownOrCurrentLocation()
                }
                if (location == null) {
                    // Fall back to last saved location if we truly can't get a GPS/network fix.
                    val last = runCatching { prefs.lastLocation.first() }.getOrNull()
                    if (last != null) {
                        loadWeather(last.first, last.second, last.third)
                    } else {
                        _uiState.value = UiState.Error("Couldn't determine your location. Check GPS/network location is enabled, or search for a city instead.")
                    }
                    return@launch
                }
                loadWeather(location.latitude, location.longitude, "My Location")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to fetch location")
            }
        }
    }

    fun loadWeather(latitude: Double, longitude: Double, name: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val bundle = repository.getWeatherBundle(latitude, longitude, name)
                val moon = MoonPhaseCalculator.calculate()
                prefs.saveLastLocation(latitude, longitude, name)
                _uiState.value = UiState.Success(bundle, moon, tempUnit.value, System.currentTimeMillis())
                
                // Update selected location if it matches one of our saved ones (or create temp)
                val saved = prefs.savedLocations.first()
                _selectedLocation.value = saved.find { it.latitude == latitude && it.longitude == longitude }
                    ?: PlaceResult(0, name, null, null, latitude, longitude)

                // Trigger widget updates
                viewModelScope.launch {
                    CurrentWeatherWidget().updateAll(getApplication())
                    ForecastWidget().updateAll(getApplication())
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load weather data")
            }
        }
    }

    fun saveLocation(place: PlaceResult) = viewModelScope.launch {
        prefs.saveLocation(place)
    }

    fun removeLocation(id: Long) = viewModelScope.launch {
        prefs.removeLocation(id)
    }

    fun onPermissionDenied() {
        _uiState.value = UiState.NeedsPermission
    }

    fun searchPlaces(query: String) {
        viewModelScope.launch {
            _searchResults.value = runCatching { repository.searchPlaces(query) }.getOrDefault(emptyList())
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun setTempUnit(unit: TempUnit) {
        viewModelScope.launch {
            prefs.setTempUnit(unit)
            val current = _uiState.value
            if (current is UiState.Success) {
                _uiState.value = current.copy(tempUnit = unit)
            }
        }
    }

    fun setWindUnit(unit: WindUnit) = viewModelScope.launch { prefs.setWindUnit(unit) }
    fun setPressureUnit(unit: PressureUnit) = viewModelScope.launch { prefs.setPressureUnit(unit) }
    fun setUseDynamicColor(enabled: Boolean) = viewModelScope.launch { prefs.setUseDynamicColor(enabled) }
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setRefreshInterval(interval: RefreshInterval) = viewModelScope.launch { prefs.setRefreshInterval(interval) }
    fun setShowTideCard(enabled: Boolean) = viewModelScope.launch { prefs.setShowTideCard(enabled) }
    fun setShowMoonCard(enabled: Boolean) = viewModelScope.launch { prefs.setShowMoonCard(enabled) }
    fun setShowRadarByDefault(enabled: Boolean) = viewModelScope.launch { prefs.setShowRadarByDefault(enabled) }
    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { prefs.setNotificationsEnabled(enabled) }

    fun setWidgetShowLocation(enabled: Boolean) = viewModelScope.launch { prefs.setWidgetShowLocation(enabled) }
    fun setWidgetShowHumidity(enabled: Boolean) = viewModelScope.launch { prefs.setWidgetShowHumidity(enabled) }
    fun setWidgetShowWind(enabled: Boolean) = viewModelScope.launch { prefs.setWidgetShowWind(enabled) }
    fun setWidgetShowDescription(enabled: Boolean) = viewModelScope.launch { prefs.setWidgetShowDescription(enabled) }
    fun setWidgetShowUv(enabled: Boolean) = viewModelScope.launch { prefs.setWidgetShowUv(enabled) }
    fun setWidgetShowVisibility(enabled: Boolean) = viewModelScope.launch { prefs.setWidgetShowVisibility(enabled) }
    fun setWidgetShowPressure(enabled: Boolean) = viewModelScope.launch { prefs.setWidgetShowPressure(enabled) }
    fun setWidgetForecastDays(days: Int) = viewModelScope.launch { prefs.setWidgetForecastDays(days) }
}
