package com.fossyfriend.fossyweather.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.data.prefs.TempUnit
import com.fossyfriend.fossyweather.domain.DayForecast
import com.fossyfriend.fossyweather.domain.HourForecast
import com.fossyfriend.fossyweather.domain.MetricType
import com.fossyfriend.fossyweather.domain.WeatherBundle
import com.fossyfriend.fossyweather.domain.WeatherCode
import com.fossyfriend.fossyweather.ui.components.*
import com.fossyfriend.fossyweather.util.formatTemp
import com.fossyfriend.fossyweather.util.isoDateToFullLabel
import com.fossyfriend.fossyweather.util.isoToHourLabel
import com.fossyfriend.fossyweather.util.isoToTimeLabel
import com.fossyfriend.fossyweather.viewmodel.UiState
import com.fossyfriend.fossyweather.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel,
    onOpenMap: (WeatherBundle) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMetric: (MetricType) -> Unit,
    onOpenPressure: () -> Unit,
    onOpenTide: () -> Unit,
    onOpenMoon: () -> Unit,
    onOpenCurrentDetail: () -> Unit,
    onOpenHourDetail: (Int) -> Unit,
    onOpenDayDetail: (Int) -> Unit,
    onRequestLocationPermission: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val showTideCard by viewModel.showTideCard.collectAsState()
    val showMoonCard by viewModel.showMoonCard.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Locations", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { 
                        showSearch = true
                        scope.launch { drawerState.close() }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add location")
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(savedLocations, key = { it.id }) { place ->
                        LocationDrawerItem(
                            place = place,
                            isSelected = selectedLocation?.id == place.id,
                            onClick = {
                                viewModel.loadWeather(place.latitude, place.longitude, place.displayName)
                                scope.launch { drawerState.close() }
                            },
                            onDelete = { viewModel.removeLocation(place.id) }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("FossyWeather") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onRequestLocationPermission) {
                            Icon(Icons.Filled.MyLocation, contentDescription = "Use my location")
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    },
                    label = "home_state"
                ) { state ->
                    when (state) {
                        is UiState.Loading -> LoadingState()
                        is UiState.NeedsPermission -> PermissionRationaleState(onRequestLocationPermission, onOpenSearch = { showSearch = true })
                        is UiState.Error -> ErrorState(state.message, onRetry = onRequestLocationPermission, onOpenSearch = { showSearch = true })
                        is UiState.Success -> WeatherContent(
                            state = state,
                            showTideCard = showTideCard,
                            showMoonCard = showMoonCard,
                            onOpenMap = { onOpenMap(state.bundle) },
                            onOpenMetric = onOpenMetric,
                            onOpenPressure = onOpenPressure,
                            onOpenTide = onOpenTide,
                            onOpenMoon = onOpenMoon,
                            onOpenCurrentDetail = onOpenCurrentDetail,
                            onOpenHourDetail = onOpenHourDetail,
                            onOpenDayDetail = onOpenDayDetail
                        )
                    }
                }

                if (showSearch) {
                    LocationSearchSheet(
                        viewModel = viewModel,
                        onDismiss = { showSearch = false },
                        onPlaceSelected = { place ->
                            viewModel.loadWeather(place.latitude, place.longitude, place.displayName)
                            showSearch = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationDrawerItem(
    place: com.fossyfriend.fossyweather.domain.PlaceResult,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd) {
                showDeleteDialog = true
                false // Don't dismiss yet, wait for dialog
            } else false
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Location") },
            text = { Text("Are you sure you want to remove ${place.name} from your saved locations?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                MaterialTheme.colorScheme.errorContainer
            } else Color.Transparent
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        NavigationDrawerItem(
            label = {
                Column {
                    Text(place.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    Text(place.displayName, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            },
            selected = isSelected,
            onClick = onClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

@Composable
private fun WeatherContent(
    state: UiState.Success,
    showTideCard: Boolean,
    showMoonCard: Boolean,
    onOpenMap: () -> Unit,
    onOpenMetric: (MetricType) -> Unit,
    onOpenPressure: () -> Unit,
    onOpenTide: () -> Unit,
    onOpenMoon: () -> Unit,
    onOpenCurrentDetail: () -> Unit,
    onOpenHourDetail: (Int) -> Unit,
    onOpenDayDetail: (Int) -> Unit
) {
    val bundle = state.bundle
    val listState = rememberLazyListState()

    // One-shot entrance animation the first time real content appears.
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 8 }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "current") {
                AnimatedSection(visible = visibleState.targetState, delay = 0) {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            val firstVisible = listState.firstVisibleItemIndex == 0
                            val offset = if (firstVisible) listState.firstVisibleItemScrollOffset else 600
                            val fraction = (offset / 600f).coerceIn(0f, 1f)
                            alpha = 1f - fraction
                            scaleX = 1f - fraction * 0.08f
                            scaleY = 1f - fraction * 0.08f
                            translationY = -fraction * 40f
                        }
                    ) {
                        CurrentWeatherCard(
                            locationName = bundle.locationName,
                            current = bundle.current,
                            tempUnit = state.tempUnit,
                            onClick = onOpenCurrentDetail
                        )
                    }
                }
            }
            item(key = "map_button") {
                AnimatedSection(visible = visibleState.targetState, delay = 100) {
                    OutlinedButton(onClick = onOpenMap, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Map, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("View on map")
                    }
                }
            }
            item(key = "hourly") {
                AnimatedSection(visible = visibleState.targetState, delay = 200) {
                    HourlyForecastRow(bundle.hourly, state.tempUnit, onHourClick = { hour ->
                        val index = bundle.hourly.indexOf(hour)
                        if (index != -1) onOpenHourDetail(index)
                    })
                }
            }
            item(key = "details") {
                AnimatedSection(visible = visibleState.targetState, delay = 300) {
                    DetailsGrid(bundle.current, onMetricClick = onOpenMetric)
                }
            }
            item(key = "pressure") {
                AnimatedSection(visible = visibleState.targetState, delay = 400) {
                    PressureTrendCard(bundle.hourly, bundle.current.pressureMsl, onClick = onOpenPressure)
                }
            }
            if (showMoonCard) {
                item(key = "moon") {
                    AnimatedSection(visible = visibleState.targetState, delay = 500) {
                        val today = bundle.daily.firstOrNull()
                        MoonPhaseCard(
                            moon = state.moonPhase,
                            sunrise = today?.sunrise ?: "",
                            sunset = today?.sunset ?: "",
                            onClick = onOpenMoon
                        )
                    }
                }
            }
            if (showTideCard) {
                item(key = "tide") {
                    AnimatedSection(visible = visibleState.targetState, delay = 600) {
                        TideCard(bundle.marine, bundle.isCoastal, onClick = onOpenTide)
                    }
                }
            }
            item(key = "daily") {
                AnimatedSection(visible = visibleState.targetState, delay = 700) {
                    DailyForecastList(bundle.daily, state.tempUnit, onDayClick = { day ->
                        val index = bundle.daily.indexOf(day)
                        if (index != -1) onOpenDayDetail(index)
                    })
                }
            }
            item(key = "last_updated") {
                val timeStr = remember(state.lastUpdated) {
                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(state.lastUpdated))
                }
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Last updated at $timeStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            item(key = "bottom_spacer") { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AnimatedSection(
    visible: Boolean,
    delay: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400, delayMillis = delay)) + 
                slideInVertically(tween(400, delayMillis = delay)) { it / 10 }
    ) {
        content()
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Fetching weather…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PermissionRationaleState(onRequestPermission: () -> Unit, onOpenSearch: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text("Location access needed", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "FossyWeather uses your device location to show local weather. You can grant access or search for a city manually.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRequestPermission) { Text("Grant location access") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onOpenSearch) { Text("Search for a city instead") }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, onOpenSearch: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Something went wrong", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRetry) { Text("Try again") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onOpenSearch) { Text("Search for a city instead") }
    }
}
