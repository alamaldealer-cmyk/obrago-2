package com.obrago.app.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng
import com.obrago.app.data.model.Bid

@Composable
fun LiveMapView(
    modifier: Modifier = Modifier,
    customerLocation: List<Double>?,
    workerLocation: List<Double>? = null,
    biddingWorkers: List<Bid> = emptyList()
) {
    val defaultLatLng = LatLng(33.6844, 73.0479) // Islamabad
    val customerLatLng = customerLocation?.takeIf { it.size == 2 }
        ?.let { LatLng(it[0], it[1]) } ?: defaultLatLng

    val workerLatLng = workerLocation?.takeIf { it.size == 2 }
        ?.let { LatLng(it[0], it[1]) }

    val biddingMarkers = biddingWorkers.mapIndexed { index, bid ->
        val angle = (index * 47) % 360
        val radius = 0.01
        val lat = customerLatLng.latitude + radius * kotlin.math.cos(Math.toRadians(angle.toDouble()))
        val lng = customerLatLng.longitude + radius * kotlin.math.sin(Math.toRadians(angle.toDouble()))
        bid.workerName to LatLng(lat, lng)
    }

    InteractiveLiveMapView(
        modifier = modifier.fillMaxSize(),
        customerLocation = customerLatLng,
        workerLocation = workerLatLng,
        workerBids = biddingMarkers
    )
}

