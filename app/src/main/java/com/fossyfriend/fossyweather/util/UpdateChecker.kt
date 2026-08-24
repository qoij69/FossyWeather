package com.fossyfriend.fossyweather.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    @SerialName("html_url") val htmlUrl: String,
    val name: String,
    val body: String
)

interface GitHubApi {
    @GET("repos/qoij69/FossyWeather/releases/latest")
    suspend fun getLatestRelease(): GitHubRelease
}

object UpdateChecker {
    private val json = Json { ignoreUnknownKeys = true }
    
    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GitHubApi::class.java)
    }

    suspend fun checkForUpdates(currentVersion: String): GitHubRelease? = withContext(Dispatchers.IO) {
        try {
            val latest = api.getLatestRelease()
            val latestClean = latest.tagName.lowercase().replace("v", "").trim()
            val currentClean = currentVersion.lowercase().replace("v", "").trim()
            
            val isNewer = isNewerVersion(latestClean, currentClean)
            android.util.Log.d("UpdateChecker", "Latest: $latestClean, Current: $currentClean, New: $isNewer")
            
            if (isNewer) {
                latest
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        
        for (i in 0 until minOf(latestParts.size, currentParts.size)) {
            if (latestParts[i] > currentParts[i]) return true
            if (latestParts[i] < currentParts[i]) return false
        }
        return latestParts.size > currentParts.size
    }

    fun openReleasePage(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
