package com.fossyfriend.fossyweather.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.data.prefs.*
import com.fossyfriend.fossyweather.util.ShareUtils
import com.fossyfriend.fossyweather.viewmodel.WeatherViewModel

private data class SettingsTab(val title: String, val icon: ImageVector)
private val tabs = listOf(
    SettingsTab("General", Icons.Filled.Settings),
    SettingsTab("Units", Icons.Filled.Straighten),
    SettingsTab("Display", Icons.Filled.DisplaySettings),
    SettingsTab("Widgets", Icons.Filled.Widgets),
    SettingsTab("Notifications", Icons.Filled.Notifications),
    SettingsTab("About", Icons.Filled.Info)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: WeatherViewModel, onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(tab.title) },
                            icon = { Icon(tab.icon, contentDescription = null) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> GeneralTab(viewModel)
                1 -> UnitsTab(viewModel)
                2 -> DisplayTab(viewModel)
                3 -> WidgetsTab(viewModel)
                4 -> NotificationsTab(viewModel)
                5 -> AboutTab()
            }
        }
    }
}

@Composable
private fun GeneralTab(viewModel: WeatherViewModel) {
    val showTide by viewModel.showTideCard.collectAsState()
    val showMoon by viewModel.showMoonCard.collectAsState()
    val showRadar by viewModel.showRadarByDefault.collectAsState()
    val refreshInterval by viewModel.refreshInterval.collectAsState()

    SettingsList {
        item {
            SettingsSection(title = "Home screen cards") {
                SwitchRow("Tide & Sea card", "Hide this for inland locations", showTide) { viewModel.setShowTideCard(it) }
                SwitchRow("Sun & Moon card", "Sunrise/sunset and moon phase", showMoon) { viewModel.setShowMoonCard(it) }
            }
        }
        item {
            SettingsSection(title = "Map") {
                SwitchRow("Radar layer on by default", "Precipitation radar overlay when opening the map", showRadar) { viewModel.setShowRadarByDefault(it) }
            }
        }
        item {
            SettingsSection(title = "Auto-refresh") {
                Column {
                    RefreshInterval.entries.forEach { option ->
                        RadioRow(
                            label = option.label,
                            selected = refreshInterval == option,
                            onClick = { viewModel.setRefreshInterval(option) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Background refresh requires the app to be reopened periodically; a full WorkManager-based background sync can be added on top of this setting.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitsTab(viewModel: WeatherViewModel) {
    val tempUnit by viewModel.tempUnit.collectAsState()
    val windUnit by viewModel.windUnit.collectAsState()
    val pressureUnit by viewModel.pressureUnit.collectAsState()

    SettingsList {
        item {
            SettingsSection(title = "Temperature") {
                Row {
                    FilterChip(selected = tempUnit == TempUnit.CELSIUS, onClick = { viewModel.setTempUnit(TempUnit.CELSIUS) }, label = { Text("Celsius (°C)") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = tempUnit == TempUnit.FAHRENHEIT, onClick = { viewModel.setTempUnit(TempUnit.FAHRENHEIT) }, label = { Text("Fahrenheit (°F)") })
                }
            }
        }
        item {
            SettingsSection(title = "Wind speed") {
                Row {
                    WindUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = windUnit == unit,
                            onClick = { viewModel.setWindUnit(unit) },
                            label = { Text(unit.label) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Currently displayed values are fetched in km/h; unit conversion for display is wired up here for future use.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            SettingsSection(title = "Pressure") {
                Row {
                    PressureUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = pressureUnit == unit,
                            onClick = { viewModel.setPressureUnit(unit) },
                            label = { Text(unit.label) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayTab(viewModel: WeatherViewModel) {
    val dynamicColor by viewModel.useDynamicColor.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    SettingsList {
        item {
            SettingsSection(title = "Theme") {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        RadioRow(
                            label = when (mode) {
                                ThemeMode.SYSTEM -> "Follow system"
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                            },
                            selected = themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) }
                        )
                    }
                }
            }
        }
        item {
            SettingsSection(title = "Material You") {
                SwitchRow(
                    "Dynamic color",
                    "Color the app from your wallpaper (Android 12+)",
                    dynamicColor
                ) { viewModel.setUseDynamicColor(it) }
            }
        }
    }
}

@Composable
private fun WidgetsTab(viewModel: WeatherViewModel) {
    val showLocation by viewModel.widgetShowLocation.collectAsState()
    val showHumidity by viewModel.widgetShowHumidity.collectAsState()
    val showWind by viewModel.widgetShowWind.collectAsState()
    val showDescription by viewModel.widgetShowDescription.collectAsState()
    val showUv by viewModel.widgetShowUv.collectAsState()
    val showVisibility by viewModel.widgetShowVisibility.collectAsState()
    val showPressure by viewModel.widgetShowPressure.collectAsState()
    val forecastDays by viewModel.widgetForecastDays.collectAsState()

    SettingsList {
        item {
            SettingsSection(title = "Current Weather Widget") {
                SwitchRow("Show location name", "Display the city name on the widget", showLocation) { viewModel.setWidgetShowLocation(it) }
                SwitchRow("Show humidity", "Display current humidity percentage", showHumidity) { viewModel.setWidgetShowHumidity(it) }
                SwitchRow("Show wind speed", "Display current wind speed", showWind) { viewModel.setWidgetShowWind(it) }
                SwitchRow("Show description", "Display weather condition text (e.g., 'Cloudy')", showDescription) { viewModel.setWidgetShowDescription(it) }
                SwitchRow("Show UV index", "Display current UV index level", showUv) { viewModel.setWidgetShowUv(it) }
                SwitchRow("Show visibility", "Display visibility in km", showVisibility) { viewModel.setWidgetShowVisibility(it) }
                SwitchRow("Show pressure", "Display atmospheric pressure", showPressure) { viewModel.setWidgetShowPressure(it) }
            }
        }
        item {
            SettingsSection(title = "Forecast Widget") {
                Text("Number of days to show: $forecastDays", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = forecastDays.toFloat(),
                    onValueChange = { viewModel.setWidgetForecastDays(it.toInt()) },
                    valueRange = 1f..7f,
                    steps = 5
                )
            }
        }
    }
}

@Composable
private fun NotificationsTab(viewModel: WeatherViewModel) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    SettingsList {
        item {
            SettingsSection(title = "Alerts") {
                SwitchRow(
                    "Daily weather notification",
                    "A morning summary notification (requires POST_NOTIFICATIONS permission at runtime on Android 13+)",
                    notificationsEnabled
                ) { viewModel.setNotificationsEnabled(it) }
                Spacer(Modifier.height(8.dp))
                Text(
                    "This toggle stores the preference; wiring it to an actual WorkManager notification job is the next step for a full alerts feature.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AboutTab() {
    val uriHandler = LocalUriHandler.current
    val context = androidx.compose.ui.platform.LocalContext.current
    SettingsList {
        item {
            SettingsSection(title = "Support & Share") {
                Button(
                    onClick = { ShareUtils.shareApk(context) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Share app (APK)")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Check out FossyWeather - a beautiful, Material 3 weather app made by @qoij! 🌦️")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Recommend via link")
                }
            }
        }
        item {
            SettingsSection(title = "Data sources") {
                Text(
                    "Weather, pressure & UV: Open-Meteo Forecast API\n" +
                        "Tide & wave data: Open-Meteo Marine API\n" +
                        "Moon phase: calculated on-device (no network)\n" +
                        "Map: OpenStreetMap via osmdroid\n" +
                        "Radar overlay: RainViewer public API",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "This app uses only free, open, no-API-key-required data sources.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            SettingsSection(title = "Credits") {
                Text(
                    "FossyWeather is a completely free, open-source project dedicated to privacy and Material Design.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Made with ❤️ by ", style = MaterialTheme.typography.bodyLarge)
                    Text("qoij", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(16.dp))
                CreditLink(Icons.Filled.Email, "Email", "qoij69@gmail.com") { uriHandler.openUri("mailto:qoij69@gmail.com") }
                CreditLink(Icons.Filled.Code, "GitHub", "View source code") { uriHandler.openUri("https://github.com/qoij/FossyWeather") }
                CreditLink(Icons.Filled.MusicNote, "TikTok", "@techbyqoij69") { uriHandler.openUri("https://www.tiktok.com/@techbyqoij69") }
                CreditLink(Icons.Filled.PlayArrow, "YouTube", "@qoij69") { uriHandler.openUri("https://www.youtube.com/@qoij69") }
            }
        }
    }
}

@Composable
private fun SettingsList(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CreditLink(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
