package com.fossyfriend.fossyweather.data.remote

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val weatherApi: OpenMeteoApi by lazy {
        retrofit(OpenMeteoApi.BASE_URL).create(OpenMeteoApi::class.java)
    }

    val marineApi: OpenMeteoApi by lazy {
        retrofit(OpenMeteoApi.MARINE_BASE_URL).create(OpenMeteoApi::class.java)
    }

    val geocodingApi: GeocodingApi by lazy {
        retrofit(OpenMeteoApi.GEOCODING_BASE_URL).create(GeocodingApi::class.java)
    }
}
