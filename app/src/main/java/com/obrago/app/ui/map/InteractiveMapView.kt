package com.obrago.app.ui.map

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.model.LatLng

@Composable
fun InteractiveMapPickerView(
    modifier: Modifier = Modifier,
    initialLatLng: LatLng = LatLng(31.5204, 74.3587),
    onLocationSelected: (LatLng) -> Unit
) {
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body, html, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #e5e7eb; }
                .leaflet-container { font-family: sans-serif; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: false }).setView([${initialLatLng.latitude}, ${initialLatLng.longitude}], 14);
                L.control.zoom({ position: 'bottomright' }).addTo(map);

                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: 'OpenStreetMap'
                }).addTo(map);

                var greenIcon = L.icon({
                    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
                    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                    iconSize: [25, 41],
                    iconAnchor: [12, 41],
                    popupAnchor: [1, -34],
                    shadowSize: [41, 41]
                });

                var marker = L.marker([${initialLatLng.latitude}, ${initialLatLng.longitude}], {
                    draggable: true,
                    icon: greenIcon
                }).addTo(map);

                function notifyLocation(lat, lng) {
                    if (window.Android && window.Android.onLocationPicked) {
                        window.Android.onLocationPicked(lat, lng);
                    }
                }

                map.on('click', function(e) {
                    var lat = e.latlng.lat;
                    var lng = e.latlng.lng;
                    marker.setLatLng(e.latlng);
                    notifyLocation(lat, lng);
                });

                marker.on('dragend', function(e) {
                    var position = marker.getLatLng();
                    notifyLocation(position.lat, position.lng);
                });
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onLocationPicked(lat: Double, lng: Double) {
                        onLocationSelected(LatLng(lat, lng))
                    }
                }, "Android")
                loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
            }
        }
    )
}

@Composable
fun InteractiveLiveMapView(
    modifier: Modifier = Modifier,
    customerLocation: LatLng,
    workerLocation: LatLng? = null,
    workerBids: List<Pair<String, LatLng>> = emptyList()
) {
    val workerMarkersJs = workerLocation?.let {
        "L.marker([${it.latitude}, ${it.longitude}]).addTo(map).bindPopup('Assigned Worker');"
    } ?: ""

    val bidsMarkersJs = workerBids.joinToString("\n") { (name, pos) ->
        "L.marker([${pos.latitude}, ${pos.longitude}]).addTo(map).bindPopup('${name.replace("'", "\\'")}');"
    }

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body, html, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #e5e7eb; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: false }).setView([${customerLocation.latitude}, ${customerLocation.longitude}], 14);
                L.control.zoom({ position: 'bottomright' }).addTo(map);

                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: 'OpenStreetMap'
                }).addTo(map);

                var customerMarker = L.marker([${customerLocation.latitude}, ${customerLocation.longitude}]).addTo(map).bindPopup('You (Customer)');

                $workerMarkersJs
                $bidsMarkersJs
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
            }
        }
    )
}
