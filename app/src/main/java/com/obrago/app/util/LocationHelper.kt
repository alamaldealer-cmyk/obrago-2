package com.obrago.app.util

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Mirrors requestLocation() in store.tsx (GPS fetch) and the reverse-geocode
 * fetch to Nominatim in CustomerPostJob (src/CustomerApp.tsx).
 */
object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLatLng(context: Context): Pair<Double, Double>? {
        val client = LocationServices.getFusedLocationProviderClient(context)
        return try {
            val location = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            location?.let { it.latitude to it.longitude }
        } catch (e: Exception) {
            null
        }
    }

    private val httpClient by lazy { OkHttpClient() }

    /** Reverse-geocodes GPS coordinates into a short readable address, same endpoint the web app uses. */
    suspend fun reverseGeocode(lat: Double, lon: Double): Pair<String, String?> = suspendCancellableCoroutine { cont ->
        val request = Request.Builder()
            .url("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon")
            .header("User-Agent", "ObragoApp/1.0")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        httpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                cont.resume("GPS Location (${"%.4f".format(lat)}, ${"%.4f".format(lon)})" to null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                try {
                    val body = response.body?.string()
                    if (body.isNullOrBlank()) {
                        cont.resume("GPS Location (${"%.4f".format(lat)}, ${"%.4f".format(lon)})" to null)
                        return
                    }
                    val json = JSONObject(body)
                    val displayName = json.optString("display_name", "")
                    if (displayName.isNotBlank()) {
                        val shortAddress = displayName.split(",").take(3).joinToString(",").trim()
                        val city = json.optJSONObject("address")?.optString("city")?.takeIf { it.isNotBlank() }
                        cont.resume(shortAddress to city)
                    } else {
                        cont.resume("GPS Location (${"%.4f".format(lat)}, ${"%.4f".format(lon)})" to null)
                    }
                } catch (e: Exception) {
                    cont.resume("GPS Location (${"%.4f".format(lat)}, ${"%.4f".format(lon)})" to null)
                }
            }
        })
    }
}
