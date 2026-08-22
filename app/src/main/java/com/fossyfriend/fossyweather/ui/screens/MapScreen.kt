package com.fossyfriend.fossyweather.ui.screens

import android.content.Context
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.net.URL

/**
 * RainViewer (https://www.rainviewer.com/api.html) publishes free public radar tiles,
 * no API key required. This builds the correctly-shaped tile URL for their API:
 * https://tilecache.rainviewer.com{path}/256/{z}/{x}/{y}/2/1_1.png
 */
private class RainViewerTileSource(private val path: String) : OnlineTileSourceBase(
    "RainViewerRadar", 0, 12, 256, ".png",
    arrayOf("https://tilecache.rainviewer.com")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val z = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "$baseUrl$path/256/$z/$x/$y/2/1_1.png"
    }
}

private suspend fun fetchLatestRainViewerPath(): String? = withContext(Dispatchers.IO) {
    runCatching {
        val text = URL("https://api.rainviewer.com/public/weather-maps.json").readText()
        val json = JSONObject(text)
        val radar = json.getJSONObject("radar")
        val past = radar.getJSONArray("past")
        val latest = past.getJSONObject(past.length() - 1)
        latest.getString("path")
    }.getOrNull()
}

/**
 * OpenStreetMap-based map (osmdroid) — fully FOSS, no API key required.
 * Optionally overlays live precipitation radar tiles from RainViewer's free public API.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    latitude: Double,
    longitude: Double,
    locationName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showRadar by remember { mutableStateOf(true) }
    var radarPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
        radarPath = fetchLatestRainViewerPath()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(locationName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showRadar = !showRadar }) {
                        Icon(Icons.Filled.Layers, contentDescription = "Toggle radar layer")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            OsmMapView(latitude = latitude, longitude = longitude, showRadar = showRadar, radarPath = radarPath)
            if (showRadar) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 3.dp
                ) {
                    Text(
                        "Precipitation radar: RainViewer (free, public)",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun OsmMapView(latitude: Double, longitude: Double, showRadar: Boolean, radarPath: String?) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setMultiTouchControls(true)
                controller.setZoom(10.0)
                controller.setCenter(GeoPoint(latitude, longitude))
                val marker = Marker(this).apply {
                    position = GeoPoint(latitude, longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                overlays.add(marker)
                mapView.value = this
            }
        },
        update = { view ->
            view.controller.setCenter(GeoPoint(latitude, longitude))
            val existingRadar = view.overlays.filterIsInstance<TilesOverlay>()
            existingRadar.forEach { view.overlays.remove(it) }

            if (showRadar && radarPath != null) {
                val provider = org.osmdroid.tileprovider.MapTileProviderBasic(view.context, RainViewerTileSource(radarPath))
                val radarOverlay = TilesOverlay(provider, view.context).apply {
                    loadingBackgroundColor = android.graphics.Color.TRANSPARENT
                    loadingLineColor = android.graphics.Color.TRANSPARENT
                }
                view.overlays.add(radarOverlay)
            }
            view.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.value?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.value?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.value?.onDetach()
        }
    }
}
