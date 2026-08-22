package com.fossyfriend.fossyweather

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            viewModel.loadWeatherFromDeviceLocation()
        } else if (permissionsState.permissions.any { it.status.shouldShowRationale }) {
            viewModel.onPermissionDenied()
        } else {
            // First launch: proactively ask.
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    FossyWeatherNavGraph(
        viewModel = viewModel,
        onRequestLocationPermission = {
            if (permissionsState.allPermissionsGranted) {
                viewModel.loadWeatherFromDeviceLocation()
            } else {
                permissionsState.launchMultiplePermissionRequest()
            }
        }
    )
}
