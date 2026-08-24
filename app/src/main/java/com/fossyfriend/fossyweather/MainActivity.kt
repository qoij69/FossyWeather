package com.fossyfriend.fossyweather

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.fossyfriend.fossyweather.data.prefs.ThemeMode
import com.fossyfriend.fossyweather.ui.nav.FossyWeatherNavGraph
import com.fossyfriend.fossyweather.ui.theme.FossyWeatherTheme
import com.fossyfriend.fossyweather.util.NotificationHelper
import com.fossyfriend.fossyweather.viewmodel.WeatherViewModel
import com.fossyfriend.fossyweather.widget.CurrentWeatherWidget
import com.fossyfriend.fossyweather.widget.ForecastWidget
import androidx.glance.appwidget.updateAll
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)

        lifecycleScope.launch {
            CurrentWeatherWidget().updateAll(this@MainActivity)
            ForecastWidget().updateAll(this@MainActivity)
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.useDynamicColor.collectAsState()
            val darkOverride = when (themeMode) {
                ThemeMode.SYSTEM -> null
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            FossyWeatherTheme(darkThemeOverride = darkOverride, dynamicColor = dynamicColor) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FossyWeatherRoot(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun FossyWeatherRoot(viewModel: WeatherViewModel) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val notifPermissionState = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberMultiplePermissionsState(permissions = listOf(Manifest.permission.POST_NOTIFICATIONS))
    } else null

    val notifRefusalCount by viewModel.notifRefusalCount.collectAsState()
    val hasShownWelcome by viewModel.hasShownWelcomeNotification.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val currentVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
        } catch (e: Exception) {
            "0.0.0"
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkForUpdates(currentVersion)
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.loadWeatherFromDeviceLocation()
            
            if (!hasShownWelcome) {
                NotificationHelper.showTestNotification(context)
                viewModel.markWelcomeNotificationShown()
            }
        } else if (locationPermissionsState.permissions.any { it.status.shouldShowRationale }) {
            viewModel.onPermissionDenied()
        } else {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(notifPermissionState?.allPermissionsGranted) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (notifPermissionState != null && !notifPermissionState.allPermissionsGranted && notifRefusalCount < 2) {
                notifPermissionState.launchMultiplePermissionRequest()
                
                // Note: we can't accurately detect "denied" immediately after launchMultiplePermissionRequest
                // because it's asynchronous. The next time the app starts, the effect will run again
                // if it's still not granted.
            }
        }
    }

    // Helper to increment refusal if it was just denied
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val showRationale = notifPermissionState?.permissions?.any { it.status.shouldShowRationale } == true
        DisposableEffect(showRationale) {
            if (showRationale) {
                viewModel.incrementNotifRefusalCount()
            }
            onDispose {}
        }
    }

    FossyWeatherNavGraph(
        viewModel = viewModel,
        onRequestLocationPermission = {
            if (locationPermissionsState.allPermissionsGranted) {
                viewModel.loadWeatherFromDeviceLocation()
            } else {
                locationPermissionsState.launchMultiplePermissionRequest()
            }
        },
        onRequestNotificationPermission = {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notifPermissionState?.launchMultiplePermissionRequest()
            }
        }
    )
}
