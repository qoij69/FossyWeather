package com.fossyfriend.fossyweather.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ShareUtils {
    fun shareApk(context: Context) {
        try {
            // Get the APK path from ApplicationInfo
            val apkPath = context.applicationInfo.publicSourceDir
            val sourceFile = File(apkPath)
            
            // Create a dedicated directory in cache for sharing
            val shareDir = File(context.cacheDir, "share")
            if (!shareDir.exists()) shareDir.mkdirs()
            
            val destFile = File(shareDir, "FossyWeather.apk")
            
            // Copy APK to cache
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Add text as well
                putExtra(Intent.EXTRA_TEXT, "Check out FossyWeather! Made with love by @qoij 🌦️")
            }
            
            context.startActivity(Intent.createChooser(intent, "Share FossyWeather APK"))
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to text share
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check out FossyWeather - a beautiful Material 3 weather app! 🌦️")
            }
            context.startActivity(Intent.createChooser(textIntent, "Share FossyWeather"))
        }
    }
}
