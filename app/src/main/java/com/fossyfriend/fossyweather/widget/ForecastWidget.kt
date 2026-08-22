package com.fossyfriend.fossyweather.widget

import android.content.Context
import androidx.compose.ui.unit.dp
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.fossyfriend.fossyweather.MainActivity
import com.fossyfriend.fossyweather.data.prefs.UserPreferences
import com.fossyfriend.fossyweather.data.repository.WeatherRepository
import com.fossyfriend.fossyweather.util.isoDateToDayLabel
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

class ForecastWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = UserPreferences(context)
        val last = prefs.lastLocation.first()
        val forecastDays = prefs.widgetForecastDays.first()

        val bundle = last?.let { (lat, lon, name) ->
            runCatching { WeatherRepository().getWeatherBundle(lat, lon, name) }.getOrNull()
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (bundle != null) {
                    Text(
                        text = "Forecast: ${bundle.locationName}",
                        style = TextStyle(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        bundle.daily.take(forecastDays).forEach { day ->
                            Row(
                                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = isoDateToDayLabel(day.isoDate),
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                Text(
                                    text = "${day.tempMax.roundToInt()}° / ${day.tempMin.roundToInt()}°",
                                    style = TextStyle(fontWeight = FontWeight.Medium)
                                )
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

class ForecastWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ForecastWidget()
}
