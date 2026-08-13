package com.obrago.app.ui.customer

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.Foundation
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.obrago.app.data.model.AppData
import com.obrago.app.data.model.User
import com.obrago.app.ui.map.LocationPickerMapDialog
import com.obrago.app.ui.theme.ObragoGreenDark
import com.obrago.app.util.LocationHelper
import kotlinx.coroutines.launch

@Composable
fun CustomerPostJobScreen(
    categoryId: String,
    currentUser: User?,
    currency: String = "Rs.",
    onBack: () -> Unit,
    onSubmit: (description: String, location: String, city: String, coords: List<Double>?, budget: Double) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val normalizedCatId = normalizeCategoryId(categoryId)
    val category = AppData.CATEGORIES.firstOrNull { it.id.lowercase() == normalizedCatId }
    val categoryDisplayName = category?.name ?: normalizedCatId.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    val currentCurrency by AppSettingsManager.currentCurrency.collectAsState()
    var description by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf(currentUser?.city?.ifBlank { "Lahore" } ?: "Lahore") }
    var location by remember { mutableStateOf("DHA Phase 5, Lahore") }
    var locationStatus by remember { mutableStateOf("Current Location (Fetching...)") }

    val defaultBudgetVal = if (currentCurrency.code == "PKR") "5000" else if (currentCurrency.code in listOf("USD", "EUR", "GBP")) "50" else "200"
    var budget by remember { mutableStateOf(defaultBudgetVal) }
    var selectedChip by remember { mutableStateOf(defaultBudgetVal) }
    var coords by remember { mutableStateOf<List<Double>?>(null) }
    var cityExpanded by remember { mutableStateOf(false) }
    var showLocationModal by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    fun fetchLocationInternal() {
        scope.launch {
            val latLng = LocationHelper.getCurrentLatLng(context)
            if (latLng == null) {
                locationStatus = "Location Unavailable"
                location = "Enter location manually"
                showLocationModal = true
                return@launch
            }
            coords = listOf(latLng.first, latLng.second)
            locationStatus = "GPS Location Updated"
            val (address, city) = LocationHelper.reverseGeocode(latLng.first, latLng.second)
            if (address.isNotBlank()) location = address
            if (!city.isNullOrBlank()) selectedCity = city
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) fetchLocationInternal() else {
            locationStatus = "Permission Denied"
            location = "Tap to choose on map"
        }
    }

    fun fetchLocation() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        fetchLocationInternal()
    }

    LaunchedEffect(Unit) { fetchLocation() }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Round Back Button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F7F0))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ObragoGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Obrago Logo Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(color = Color(0xFF0D253A), fontWeight = FontWeight.Bold, fontSize = 26.sp)) {
                                        append("Obr")
                                    }
                                    withStyle(style = SpanStyle(color = ObragoGreenDark, fontWeight = FontWeight.Bold, fontSize = 26.sp)) {
                                        append("ago")
                                    }
                                }
                            )
                        }

                        // 100% Safe Pill Badge
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFE8F7F0),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = null,
                                    tint = ObragoGreenDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "100% Safe",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ObragoGreenDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Request Worker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Nearby workers will bid shortly",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val isPenalty = (currentUser?.penaltyFee ?: 0.0) > 0
                    Button(
                        onClick = {
                            if (coords == null) {
                                coords = listOf(31.5204, 74.3587) // Default Lahore
                            }
                            onSubmit(description.ifBlank { "Service Request" }, location, selectedCity, coords, budget.toDoubleOrNull() ?: 5000.0)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )

                            Text(
                                text = if (isPenalty) "Pay Penalty & Request Now" else "Request Now",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFFAFAFA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Outstanding Penalty Notice (If applicable)
            if ((currentUser?.penaltyFee ?: 0.0) > 0) {
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Outstanding Penalty", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "You have a penalty fee of $currency${currentUser?.penaltyFee} from a cancelled job.",
                            fontSize = 12.sp,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }

            // 1. What service do you need?
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "What service do you need?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                val serviceIcon = getCategoryIcon(categoryId)
                val serviceDesc = getCategoryDescription(categoryId)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFDCFCE7))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = serviceIcon,
                                contentDescription = null,
                                tint = ObragoGreenDark,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = categoryDisplayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = serviceDesc,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 2. Select City
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select City",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Box {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { cityExpanded = true },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ObragoGreenDark,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = selectedCity,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = cityExpanded,
                        onDismissRequest = { cityExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(Color.White)
                    ) {
                        AppData.PAKISTAN_CITIES.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city, fontSize = 14.sp) },
                                onClick = {
                                    selectedCity = city
                                    cityExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 3. Area / Exact Address
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Area / Exact Address",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Row(
                        modifier = Modifier.clickable { showMapPicker = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = ObragoGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Choose on Google Map",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ObragoGreenDark
                        )
                    }
                }

                // Map Preview Card Box
                MapPreviewCard(
                    locationStatus = locationStatus,
                    locationAddress = location,
                    onMapClick = { showMapPicker = true },
                    onGpsClick = { fetchLocation() }
                )
            }

            // 4. Your Budget (PKR)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Budget (PKR)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                Toast.makeText(context, "Workers will bid near or below this budget.", Toast.LENGTH_SHORT).show()
                            }
                    )
                }

                // Budget Input Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F7F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Payments,
                                contentDescription = null,
                                tint = ObragoGreenDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        TextField(
                            value = budget,
                            onValueChange = {
                                budget = it
                                selectedChip = if (it in listOf("500", "1000", "5000")) it else "Custom"
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        )

                        Column {
                            IconButton(
                                onClick = {
                                    val current = budget.toDoubleOrNull() ?: 0.0
                                    budget = (current + 500).toInt().toString()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase", tint = Color(0xFF64748B))
                            }
                            IconButton(
                                onClick = {
                                    val current = budget.toDoubleOrNull() ?: 0.0
                                    if (current >= 500) {
                                        budget = (current - 500).toInt().toString()
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease", tint = Color(0xFF64748B))
                            }
                        }
                    }
                }

                // Quick Selection Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chips = listOf("500" to "PKR 500", "1000" to "PKR 1,000", "5000" to "PKR 5,000", "Custom" to "Custom")
                    chips.forEach { (key, label) ->
                        val isSelected = selectedChip == key || (key == "5000" && budget == "5000")
                        Surface(
                            onClick = {
                                selectedChip = key
                                if (key != "Custom") budget = key
                            },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) Color(0xFFF0FDF4) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) ObragoGreenDark else Color.Transparent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ObragoGreenDark else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Add Details (Optional)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Add Details (Optional)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            TextField(
                                value = description,
                                onValueChange = { if (it.length <= 200) description = it },
                                placeholder = {
                                    Text("e.g. Pipe leaking in kitchen sink...", color = Color(0xFF94A3B8), fontSize = 14.sp)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 70.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }

                        Text(
                            text = "${description.length}/200",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            // 6. Trust Features Banner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrustFeatureItem(icon = Icons.Outlined.Construction, title = "Verified Workers")
                    VerticalDivider(modifier = Modifier.height(20.dp), color = Color(0xFFBBF7D0))
                    TrustFeatureItem(icon = Icons.Outlined.FlashOn, title = "Quick Response")
                    VerticalDivider(modifier = Modifier.height(20.dp), color = Color(0xFFBBF7D0))
                    TrustFeatureItem(icon = Icons.Outlined.StarOutline, title = "Best Prices")
                }
            }
        }
    }

    if (showLocationModal) {
        AlertDialog(
            onDismissRequest = { showLocationModal = false },
            title = { Text("Location / GPS Required", fontWeight = FontWeight.Bold) },
            text = { Text("Job post karne se pehle mobile ki Location / GPS ON honi lazmi hai taake qareebi workers ko request bhej saken.") },
            confirmButton = {
                TextButton(onClick = {
                    showLocationModal = false
                    fetchLocation()
                }) {
                    Text("Turn On GPS & Retry", color = ObragoGreenDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationModal = false }) { Text("Cancel") }
            }
        )
    }

    if (showMapPicker) {
        LocationPickerMapDialog(
            onDismiss = { showMapPicker = false },
            onLocationSelected = { address, city, latLng ->
                location = address
                coords = if (latLng != null) listOf(latLng.latitude, latLng.longitude) else listOf(31.5204, 74.3587)
                if (!city.isNullOrBlank()) {
                    selectedCity = city
                }
                locationStatus = "Map Location Selected"
                showMapPicker = false
            }
        )
    }
}

/** Custom Stylized Map Preview Card with Floating Address & GPS target */
@Composable
private fun MapPreviewCard(
    locationStatus: String,
    locationAddress: String,
    onMapClick: () -> Unit,
    onGpsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable { onMapClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF5F2)),
        border = BorderStroke(1.dp, Color(0xFFD8E7E1))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw Map Background Grid (roads and green parks)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Green park patches
                drawRoundRect(
                    color = Color(0xFFDCF0E6),
                    topLeft = Offset(w * 0.08f, h * 0.08f),
                    size = Size(w * 0.38f, h * 0.45f),
                    cornerRadius = CornerRadius(16f, 16f)
                )

                drawRoundRect(
                    color = Color(0xFFD4ECE1),
                    topLeft = Offset(w * 0.55f, h * 0.48f),
                    size = Size(w * 0.38f, h * 0.45f),
                    cornerRadius = CornerRadius(16f, 16f)
                )

                // White roads
                val roadColor = Color.White
                drawRect(
                    color = roadColor,
                    topLeft = Offset(0f, h * 0.45f),
                    size = Size(w, 16f)
                )
                drawRect(
                    color = roadColor,
                    topLeft = Offset(w * 0.45f, 0f),
                    size = Size(16f, h)
                )
                drawRect(
                    color = roadColor,
                    topLeft = Offset(w * 0.82f, 0f),
                    size = Size(14f, h)
                )
            }

            // Foreground Floating UI Elements
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Floating Address Pill
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ObragoGreenDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = locationStatus,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = locationAddress,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Floating Circular Target GPS Button
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { onGpsClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Target GPS",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrustFeatureItem(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ObragoGreenDark,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ObragoGreenDark
        )
    }
}

private fun normalizeCategoryId(id: String): String {
    return when (id.lowercase().trim()) {
        "electrical", "electrician" -> "electrician"
        "plumbing", "plumber" -> "plumber"
        "carpentry", "carpenter" -> "carpenter"
        "cleaning", "cleaner" -> "cleaner"
        "painting", "painter" -> "painter"
        "ac_repair", "ac", "ac technician" -> "ac"
        "mechanic" -> "mechanic"
        "mason", "construction" -> "mason"
        else -> id.lowercase().trim()
    }
}

private fun getCategoryIcon(categoryId: String): ImageVector {
    return when (normalizeCategoryId(categoryId)) {
        "electrician" -> Icons.Outlined.FlashOn
        "plumber" -> Icons.Outlined.WaterDrop
        "carpenter" -> Icons.Outlined.Build
        "cleaner" -> Icons.Outlined.CleaningServices
        "painter" -> Icons.Outlined.FormatPaint
        "mechanic" -> Icons.Outlined.Build
        "ac" -> Icons.Outlined.AcUnit
        "mason" -> Icons.Outlined.Foundation
        else -> Icons.Outlined.Construction
    }
}

private fun getCategoryDescription(categoryId: String): String {
    return when (normalizeCategoryId(categoryId)) {
        "electrician" -> "Wiring, switchboard, short circuit"
        "plumber" -> "Pipe repair, leakage, installation"
        "carpenter" -> "Door, furniture, lock repair"
        "cleaner" -> "Home cleaning, sofa, deep clean"
        "painter" -> "Wall paint, touch-up, polish"
        "mechanic" -> "Engine, brakes, maintenance"
        "ac" -> "AC service, gas refill, repair"
        "mason" -> "Tile, wall, concrete repair"
        else -> "Professional repair & service"
    }
}
