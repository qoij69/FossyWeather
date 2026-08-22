package com.fossyfriend.fossyweather.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.fossyfriend.fossyweather.MainActivity
import com.fossyfriend.fossyweather.data.prefs.UserPreferences
import com.fossyfriend.fossyweather.data.repository.WeatherRepository
import com.fossyfriend.fossyweather.domain.WeatherCode
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

class CurrentWeatherWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = UserPreferences(context)
        val last = prefs.lastLocation.first()
        val showLocation = prefs.widgetShowLocation.first()
        val showHumidity = prefs.widgetShowHumidity.first()
        val showWind = prefs.widgetShowWind.first()
        val showDescription = prefs.widgetShowDescription.first()
        val showUv = prefs.widgetShowUv.first()
        val showVisibility = prefs.widgetShowVisibility.first()
        val showPressure = prefs.widgetShowPressure.first()

        val bundle = last?.let { (lat, lon, name) ->
            runCatching { WeatherRepository().getWeatherBundle(lat, lon, name) }.getOrNull()
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (bundle != null) {
                    if (showLocation) {
                        Text(
                            text = bundle.locationName,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                    Text(
                        text = "${bundle.current.temperature.roundToInt()}°",
                        style = TextStyle(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (showDescription) {
                        Text(
                            text = WeatherCode.description(bundle.current.weatherCode)
                        )
                    }
                    if (showHumidity || showWind || showUv || showVisibility || showPressure) {
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Column(
                            modifier = GlanceModifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (showHumidity) {
                                    Text(
                                        text = "H: ${bundle.current.humidity}%",
                                        style = TextStyle(fontSize = 12.sp)
                                    )
                                }
                                if (showHumidity && showWind) {
                                    Spacer(modifier = GlanceModifier.width(12.dp))
                                }
                                if (showWind) {
                                    Text(
                                        text = "W: ${bundle.current.windSpeed.roundToInt()} km/h",
                                        style = TextStyle(fontSize = 12.sp)
                                    )
                                }
                            }
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (showUv) {
                                    Text(
                                        text = "UV: ${bundle.current.uvIndex.roundToInt()}",
                                        style = TextStyle(fontSize = 12.sp)
                                    )
                                }
                                if (showUv && (showVisibility || showPressure)) {
                                    Spacer(modifier = GlanceModifier.width(12.dp))
                                }
                                if (showVisibility) {
                                    Text(
                                        text = "V: ${(bundle.current.visibilityMeters / 1000).roundToInt()}km",
                                        style = TextStyle(fontSize = 12.sp)
                                    )
                                }
                                if (showVisibility && showPressure) {
                                    Spacer(modifier = GlanceModifier.width(12.dp))
                                }
                                if (showPressure) {
                                    Text(
                                        text = "P: ${bundle.current.pressureMsl.roundToInt()}hPa",
                                        style = TextStyle(fontSize = 12.sp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(text = "Open FossyWeather to set a location")
                }
            }
        }
    }
}

class CurrentWeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CurrentWeatherWidget()
}
