package com.fossyfriend.fossyweather.ui.nav

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fossyfriend.fossyweather.domain.MetricType
import com.fossyfriend.fossyweather.domain.WeatherBundle
import com.fossyfriend.fossyweather.ui.components.DetailRow
import com.fossyfriend.fossyweather.ui.screens.DetailScreen
import com.fossyfriend.fossyweather.ui.screens.HomeScreen
import com.fossyfriend.fossyweather.ui.screens.MapScreen
import com.fossyfriend.fossyweather.ui.screens.MetricDetailScreen
import com.fossyfriend.fossyweather.ui.screens.MoonDetailScreen
import com.fossyfriend.fossyweather.ui.screens.PressureDetailScreen
import com.fossyfriend.fossyweather.ui.screens.SettingsScreen
import com.fossyfriend.fossyweather.ui.screens.TideDetailScreen
import com.fossyfriend.fossyweather.util.formatTemp
import com.fossyfriend.fossyweather.util.isoDateToFullLabel
import com.fossyfriend.fossyweather.util.isoToHourLabel
import com.fossyfriend.fossyweather.util.isoToTimeLabel
import com.fossyfriend.fossyweather.viewmodel.UiState
import com.fossyfriend.fossyweather.viewmodel.WeatherViewModel
import kotlin.math.roundToInt

private object Routes {
    const val HOME = "home"
    const val MAP = "map"
    const val SETTINGS = "settings"
    const val PRESSURE = "pressure"
    const val TIDE = "tide"
    const val MOON = "moon"
    const val METRIC = "metric/{metricName}"
    const val DETAIL_CURRENT = "detail_current"
    const val DETAIL_HOURLY = "detail_hourly/{hourIndex}"
    const val DETAIL_DAILY = "detail_daily/{dayIndex}"
    const val HERO_OVERLAY = "hero_overlay"
}

private const val ANIM_DURATION = 500
private val enterAnim = fadeIn(tween(ANIM_DURATION, easing = EaseOutQuart)) + 
    slideInHorizontally(tween(ANIM_DURATION, easing = EaseOutQuart)) { it / 4 } +
    scaleIn(tween(ANIM_DURATION, easing = EaseOutQuart), initialScale = 0.95f)

private val exitAnim = fadeOut(tween(ANIM_DURATION, easing = EaseOutQuart)) + 
    slideOutHorizontally(tween(ANIM_DURATION, easing = EaseOutQuart)) { -it / 4 } +
    scaleOut(tween(ANIM_DURATION, easing = EaseOutQuart), targetScale = 1.05f)

private val popEnterAnim = fadeIn(tween(ANIM_DURATION, easing = EaseOutQuart)) + 
    slideInHorizontally(tween(ANIM_DURATION, easing = EaseOutQuart)) { -it / 4 } +
    scaleIn(tween(ANIM_DURATION, easing = EaseOutQuart), initialScale = 1.05f)

private val popExitAnim = fadeOut(tween(ANIM_DURATION, easing = EaseOutQuart)) + 
    slideOutHorizontally(tween(ANIM_DURATION, easing = EaseOutQuart)) { it / 4 } +
    scaleOut(tween(ANIM_DURATION, easing = EaseOutQuart), targetScale = 0.95f)

@Composable
fun FossyWeatherNavGraph(
    viewModel: WeatherViewModel,
    onRequestLocationPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    val navController: NavHostController = rememberNavController()
    var lastBundle by remember { mutableStateOf<WeatherBundle?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        val state = uiState
        if (state is UiState.Success) {
            lastBundle = state.bundle
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { enterAnim },
        exitTransition = { exitAnim },
        popEnterTransition = { popEnterAnim },
        popExitTransition = { popExitAnim }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onOpenMap = { bundle ->
                    lastBundle = bundle
                    navController.navigate(Routes.MAP)
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenMetric = { metric ->
                    navController.navigate("metric/${metric.name}")
                },
                onOpenPressure = { navController.navigate(Routes.PRESSURE) },
                onOpenTide = { navController.navigate(Routes.TIDE) },
                onOpenMoon = { navController.navigate(Routes.MOON) },
                onOpenCurrentDetail = { navController.navigate(Routes.DETAIL_CURRENT) },
                onOpenHourDetail = { index -> navController.navigate("detail_hourly/$index") },
                onOpenDayDetail = { index -> navController.navigate("detail_daily/$index") },
                onOpenHeroOverlay = { navController.navigate(Routes.HERO_OVERLAY) },
                onRequestLocationPermission = onRequestLocationPermission
            )
        }
        composable(Routes.HERO_OVERLAY) {
            val bundle = lastBundle
            val uiStateVal = uiState
            if (bundle != null && uiStateVal is UiState.Success) {
                com.fossyfriend.fossyweather.ui.screens.HeroWeatherOverlay(
                    bundle = bundle,
                    tempUnit = uiStateVal.tempUnit,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.DETAIL_CURRENT) {
            val bundle = lastBundle
            val uiStateVal = uiState
            if (bundle != null && uiStateVal is UiState.Success) {
                DetailScreen(
                    title = "Right now in ${bundle.locationName}",
                    subtitle = com.fossyfriend.fossyweather.domain.WeatherCode.description(bundle.current.weatherCode),
                    rows = listOf(
                        DetailRow("Temperature", formatTemp(bundle.current.temperature, uiStateVal.tempUnit)),
                        DetailRow("Feels like", formatTemp(bundle.current.apparentTemperature, uiStateVal.tempUnit)),
                        DetailRow("Humidity", "${bundle.current.humidity}%"),
                        DetailRow("Wind", "${bundle.current.windSpeed.roundToInt()} km/h"),
                        DetailRow("Gusts", "${bundle.current.windGusts.roundToInt()} km/h"),
                        DetailRow("Pressure", "${bundle.current.pressureMsl.roundToInt()} hPa"),
                        DetailRow("UV index", "${bundle.current.uvIndex.roundToInt()}"),
                        DetailRow("Cloud cover", "${bundle.current.cloudCover}%")
                    ),
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            Routes.DETAIL_HOURLY,
            arguments = listOf(navArgument("hourIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val bundle = lastBundle
            val uiStateVal = uiState
            val index = backStackEntry.arguments?.getInt("hourIndex") ?: 0
            val hour = bundle?.hourly?.getOrNull(index)
            if (bundle != null && hour != null && uiStateVal is UiState.Success) {
                DetailScreen(
                    title = isoToHourLabel(hour.isoTime),
                    subtitle = com.fossyfriend.fossyweather.domain.WeatherCode.description(hour.weatherCode),
                    rows = listOf(
                        DetailRow("Temperature", formatTemp(hour.temperature, uiStateVal.tempUnit)),
                        DetailRow("Chance of rain", "${hour.precipitationProbability}%"),
                        DetailRow("Humidity", "${hour.humidity}%"),
                        DetailRow("Wind speed", "${hour.windSpeed.roundToInt()} km/h"),
                        DetailRow("Pressure", "${hour.pressureMsl.roundToInt()} hPa"),
                        DetailRow("UV index", "${hour.uvIndex.roundToInt()}"),
                        DetailRow("Cloud cover", "${hour.cloudCover}%"),
                        DetailRow("Visibility", "${(hour.visibilityMeters / 1000).roundToInt()} km")
                    ),
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            Routes.DETAIL_DAILY,
            arguments = listOf(navArgument("dayIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val bundle = lastBundle
            val uiStateVal = uiState
            val index = backStackEntry.arguments?.getInt("dayIndex") ?: 0
            val day = bundle?.daily?.getOrNull(index)
            if (bundle != null && day != null && uiStateVal is UiState.Success) {
                DetailScreen(
                    title = isoDateToFullLabel(day.isoDate),
                    subtitle = com.fossyfriend.fossyweather.domain.WeatherCode.description(day.weatherCode),
                    rows = listOf(
                        DetailRow("High", formatTemp(day.tempMax, uiStateVal.tempUnit)),
                        DetailRow("Low", formatTemp(day.tempMin, uiStateVal.tempUnit)),
                        DetailRow("Chance of rain", "${day.precipitationProbabilityMax}%"),
                        DetailRow("Precipitation", "${"%.1f".format(day.precipitationSum)} mm"),
                        DetailRow("Max wind", "${day.windSpeedMax.roundToInt()} km/h"),
                        DetailRow("Max UV index", "${day.uvIndexMax.roundToInt()}"),
                        DetailRow("S☀️nrise", isoToTimeLabel(day.sunrise)),
                        DetailRow("S☀️nset", isoToTimeLabel(day.sunset))
                    ),
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.MAP) {
            val bundle = lastBundle
            if (bundle != null) {
                MapScreen(
                    latitude = bundle.latitude,
                    longitude = bundle.longitude,
                    locationName = bundle.locationName,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRequestNotificationPermission = onRequestNotificationPermission
            )
        }
        composable(Routes.PRESSURE) {
            val bundle = lastBundle
            if (bundle != null) {
                PressureDetailScreen(
                    currentPressure = bundle.current.pressureMsl,
                    hourly = bundle.hourly,
                    daily = bundle.daily,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.TIDE) {
            val bundle = lastBundle
            if (bundle != null) {
                TideDetailScreen(
                    marine = bundle.marine,
                    isCoastal = bundle.isCoastal,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.MOON) {
            MoonDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Routes.METRIC,
            arguments = listOf(navArgument("metricName") { type = NavType.StringType })
        ) { backStackEntry ->
            val bundle = lastBundle
            val metricName = backStackEntry.arguments?.getString("metricName")
            val metric = metricName?.let { runCatching { MetricType.valueOf(it) }.getOrNull() }
            if (bundle != null && metric != null) {
                MetricDetailScreen(
                    metric = metric,
                    hourly = bundle.hourly,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
