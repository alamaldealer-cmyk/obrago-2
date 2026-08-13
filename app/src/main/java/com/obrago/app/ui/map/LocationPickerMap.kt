package com.obrago.app.ui.map

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.obrago.app.ui.theme.ObragoGreenDark
import com.obrago.app.util.LocationHelper
import kotlinx.coroutines.launch

@Composable
fun LocationPickerMapDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (address: String, city: String?, latLng: LatLng?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedLatLng by remember { mutableStateOf(LatLng(31.5204, 74.3587)) } // Default Lahore/Pakistan
    var manualAddress by remember { mutableStateOf("") }
    var manualCity by remember { mutableStateOf("") }
    var isLoadingAddress by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng, 15f)
    }

    // Reverse geocode whenever map is tapped
    fun updateLocation(latLng: LatLng) {
        selectedLatLng = latLng
        isLoadingAddress = true
        scope.launch {
            val (shortAddress, city) = LocationHelper.reverseGeocode(latLng.latitude, latLng.longitude)
            manualAddress = shortAddress
            if (!city.isNullOrBlank()) {
                manualCity = city
            }
            isLoadingAddress = false
        }
    }

    // Try fetching GPS on open
    LaunchedEffect(Unit) {
        val latLngPair = LocationHelper.getCurrentLatLng(context)
        if (latLngPair != null) {
            val gpsLatLng = LatLng(latLngPair.first, latLngPair.second)
            cameraPositionState.position = CameraPosition.fromLatLngZoom(gpsLatLng, 15f)
            updateLocation(gpsLatLng)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF3F4F6)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive OpenStreetMap / Leaflet View
                InteractiveMapPickerView(
                    modifier = Modifier.fillMaxSize(),
                    initialLatLng = selectedLatLng,
                    onLocationSelected = { latLng ->
                        updateLocation(latLng)
                    }
                )

                // Top Floating Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }

                    Card(
                        shape = RoundedCornerShape(50),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Text(
                            text = "Tap on map to select location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                val latLngPair = LocationHelper.getCurrentLatLng(context)
                                if (latLngPair != null) {
                                    val gpsLatLng = LatLng(latLngPair.first, latLngPair.second)
                                    cameraPositionState.animate(
                                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(gpsLatLng, 16f)
                                    )
                                    updateLocation(gpsLatLng)
                                }
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        containerColor = Color.White,
                        contentColor = ObragoGreenDark,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                    }
                }

                // Bottom Input Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 42.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ObragoGreenDark,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLoadingAddress) "FETCHING ADDRESS..." else "SELECTED LOCATION DETAILS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = manualAddress,
                            onValueChange = { manualAddress = it },
                            label = { Text("Full Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = manualCity,
                            onValueChange = { manualCity = it },
                            label = { Text("City") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (manualAddress.isNotBlank()) {
                                    onLocationSelected(manualAddress, manualCity.ifBlank { "Unknown" }, selectedLatLng)
                                }
                            },
                            enabled = manualAddress.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Confirm Location",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
