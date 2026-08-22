package com.fossyfriend.fossyweather.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Uses the plain Android LocationManager (no Google Play Services dependency)
 * so the app stays fully buildable/runnable on de-Googled / FOSS devices (e.g. GrapheneOS, /e/OS).
 */
class LocationProvider(private val context: Context) {

    private val locationManager: LocationManager
        get() = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownOrCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        val lm = locationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { lm.isProviderEnabled(it) }

        // Try last known location first for instant results.
        val lastKnown = providers
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

        if (lastKnown != null && System.currentTimeMillis() - lastKnown.time < 10 * 60 * 1000) {
            cont.resume(lastKnown)
            return@suspendCancellableCoroutine
        }

        if (providers.isEmpty()) {
            cont.resume(lastKnown)
            return@suspendCancellableCoroutine
        }

        // Wait for a fresh fix. The caller (ViewModel) wraps this in withTimeoutOrNull,
        // and cancellation here removes the listener so we never leak updates.
        var resolved = false
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (!resolved) {
                    resolved = true
                    providers.forEach { runCatching { lm.removeUpdates(this) } }
                    cont.resume(location)
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        providers.forEach { provider ->
            runCatching {
                lm.requestLocationUpdates(provider, 0L, 0f, listener, context.mainLooper)
            }
        }

        cont.invokeOnCancellation {
            providers.forEach { runCatching { lm.removeUpdates(listener) } }
        }
    }

    fun isLocationEnabled(): Boolean {
        val lm = locationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
