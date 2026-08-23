package com.fossyfriend.fossyweather.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val FallbackLight = lightColorScheme(
    primary = Color(0xFF3D6EFF),
    secondary = Color(0xFF5B6BAE),
    tertiary = Color(0xFF7A5580)
)

private val FallbackDark = darkColorScheme(
    primary = Color(0xFFB0C6FF),
    secondary = Color(0xFFC2C9E8),
    tertiary = Color(0xFFE9B7DC)
)

@Composable
fun FossyWeatherTheme(
    darkThemeOverride: Boolean? = null,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = darkThemeOverride ?: systemDark
    val context = LocalContext.current

    val colorScheme = remember(darkTheme, dynamicColor) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> FallbackDark
            else -> FallbackLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FossyWeatherTypography,
        shapes = FossyWeatherShapes,
        content = content
    )
}
