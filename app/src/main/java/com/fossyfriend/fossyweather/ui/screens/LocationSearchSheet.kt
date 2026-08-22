package com.fossyfriend.fossyweather.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fossyfriend.fossyweather.domain.PlaceResult
import com.fossyfriend.fossyweather.viewmodel.WeatherViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchSheet(
    viewModel: WeatherViewModel,
    onDismiss: () -> Unit,
    onPlaceSelected: (PlaceResult) -> Unit
) {
    val results by viewModel.searchResults.collectAsState()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(query) {
        delay(350) // debounce
        if (query.length >= 2) viewModel.searchPlaces(query) else viewModel.clearSearchResults()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearSearchResults() }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search city or place") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(results) { place ->
                    ListItem(
                        headlineContent = { Text(place.displayName) },
                        leadingContent = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                        modifier = Modifier.clickable {
                            onPlaceSelected(place)
                            viewModel.saveLocation(place)
                        }
                    )
                }
            }
        }
    }
}
