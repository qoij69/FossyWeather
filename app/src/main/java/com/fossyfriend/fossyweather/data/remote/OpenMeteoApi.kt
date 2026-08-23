package com.fossyfriend.fossyweather.data.remote

import com.fossyfriend.fossyweather.data.remote.dto.ForecastResponse
import com.fossyfriend.fossyweather.data.remote.dto.GeocodingResponse
import com.fossyfriend.fossyweather.data.remote.dto.MarineResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo (https://open-meteo.com) is a free, open-source-friendly weather API.
 * No API key is required for non-commercial use.
 */
interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("temperature_unit") temperatureUnit: String = "celsius",
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
        @Query("precipitation_unit") precipitationUnit: String = "mm",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 8
    ): ForecastResponse

    @GET("v1/marine")
    suspend fun getMarine(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = MARINE_FIELDS,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 3
    ): MarineResponse

    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "us_aqi,pm2_5,pm10",
        @Query("timezone") timezone: String = "auto"
    ): com.fossyfriend.fossyweather.data.remote.dto.AirQualityResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
        const val MARINE_BASE_URL = "https://marine-api.open-meteo.com/"
        const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
        const val AIR_QUALITY_BASE_URL = "https://air-quality-api.open-meteo.com/"

        private const val CURRENT_FIELDS = "temperature_2m,relative_humidity_2m,apparent_temperature," +
            "is_day,precipitation,weather_code,cloud_cover,pressure_msl,surface_pressure," +
            "wind_speed_10m,wind_direction_10m,wind_gusts_10m,uv_index,visibility,dew_point_2m"

        private const val HOURLY_FIELDS = "temperature_2m,precipitation_probability,weather_code," +
            "pressure_msl,visibility,uv_index,relative_humidity_2m,wind_speed_10m,cloud_cover,dew_point_2m"

        private const val DAILY_FIELDS = "weather_code,temperature_2m_max,temperature_2m_min," +
            "sunrise,sunset,uv_index_max,precipitation_probability_max,wind_speed_10m_max,precipitation_sum"

        private const val MARINE_FIELDS = "wave_height,wave_period,sea_surface_temperature,sea_level_height_msl"
    }
}

interface GeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}
