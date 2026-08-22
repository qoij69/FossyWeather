package com.fossyfriend.fossyweather.data.repository

import com.fossyfriend.fossyweather.data.remote.NetworkModule
import com.fossyfriend.fossyweather.domain.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class WeatherRepository {

    private val weatherApi = NetworkModule.weatherApi
    private val marineApi = NetworkModule.marineApi
    private val geocodingApi = NetworkModule.geocodingApi

    suspend fun searchPlaces(query: String): List<PlaceResult> {
        if (query.isBlank()) return emptyList()
        val resp = geocodingApi.search(query)
        return resp.results.orEmpty().map {
            PlaceResult(
                id = it.id,
                name = it.name,
                admin1 = it.admin1,
                country = it.country,
                latitude = it.latitude,
                longitude = it.longitude
            )
        }
    }

    /**
     * Fetches forecast + marine (tide/wave) data concurrently.
     * Marine data gracefully degrades to an empty list for inland locations
     * (Open-Meteo's marine API simply returns nulls/empty there).
     */
    suspend fun getWeatherBundle(
        latitude: Double,
        longitude: Double,
        locationName: String
    ): WeatherBundle = coroutineScope {
        val forecastDeferred = async { weatherApi.getForecast(latitude, longitude) }
        val marineDeferred = async {
            runCatching { marineApi.getMarine(latitude, longitude) }.getOrNull()
        }

        val forecast = forecastDeferred.await()
        val marineResp = marineDeferred.await()

        val current = forecast.current!!
        val currentWeather = CurrentWeather(
            temperature = current.temperature,
            apparentTemperature = current.apparentTemperature,
            humidity = current.humidity,
            isDay = current.isDay == 1,
            weatherCode = current.weatherCode,
            cloudCover = current.cloudCover,
            pressureMsl = current.pressureMsl,
            surfacePressure = current.surfacePressure,
            windSpeed = current.windSpeed,
            windDirection = current.windDirection,
            windGusts = current.windGusts,
            uvIndex = current.uvIndex ?: 0.0,
            visibilityMeters = current.visibility ?: 0.0
        )

        val hourly = forecast.hourly?.let { h ->
            h.time.indices.map { i ->
                HourForecast(
                    isoTime = h.time[i],
                    temperature = h.temperature.getOrElse(i) { 0.0 },
                    precipitationProbability = h.precipitationProbability.getOrElse(i) { 0 },
                    weatherCode = h.weatherCode.getOrElse(i) { 0 },
                    pressureMsl = h.pressureMsl.getOrElse(i) { 0.0 },
                    uvIndex = h.uvIndex.getOrElse(i) { 0.0 },
                    humidity = h.humidity.getOrElse(i) { 0 },
                    windSpeed = h.windSpeed.getOrElse(i) { 0.0 },
                    cloudCover = h.cloudCover.getOrElse(i) { 0 },
                    visibilityMeters = h.visibility.getOrElse(i) { 0.0 }
                )
            }
        } ?: emptyList()

        val daily = forecast.daily?.let { d ->
            d.time.indices.map { i ->
                DayForecast(
                    isoDate = d.time[i],
                    weatherCode = d.weatherCode.getOrElse(i) { 0 },
                    tempMax = d.tempMax.getOrElse(i) { 0.0 },
                    tempMin = d.tempMin.getOrElse(i) { 0.0 },
                    sunrise = d.sunrise.getOrElse(i) { "" },
                    sunset = d.sunset.getOrElse(i) { "" },
                    uvIndexMax = d.uvIndexMax.getOrElse(i) { 0.0 },
                    precipitationProbabilityMax = d.precipitationProbabilityMax.getOrElse(i) { 0 },
                    windSpeedMax = d.windSpeedMax.getOrElse(i) { 0.0 },
                    precipitationSum = d.precipitationSum.getOrElse(i) { 0.0 }
                )
            }
        } ?: emptyList()

        val marine = marineResp?.hourly?.let { m ->
            m.time.indices.map { i ->
                MarinePoint(
                    isoTime = m.time[i],
                    waveHeight = m.waveHeight.getOrNull(i),
                    wavePeriod = m.wavePeriod.getOrNull(i),
                    seaSurfaceTemp = m.seaSurfaceTemp.getOrNull(i),
                    seaLevelHeight = m.seaLevelHeightMsl.getOrNull(i)
                )
            }
        } ?: emptyList()

        val isCoastal = marine.any { it.waveHeight != null }

        WeatherBundle(
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            timezone = forecast.timezone,
            current = currentWeather,
            hourly = hourly,
            daily = daily,
            marine = marine,
            isCoastal = isCoastal
        )
    }
}
