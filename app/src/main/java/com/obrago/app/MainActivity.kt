package com.obrago.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.obrago.app.data.repository.AppSettingsManager
import com.obrago.app.notifications.NotificationHelper
import com.obrago.app.ui.navigation.ObragoNavGraph
import com.obrago.app.ui.theme.ObragoTheme
import com.obrago.app.util.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppSettingsManager.init(applicationContext)
        NotificationHelper.ensureChannel(this)
        setContent {
            ObragoRoot()
        }
    }
}

@Composable
private fun ObragoRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDarkMode by AppSettingsManager.isDarkMode.collectAsState()

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            scope.launch(Dispatchers.IO) {
                try {
                    val latLng = LocationHelper.getCurrentLatLng(context)
                    if (latLng != null) {
                        val geocoder = android.location.Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(latLng.first, latLng.second, 1)
                        val countryCode = addresses?.firstOrNull()?.countryCode
                        if (!countryCode.isNullOrBlank()) {
                            AppSettingsManager.updateCurrencyFromLocation(countryCode)
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to system locale country
                    val systemCountry = Locale.getDefault().country
                    if (systemCountry.isNotBlank()) {
                        AppSettingsManager.updateCurrencyFromLocation(systemCountry)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }

    ObragoTheme(darkTheme = isDarkMode) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            ObragoNavGraph()
        }
    }
}

