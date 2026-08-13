package com.obrago.app.ui.customer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.obrago.app.data.repository.SessionManager
import com.obrago.app.data.repository.AppSettingsManager
import com.obrago.app.data.repository.AppLanguage
import com.obrago.app.data.repository.CurrencyOption
import com.obrago.app.ui.theme.ObragoGreenDark

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.obrago.app.data.model.Role
import com.obrago.app.data.model.User
import com.obrago.app.data.repository.UserRepository
import com.obrago.app.data.repository.JobRepository

private enum class ProfileScreenState {
    MAIN,
    PERSONAL_INFO,
    PAYMENT_METHODS,
    MY_ADDRESSES,
    SAVED_WORKERS,
    NOTIFICATIONS,
    HELP_SUPPORT,
    SETTINGS
}

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    var screenState by remember { mutableStateOf(ProfileScreenState.MAIN) }

    when (screenState) {
        ProfileScreenState.MAIN -> MainProfileView(
            onNavigate = { screenState = it },
            onBack = onBack,
            onLoggedOut = onLoggedOut
        )
        ProfileScreenState.PERSONAL_INFO -> PersonalInformationScreenView(
            onBack = { screenState = ProfileScreenState.MAIN },
            onHomeBack = onBack
        )
        ProfileScreenState.PAYMENT_METHODS -> PaymentMethodsScreenView(
            onBack = { screenState = ProfileScreenState.MAIN },
            onHomeBack = onBack
        )
        ProfileScreenState.MY_ADDRESSES -> MyAddressesScreenView(
            onBack = { screenState = ProfileScreenState.MAIN },
            onHomeBack = onBack
        )
        ProfileScreenState.SAVED_WORKERS -> SavedWorkersScreenView(
            onBack = { screenState = ProfileScreenState.MAIN },
            onHomeBack = onBack
        )
        ProfileScreenState.NOTIFICATIONS -> NotificationsScreenView(
            onBack = { screenState = ProfileScreenState.MAIN },
            onHomeBack = onBack
        )
        ProfileScreenState.HELP_SUPPORT -> HelpSupportScreenView(
            onBack = { screenState = ProfileScreenState.MAIN },
            onHomeBack = onBack
        )
        ProfileScreenState.SETTINGS -> SettingsScreenView(
            onBack = { screenState = ProfileScreenState.MAIN },
            onHomeBack = onBack
        )
    }
}

@Composable
private fun ProfileBottomNavigation(
    onHomeBack: () -> Unit,
    onOpenSaved: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeBack,
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home", tint = Color.Gray) },
            label = { Text("Home", color = Color.Gray) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = false,
            onClick = onHomeBack,
            icon = { Icon(Icons.Outlined.WorkOutline, contentDescription = "Bookings", tint = Color.Gray) },
            label = { Text("Bookings", color = Color.Gray) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = false,
            onClick = onHomeBack,
            icon = { Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Messages", tint = Color.Gray) },
            label = { Text("Messages", color = Color.Gray) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = false,
            onClick = onOpenSaved,
            icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Saved", tint = Color.Gray) },
            label = { Text("Saved", color = Color.Gray) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = ObragoGreenDark) },
            label = { Text("Profile", color = ObragoGreenDark) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )
    }
}

@Composable
private fun MainProfileView(
    onNavigate: (ProfileScreenState) -> Unit,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val jobRepository = remember { JobRepository() }
    val user by SessionManager.currentUser.collectAsState()
    val allJobs by jobRepository.observeJobs().collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()

    val myJobs = remember(allJobs, user?.id) {
        val uid = user?.id
        if (uid == null) emptyList()
        else allJobs.filter { it.customerId == uid || it.workerId == uid }
    }

    val ongoingCount = myJobs.count { it.status == "accepted" || it.status == "worker_arrived" || it.status == "in_progress" }
    val completedCount = myJobs.count { it.status == "completed" }
    val cancelledCount = myJobs.count { it.status == "cancelled" }
    val pendingCount = myJobs.count { it.status == "bidding" || it.status == "searching" }

    var showAddMoneyDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showMenuDrawer by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            ProfileBottomNavigation(
                onHomeBack = onBack,
                onOpenSaved = { onNavigate(ProfileScreenState.SAVED_WORKERS) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("O", fontWeight = FontWeight.Black, fontSize = 18.sp, color = ObragoGreenDark)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MY PROFILE", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                }

                IconButton(onClick = { onNavigate(ProfileScreenState.NOTIFICATIONS) }) {
                    Box {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = ObragoGreenDark,
                            modifier = Modifier.size(28.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .clip(CircleShape)
                                .background(ObragoGreenDark)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Info Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigate(ProfileScreenState.PERSONAL_INFO) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with Camera Overlay
                Box(modifier = Modifier.size(88.dp)) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.avatar.isNullOrBlank()) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "User Avatar",
                                tint = ObragoGreenDark,
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            AsyncImage(
                                model = user?.avatar,
                                contentDescription = "User Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                            .clickable { onNavigate(ProfileScreenState.PERSONAL_INFO) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = ObragoGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.name?.ifBlank { "Usama" } ?: "Usama",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user?.email?.ifBlank { "usama@gmail.com" } ?: "usama@gmail.com",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = ObragoGreenDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user?.phone?.ifBlank { "+92 300 1234567" } ?: "+92 300 1234567",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = ObragoGreenDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user?.city?.ifBlank { "Sahiwal, Pakistan" } ?: "Sahiwal, Pakistan",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Edit Profile",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Wallet Balance Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0FDF4))
                    .border(1.dp, Color(0xFFD1FAE5), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = ObragoGreenDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Wallet Balance", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "PKR ${user?.points ?: 4320}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ObragoGreenDark
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(Color(0xFFD1FAE5))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { showAddMoneyDialog = true },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text("Add Money", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // My Bookings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("My Bookings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text(
                            "View all",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ObragoGreenDark,
                            modifier = Modifier.clickable { onBack() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BookingStatItem(
                            icon = Icons.Outlined.WorkOutline,
                            count = "$ongoingCount",
                            label = "Ongoing",
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        )
                        Box(modifier = Modifier.height(40.dp).width(1.dp).background(Color(0xFFF3F4F6)))
                        BookingStatItem(
                            icon = Icons.Outlined.CheckCircle,
                            count = "$completedCount",
                            label = "Completed",
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        )
                        Box(modifier = Modifier.height(40.dp).width(1.dp).background(Color(0xFFF3F4F6)))
                        BookingStatItem(
                            icon = Icons.Outlined.Cancel,
                            count = "$cancelledCount",
                            label = "Cancelled",
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        )
                        Box(modifier = Modifier.height(40.dp).width(1.dp).background(Color(0xFFF3F4F6)))
                        BookingStatItem(
                            icon = Icons.Outlined.AccessTime,
                            count = "$pendingCount",
                            label = "Pending",
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Account Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        "Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AccountRow(
                        title = "Personal Information",
                        icon = Icons.Outlined.PersonOutline,
                        showDivider = true,
                        onClick = { onNavigate(ProfileScreenState.PERSONAL_INFO) }
                    )
                    AccountRow(
                        title = "Payment Methods",
                        icon = Icons.Outlined.CreditCard,
                        showDivider = true,
                        onClick = { onNavigate(ProfileScreenState.PAYMENT_METHODS) }
                    )
                    AccountRow(
                        title = "My Addresses",
                        icon = Icons.Outlined.LocationOn,
                        showDivider = true,
                        onClick = { onNavigate(ProfileScreenState.MY_ADDRESSES) }
                    )
                    AccountRow(
                        title = "Saved Workers",
                        icon = Icons.Outlined.PersonAdd,
                        showDivider = true,
                        onClick = { onNavigate(ProfileScreenState.SAVED_WORKERS) }
                    )
                    AccountRow(
                        title = "Notifications",
                        icon = Icons.Outlined.Notifications,
                        showDivider = true,
                        onClick = { onNavigate(ProfileScreenState.NOTIFICATIONS) }
                    )
                    AccountRow(
                        title = "Help & Support",
                        icon = Icons.Outlined.HeadsetMic,
                        showDivider = true,
                        onClick = { onNavigate(ProfileScreenState.HELP_SUPPORT) }
                    )
                    AccountRow(
                        title = "Settings",
                        icon = Icons.Outlined.Settings,
                        showDivider = false,
                        onClick = { onNavigate(ProfileScreenState.SETTINGS) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log Out Button Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutConfirmDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = "Log Out",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        "Log Out",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddMoneyDialog) {
        var selectedAmount by remember { mutableIntStateOf(1000) }
        var customAmountText by remember { mutableStateOf("") }
        var paymentMethod by remember { mutableStateOf("EasyPaisa") }

        AlertDialog(
            onDismissRequest = { showAddMoneyDialog = false },
            title = { Text("Top Up Wallet", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)) },
            text = {
                Column {
                    Text("Select Amount (PKR):", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(500, 1000, 2000, 5000).forEach { amt ->
                            FilterChip(
                                selected = selectedAmount == amt && customAmountText.isEmpty(),
                                onClick = {
                                    selectedAmount = amt
                                    customAmountText = ""
                                },
                                label = { Text("Rs $amt", fontSize = 12.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { customAmountText = it },
                        label = { Text("Or Enter Custom Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Payment Method:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf("EasyPaisa", "JazzCash", "Debit / Credit Card").forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = method }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(method, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val addVal = customAmountText.toIntOrNull() ?: selectedAmount
                        user?.let { current ->
                            val currentPts = current.points ?: 0
                            SessionManager.updateCurrentUser(current.copy(points = currentPts + addVal))
                        }
                        showAddMoneyDialog = false
                        Toast.makeText(context, "Added PKR $addVal via $paymentMethod!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMoneyDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showSettingsDialog) {
        var isUrdu by remember { mutableStateOf(false) }
        var pushNotificationsEnabled by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("App Settings", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Language (Urdu / English)", fontSize = 14.sp)
                        Switch(checked = isUrdu, onCheckedChange = { isUrdu = it })
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Push Notifications", fontSize = 14.sp)
                        Switch(checked = pushNotificationsEnabled, onCheckedChange = { pushNotificationsEnabled = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSettingsDialog = false
                        Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Save Settings")
                }
            }
        )
    }

    if (showMenuDrawer) {
        AlertDialog(
            onDismissRequest = { showMenuDrawer = false },
            title = { Text("Obrago Menu", fontWeight = FontWeight.Bold, color = ObragoGreenDark) },
            text = {
                Column {
                    Text("Welcome ${user?.name ?: "User"}!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Manage your services and bookings easily.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showMenuDrawer = false; onBack() }) {
                        Icon(Icons.Outlined.Home, contentDescription = null, tint = ObragoGreenDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Go to Home Dashboard", color = Color(0xFF1F2937))
                    }
                    TextButton(onClick = { showMenuDrawer = false; showAddMoneyDialog = true }) {
                        Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = ObragoGreenDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Top Up Wallet", color = Color(0xFF1F2937))
                    }
                    TextButton(onClick = { showMenuDrawer = false; onNavigate(ProfileScreenState.HELP_SUPPORT) }) {
                        Icon(Icons.Outlined.HeadsetMic, contentDescription = null, tint = ObragoGreenDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contact Support", color = Color(0xFF1F2937))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMenuDrawer = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
            text = { Text("Are you sure you want to log out of your Obrago account?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        SessionManager.logout()
                        onLoggedOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

/**
 * 1. Personal Information Screen
 */
@Composable
private fun PersonalInformationScreenView(
    onBack: () -> Unit,
    onHomeBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val user by SessionManager.currentUser.collectAsState()
    val scrollState = rememberScrollState()
    var name by remember(user) { mutableStateOf(user?.name?.ifBlank { "User" } ?: "User") }
    var email by remember(user) { mutableStateOf(user?.email?.ifBlank { "user@example.com" } ?: "user@example.com") }
    var phone by remember(user) { mutableStateOf(user?.phone?.ifBlank { "+92 300 0000000" } ?: "+92 300 0000000") }
    var dob by remember { mutableStateOf("15 May 1997") }
    var gender by remember { mutableStateOf("Male") }
    var country by remember { mutableStateOf("Pakistan") }
    var city by remember(user) { mutableStateOf(user?.city?.ifBlank { "Lahore" } ?: "Lahore") }
    var accountType by remember(user) { mutableStateOf(if (user?.role?.uppercase() == "WORKER") "Worker" else "Customer") }

    var editingFieldLabel by remember { mutableStateOf<String?>(null) }
    var editingFieldValue by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveSelectedImageToInternalStorage(context, uri)
            if (savedPath != null) {
                user?.let { u ->
                    val updatedUser = u.copy(avatar = savedPath)
                    SessionManager.updateCurrentUser(updatedUser)
                    scope.launch { userRepository.updateUserProfile(updatedUser) }
                }
                Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        bottomBar = { ProfileBottomNavigation(onHomeBack = onHomeBack, onOpenSaved = { }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            HeaderBar(title = "Personal Information", onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            // Safe Banner
            SafetyBanner(text = "Your information is safe with us.", subText = "We never share your personal data.")

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Photo
            Text("Profile Photo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clickable {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.avatar.isNullOrBlank()) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(40.dp))
                        } else {
                            AsyncImage(model = user?.avatar, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Text(email, fontSize = 12.sp, color = Color(0xFF6B7280))
                    Text(phone, fontSize = 12.sp, color = Color(0xFF6B7280))
                }

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, ObragoGreenDark),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change Photo", fontSize = 12.sp, color = ObragoGreenDark, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Details Card
            Text("Personal Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column {
                    DetailRowItem(
                        icon = Icons.Outlined.PersonOutline,
                        label = "Full Name",
                        value = name,
                        onClick = {
                            editingFieldLabel = "Full Name"
                            editingFieldValue = name
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    DetailRowItem(
                        icon = Icons.Outlined.Mail,
                        label = "Email Address",
                        value = email,
                        isLocked = true
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    DetailRowItem(
                        icon = Icons.Outlined.Phone,
                        label = "Phone Number",
                        value = phone,
                        onClick = {
                            editingFieldLabel = "Phone Number"
                            editingFieldValue = phone
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    DetailRowItem(
                        icon = Icons.Outlined.CalendarMonth,
                        label = "Date of Birth",
                        value = dob,
                        isLocked = true
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    DetailRowItem(
                        icon = Icons.Outlined.PeopleOutline,
                        label = "Gender",
                        value = gender,
                        isLocked = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Location Details Card
            Text("Location Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column {
                    DetailRowItem(
                        icon = Icons.Outlined.LocationOn,
                        label = "Country",
                        value = country,
                        isLocked = true
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    DetailRowItem(
                        icon = Icons.Outlined.LocationOn,
                        label = "City",
                        value = city,
                        onClick = {
                            editingFieldLabel = "City"
                            editingFieldValue = city
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Account Details Card
            Text("Account Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                DetailRowItem(
                    icon = Icons.Outlined.Lock,
                    label = "Account Type",
                    value = accountType,
                    isLocked = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    user?.let { u ->
                        val updated = u.copy(name = name, phone = phone, city = city)
                        SessionManager.updateCurrentUser(updated)
                        scope.launch { userRepository.updateUserProfile(updated) }
                    }
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
            ) {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog for editing allowed profile fields
    if (editingFieldLabel != null) {
        AlertDialog(
            onDismissRequest = { editingFieldLabel = null },
            title = { Text("Edit $editingFieldLabel", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editingFieldValue,
                    onValueChange = { editingFieldValue = it },
                    label = { Text(editingFieldLabel!!) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valTrim = editingFieldValue.trim()
                        if (valTrim.isNotBlank()) {
                            when (editingFieldLabel) {
                                "Full Name" -> name = valTrim
                                "Phone Number" -> phone = valTrim
                                "City" -> city = valTrim
                            }
                        }
                        editingFieldLabel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingFieldLabel = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

/**
 * 2. Payment Methods Screen
 */
@Composable
private fun PaymentMethodsScreenView(
    onBack: () -> Unit,
    onHomeBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    data class PaymentMethodItem(
        val id: String,
        val brand: String,
        val number: String,
        val expiry: String,
        val holder: String,
        val isDefault: Boolean
    )

    val methodsList = remember {
        mutableStateListOf(
            PaymentMethodItem("1", "VISA", "Visa •••• 4242", "Expires 12/26", "Usama", true),
            PaymentMethodItem("2", "MC", "Mastercard •••• 8888", "Expires 08/25", "Usama", false),
            PaymentMethodItem("3", "EP", "Easypaisa •••• 3456", "Active Account", "Usama", false)
        )
    }

    var selectedMethodId by remember { mutableStateOf("1") }
    var showAddDialog by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf("Card") } // "Card", "Easypaisa", "JazzCash", "Bank"

    Scaffold(
        bottomBar = { ProfileBottomNavigation(onHomeBack = onHomeBack, onOpenSaved = { }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HeaderBar(title = "Payment Methods", onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            SafetyBanner(text = "Your payments are secure with Obrago.", subText = "We use industry-standard encryption to protect your data.")

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saved Payment Methods", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Text(
                    "+ Add New",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObragoGreenDark,
                    modifier = Modifier.clickable {
                        addType = "Card"
                        showAddDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            methodsList.forEach { item ->
                PaymentCardItem(
                    brand = item.brand,
                    number = item.number,
                    expiry = item.expiry,
                    holder = item.holder,
                    isDefault = item.isDefault,
                    isSelected = selectedMethodId == item.id,
                    onClick = { selectedMethodId = item.id },
                    onDelete = {
                        if (methodsList.size > 1) {
                            methodsList.remove(item)
                            Toast.makeText(context, "Payment method removed", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "At least one payment method required", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Add Payment Method", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column {
                    AddPaymentOptionRow(Icons.Outlined.CreditCard, "Add Debit / Credit Card", "Add Visa, Mastercard or other cards") {
                        addType = "Card"
                        showAddDialog = true
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    AddPaymentOptionRow(Icons.Outlined.AccountBalanceWallet, "Add Easypaisa", "Pay securely using Easypaisa") {
                        addType = "Easypaisa"
                        showAddDialog = true
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    AddPaymentOptionRow(Icons.Outlined.Payments, "Add JazzCash", "Pay securely using JazzCash") {
                        addType = "JazzCash"
                        showAddDialog = true
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    AddPaymentOptionRow(Icons.Outlined.AccountBalance, "Add Bank Account", "Add your bank account for direct payments") {
                        addType = "Bank"
                        showAddDialog = true
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 100% Secure Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0FDF4))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text("100% Secure Payments", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Your payment information is safe and will never be shared with anyone.", fontSize = 12.sp, color = Color(0xFF6B7280))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddDialog) {
        var cardHolder by remember { mutableStateOf("Usama") }
        var accountNum by remember { mutableStateOf("") }
        var expiryStr by remember { mutableStateOf("12/28") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add $addType Account", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)) },
            text = {
                Column {
                    Text("Enter your $addType account details for fast checkout.", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cardHolder,
                        onValueChange = { cardHolder = it },
                        label = { Text("Account / Card Holder Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = accountNum,
                        onValueChange = { accountNum = it },
                        label = { Text(if (addType == "Card") "Card Number (16 digits)" else "Mobile / Account Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (addType == "Card") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = expiryStr,
                            onValueChange = { expiryStr = it },
                            label = { Text("Expiry Date (MM/YY)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (accountNum.isBlank()) {
                            Toast.makeText(context, "Please enter account or card number", Toast.LENGTH_SHORT).show()
                        } else {
                            val last4 = if (accountNum.length >= 4) accountNum.takeLast(4) else accountNum
                            val brandTag = when (addType) {
                                "Easypaisa" -> "EP"
                                "JazzCash" -> "JC"
                                "Bank" -> "BANK"
                                else -> "VISA"
                            }
                            val displayNum = "$addType •••• $last4"
                            val newItem = PaymentMethodItem(
                                id = System.currentTimeMillis().toString(),
                                brand = brandTag,
                                number = displayNum,
                                expiry = if (addType == "Card") "Expires $expiryStr" else "Verified Account",
                                holder = cardHolder,
                                isDefault = false
                            )
                            methodsList.add(newItem)
                            selectedMethodId = newItem.id
                            showAddDialog = false
                            Toast.makeText(context, "$addType added successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Add Method")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

/**
 * 3. My Addresses Screen
 */
@Composable
private fun MyAddressesScreenView(
    onBack: () -> Unit,
    onHomeBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val user by SessionManager.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    data class AddressItem(
        val id: String,
        val title: String,
        val icon: ImageVector,
        val address: String,
        val cityState: String,
        val phone: String,
        var isDefault: Boolean
    )

    val addressList = remember(user) {
        val list = mutableStateListOf<AddressItem>()
        if (user != null) {
            val u = user!!
            val userAddress = u.address
            val userCity = u.city ?: ""
            val userPhone = u.phone ?: ""
            if (!userAddress.isNullOrBlank()) {
                list.add(
                    AddressItem("1", "Primary Address", Icons.Outlined.Home, userAddress, userCity, userPhone, true)
                )
            }
        }
        list
    }

    var showAddressDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<AddressItem?>(null) }


    Scaffold(
        bottomBar = { ProfileBottomNavigation(onHomeBack = onHomeBack, onOpenSaved = { }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HeaderBar(title = "My Addresses", onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            SafetyBanner(
                text = "Manage your saved addresses",
                subText = "Add, update or remove addresses for quick booking."
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saved Addresses", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Text(
                    "+ Add New Address",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObragoGreenDark,
                    modifier = Modifier.clickable {
                        editingItem = null
                        showAddressDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (addressList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.LocationOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No saved addresses yet", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1F2937))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Click '+ Add New Address' above to save your location.", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                }
            } else {
                addressList.forEach { item ->
                    AddressCardItem(
                        icon = item.icon,
                        title = item.title,
                        isDefault = item.isDefault,
                        address = item.address,
                        cityState = item.cityState,
                        phone = item.phone,
                        onSetDefault = {
                            addressList.forEach { a -> a.isDefault = (a.id == item.id) }
                            Toast.makeText(context, "${item.title} set as default address", Toast.LENGTH_SHORT).show()
                        },
                        onEdit = {
                            editingItem = item
                            showAddressDialog = true
                        },
                        onDelete = {
                            addressList.remove(item)
                            user?.let { u ->
                                val updatedUser = u.copy(address = "")
                                SessionManager.updateCurrentUser(updatedUser)
                                scope.launch { userRepository.updateUserProfile(updatedUser) }
                            }
                            Toast.makeText(context, "Address deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Add New Address Card Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0FDF4))
                    .border(1.dp, Color(0xFFD1FAE5), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Add New Address", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Add a new address to book services", fontSize = 12.sp, color = Color(0xFF6B7280))
                }

                Button(
                    onClick = {
                        editingItem = null
                        showAddressDialog = true
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Add Address", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddressDialog) {
        var titleText by remember { mutableStateOf(editingItem?.title ?: "Home") }
        var fullAddressText by remember { mutableStateOf(editingItem?.address ?: (user?.address ?: "")) }
        var cityText by remember { mutableStateOf(editingItem?.cityState ?: (user?.city ?: "")) }
        var phoneText by remember { mutableStateOf(editingItem?.phone ?: (user?.phone ?: "")) }

        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text(if (editingItem == null) "Add New Address" else "Edit Address", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Address Title (e.g. Home, Work)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fullAddressText,
                        onValueChange = { fullAddressText = it },
                        label = { Text("Full Street Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cityText,
                        onValueChange = { cityText = it },
                        label = { Text("City, Province") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        label = { Text("Contact Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullAddressText.isBlank()) {
                            Toast.makeText(context, "Please enter street address", Toast.LENGTH_SHORT).show()
                        } else {
                            if (editingItem != null) {
                                val idx = addressList.indexOfFirst { it.id == editingItem?.id }
                                if (idx != -1) {
                                    addressList[idx] = addressList[idx].copy(
                                        title = titleText,
                                        address = fullAddressText,
                                        cityState = cityText,
                                        phone = phoneText
                                    )
                                }
                                Toast.makeText(context, "Address updated!", Toast.LENGTH_SHORT).show()
                            } else {
                                val newAddress = AddressItem(
                                    id = System.currentTimeMillis().toString(),
                                    title = titleText,
                                    icon = Icons.Outlined.LocationOn,
                                    address = fullAddressText,
                                    cityState = cityText,
                                    phone = phoneText,
                                    isDefault = addressList.isEmpty()
                                )
                                addressList.add(newAddress)
                                Toast.makeText(context, "New address added!", Toast.LENGTH_SHORT).show()
                            }
                            user?.let { u ->
                                val updatedUser = u.copy(address = fullAddressText, city = cityText)
                                SessionManager.updateCurrentUser(updatedUser)
                                scope.launch { userRepository.updateUserProfile(updatedUser) }
                            }
                            showAddressDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text(if (editingItem == null) "Save Address" else "Update Address")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

/**
 * 4. Saved Workers Screen
 */
@Composable
private fun SavedWorkersScreenView(
    onBack: () -> Unit,
    onHomeBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val userRepository = remember { UserRepository() }
    val allUsers by userRepository.observeAllUsers().collectAsState(initial = emptyList())

    val realWorkers = remember(allUsers) {
        allUsers.filter { it.role == Role.WORKER.value || it.role == "worker" }
    }

    Scaffold(
        bottomBar = { ProfileBottomNavigation(onHomeBack = onHomeBack, onOpenSaved = { }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HeaderBar(title = "Saved Workers", onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            SafetyBanner(
                text = "Your Saved Workers",
                subText = "View and manage verified workers you've saved for quick repeat hiring."
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TabItemText("All (${realWorkers.size})", selectedTab == 0) { selectedTab = 0 }
                TabItemText("Recently Saved", selectedTab == 1) { selectedTab = 1 }
                TabItemText("Frequently Hired", selectedTab == 2) { selectedTab = 2 }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color(0xFFE5E7EB))

            Spacer(modifier = Modifier.height(16.dp))

            // Workers List
            if (realWorkers.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.PersonAdd, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("No Saved Workers Yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "When workers register in Obrago or you save them for fast repeat booking, they will appear here.",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onHomeBack,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                        ) {
                            Text("Find Workers on Home", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                realWorkers.forEach { workerUser ->
                    RealWorkerCardItem(worker = workerUser)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Can't find right worker banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0FDF4))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Can't find the right worker?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Post a job and get offers from verified workers.", fontSize = 11.sp, color = Color(0xFF6B7280))
                }

                Button(
                    onClick = { Toast.makeText(context, "Opening Post Job...", Toast.LENGTH_SHORT).show() },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Post a Job", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 5. Notifications Screen
 */
@Composable
private fun NotificationsScreenView(
    onBack: () -> Unit,
    onHomeBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedFilter by remember { mutableStateOf("All") }

    data class NotificationItem(
        val id: String,
        val icon: ImageVector,
        val iconBg: Color,
        val iconTint: Color,
        val title: String,
        val sub: String,
        val time: String,
        val category: String, // "Bookings", "Offers", "Messages", "System"
        val section: String,  // "Today", "Yesterday", "Earlier"
        var isUnread: Boolean
    )

    val notificationsList = remember {
        mutableStateListOf(
            NotificationItem("1", Icons.Outlined.CalendarMonth, Color(0xFFECFDF5), ObragoGreenDark, "Booking Confirmed", "Your booking for Electrician on 12 May has been confirmed.", "10:30 AM", "Bookings", "Today", true),
            NotificationItem("2", Icons.Outlined.CardGiftcard, Color(0xFFF3E8FF), Color(0xFF9333EA), "Special Offer for You! 🥳", "Get 20% OFF on plumbing services. Book now and save more!", "09:15 AM", "Offers", "Today", true),
            NotificationItem("3", Icons.Outlined.ChatBubbleOutline, Color(0xFFEFF6FF), Color(0xFF2563EB), "New Message", "Tahir Mehmood (Plumber) sent you a message.", "08:45 AM", "Messages", "Today", true),
            NotificationItem("4", Icons.Outlined.StarBorder, Color(0xFFFEF3C7), Color(0xFFD97706), "Rate Your Service", "How was your experience with Asad Hussain (Carpenter)? Rate now.", "Yesterday", "Bookings", "Today", true),
            NotificationItem("5", Icons.Outlined.CalendarMonth, Color(0xFFECFDF5), ObragoGreenDark, "Booking Completed", "Your booking for Carpenter has been completed.", "Yesterday, 06:20 PM", "Bookings", "Yesterday", false),
            NotificationItem("6", Icons.Outlined.Notifications, Color(0xFFFEE2E2), Color(0xFFEF4444), "Payment Successful", "Your payment of PKR 1,500 was successful.", "Yesterday, 05:45 PM", "Bookings", "Yesterday", false),
            NotificationItem("7", Icons.Outlined.Campaign, Color(0xFFEFF6FF), Color(0xFF3B82F6), "New Feature Alert", "We've added new services in your area. Explore now!", "Yesterday, 02:30 PM", "Offers", "Yesterday", false),
            NotificationItem("8", Icons.Outlined.VerifiedUser, Color(0xFFECFDF5), ObragoGreenDark, "Account Security", "Your password was changed successfully.", "10 May 2024", "System", "Earlier", false),
            NotificationItem("9", Icons.Outlined.Settings, Color(0xFFF3E8FF), Color(0xFF8B5CF6), "Welcome to Obrago!", "Thank you for joining Obrago. Let's get your first booking!", "08 May 2024", "System", "Earlier", false)
        )
    }

    val filteredList = notificationsList.filter {
        when (selectedFilter) {
            "Bookings" -> it.category == "Bookings"
            "Offers" -> it.category == "Offers"
            "Messages" -> it.category == "Messages"
            else -> true
        }
    }

    Scaffold(
        bottomBar = { ProfileBottomNavigation(onHomeBack = onHomeBack, onOpenSaved = { }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HeaderBar(title = "Notifications", onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            SafetyBanner(text = "Stay updated!", subText = "You'll get important updates and alerts here.")

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterPill("All ${notificationsList.size}", selectedFilter == "All") { selectedFilter = "All" }
                FilterPill("Bookings ${notificationsList.count { it.category == "Bookings" }}", selectedFilter == "Bookings") { selectedFilter = "Bookings" }
                FilterPill("Offers ${notificationsList.count { it.category == "Offers" }}", selectedFilter == "Offers") { selectedFilter = "Offers" }
                FilterPill("Messages ${notificationsList.count { it.category == "Messages" }}", selectedFilter == "Messages") { selectedFilter = "Messages" }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "Mark all as read ✓",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObragoGreenDark,
                    modifier = Modifier.clickable {
                        notificationsList.forEachIndexed { idx, item ->
                            notificationsList[idx] = item.copy(isUnread = false)
                        }
                        Toast.makeText(context, "All notifications marked as read", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            listOf("Today", "Yesterday", "Earlier").forEach { sec ->
                val secItems = filteredList.filter { it.section == sec }
                if (secItems.isNotEmpty()) {
                    Text(sec, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(8.dp))

                    secItems.forEach { item ->
                        NotificationItemRow(
                            icon = item.icon,
                            iconBg = item.iconBg,
                            iconTint = item.iconTint,
                            title = item.title,
                            sub = item.sub,
                            time = item.time,
                            isUnread = item.isUnread
                        )
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 6. Help & Support Screen
 */
@Composable
private fun HelpSupportScreenView(
    onBack: () -> Unit,
    onHomeBack: () -> Unit
) {
    val context = LocalContext.current
    val user by SessionManager.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    var showHelpDialogTitle by remember { mutableStateOf<String?>(null) }
    var showHelpDialogContent by remember { mutableStateOf<String?>(null) }
    var showLiveChat by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showReportIssueDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { ProfileBottomNavigation(onHomeBack = onHomeBack, onOpenSaved = { }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HeaderBar(title = "Help & Support", onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            // Top Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0FDF4))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.HeadsetMic, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("We're here to help you!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Find answers to your questions or contact our support team.", fontSize = 11.sp, color = Color(0xFF6B7280))
                }

                Button(
                    onClick = { showLiveChat = true },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Outlined.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Contact Support", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Help Section
            Text("Quick Help", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column {
                    SupportRowItem(Icons.Outlined.HelpOutline, "FAQs", "Find answers to common questions") {
                        showHelpDialogTitle = "Frequently Asked Questions"
                        showHelpDialogContent = "Q: How do I book a service?\nA: Simply select a category on the Home screen, specify your problem, choose location and submit. Nearby verified workers will send you offers.\n\nQ: How do payments work?\nA: You can pay using Cash, Easypaisa, JazzCash, or Debit/Credit Cards after service completion."
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    SupportRowItem(Icons.Outlined.MenuBook, "How it Works", "Learn how Obrago works") {
                        showHelpDialogTitle = "How Obrago Works"
                        showHelpDialogContent = "1. Post a job with your specific requirements.\n2. Receive live bids/offers from background-checked professionals.\n3. Compare worker ratings, prices, and accept the best offer.\n4. Track worker live on map and pay securely upon completion."
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    SupportRowItem(Icons.Outlined.Shield, "Safety & Security", "Your safety is our priority") {
                        showHelpDialogTitle = "Safety & Security"
                        showHelpDialogContent = "All Obrago professionals undergo strict identity verification, police character check, and rating screening. Every job is covered by Obrago's safety guidelines and 24/7 helpline."
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    SupportRowItem(Icons.Outlined.CreditCard, "Payments & Refunds", "Learn about payments and refunds") {
                        showHelpDialogTitle = "Payments & Refunds"
                        showHelpDialogContent = "Obrago offers a transparent pricing guarantee. If you are unsatisfied with the quality of service, notify our support within 24 hours for a full investigation and refund processing."
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    SupportRowItem(Icons.Outlined.Article, "Terms & Conditions", "Read our terms and policies") {
                        showHelpDialogTitle = "Terms & Conditions"
                        showHelpDialogContent = "By using Obrago, you agree to treat service providers respectfully, provide accurate booking addresses, and settle payments promptly upon completion of agreed work."
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    SupportRowItem(Icons.Outlined.Lock, "Privacy Policy", "How we protect your data") {
                        showHelpDialogTitle = "Privacy Policy"
                        showHelpDialogContent = "We respect your privacy. Your personal information, location coordinates, and phone numbers are encrypted and only shared with assigned service professionals during active jobs."
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Need More Help Section
            Text("Need More Help?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column {
                    // Live Chat Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLiveChat = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Chat, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Live Chat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFECFDF5))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Recommended", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Chat with our support team in real-time", fontSize = 12.sp, color = Color(0xFF6B7280))
                            Text("Available 24/7", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        }

                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                    }

                    HorizontalDivider(color = Color(0xFFF3F4F6))

                    // Email Support Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@obrago.com")
                                }
                                try { context.startActivity(intent) } catch (e: Exception) {
                                    Toast.makeText(context, "Email support@obrago.com", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Mail, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Email Support", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Send us an email and we'll get back to you", fontSize = 12.sp, color = Color(0xFF6B7280))
                            Text("support@obrago.com", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                        }

                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Other Options Section
            Text("Other Options", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+923001234567"))
                                try { context.startActivity(intent) } catch (e: Exception) {
                                    Toast.makeText(context, "Calling +92 300 1234567", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Phone, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Call Us", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            Text("Speak with our support team", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                        Text("+92 300 1234567", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                    }

                    HorizontalDivider(color = Color(0xFFF3F4F6))

                    SupportRowItem(Icons.Outlined.RateReview, "Send Feedback", "Help us improve Obrago") {
                        showFeedbackDialog = true
                    }

                    HorizontalDivider(color = Color(0xFFF3F4F6))

                    SupportRowItem(Icons.Outlined.ReportProblem, "Report an Issue", "Report a problem or issue") {
                        showReportIssueDialog = true
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Informational Help Dialog
    if (showHelpDialogTitle != null && showHelpDialogContent != null) {
        AlertDialog(
            onDismissRequest = {
                showHelpDialogTitle = null
                showHelpDialogContent = null
            },
            title = { Text(showHelpDialogTitle!!, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)) },
            text = { Text(showHelpDialogContent!!, fontSize = 13.sp, color = Color(0xFF4B5563), lineHeight = 18.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showHelpDialogTitle = null
                        showHelpDialogContent = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Got it")
                }
            }
        )
    }

    // Live Chat & Support Ticket Dialog
    if (showLiveChat) {
        val ticketRepo = remember { com.obrago.app.data.repository.SupportTicketRepository() }
        val userTickets by remember(user?.id) {
            if (user != null) ticketRepo.observeUserTickets(user!!.id) else kotlinx.coroutines.flow.flowOf(emptyList())
        }.collectAsState(initial = emptyList())

        var msgText by remember { mutableStateOf("") }
        var ticketSubject by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        val activeTicket = userTickets.firstOrNull()

        AlertDialog(
            onDismissRequest = { showLiveChat = false },
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("24/7 Live Admin Support", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    if (activeTicket != null) {
                        Surface(
                            color = if (activeTicket.status == "resolved") Color(0xFFECFDF5) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                activeTicket.status.replace("_", " ").uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTicket.status == "resolved") Color(0xFF059669) else Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.height(320.dp)) {
                    if (activeTicket == null) {
                        Text("Start a conversation with Admin Support:", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ticketSubject,
                            onValueChange = { ticketSubject = it },
                            placeholder = { Text("Subject / Issue title (e.g., Payment issue)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = msgText,
                            onValueChange = { msgText = it },
                            placeholder = { Text("Describe your problem in detail...") },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (msgText.isNotBlank() && user != null) {
                                    scope.launch {
                                        val newTicket = com.obrago.app.data.model.SupportTicket(
                                            userId = user!!.id,
                                            userName = user!!.name,
                                            userRole = user!!.role,
                                            userPhone = user!!.phone ?: "",
                                            subject = ticketSubject.ifBlank { "General Support Request" },
                                            status = "open",
                                            messages = listOf(
                                                com.obrago.app.data.model.SupportMessage(
                                                    senderId = user!!.id,
                                                    senderName = user!!.name,
                                                    text = msgText.trim(),
                                                    timestamp = System.currentTimeMillis(),
                                                    isAdmin = false
                                                )
                                            )
                                        )
                                        ticketRepo.createTicket(newTicket)
                                        msgText = ""
                                        ticketSubject = ""
                                        Toast.makeText(context, "Ticket sent to Admin Support!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = msgText.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                        ) {
                            Text("Submit Ticket to Admin")
                        }
                    } else {
                        // Live message thread with Admin
                        Text("Subject: ${activeTicket.subject}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp)).padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(activeTicket.messages) { msg ->
                                val isAdmin = msg.isAdmin
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (isAdmin) Alignment.CenterStart else Alignment.CenterEnd
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (isAdmin) Color.White else Color(0xFFDCFCE7)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(if (isAdmin) "Admin Support" else "You", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = if (isAdmin) ObragoGreenDark else Color.DarkGray)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(msg.text, fontSize = 12.sp, color = Color(0xFF1F2937))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = msgText,
                                onValueChange = { msgText = it },
                                placeholder = { Text("Reply to Admin...", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (msgText.isNotBlank() && user != null) {
                                        val replyMsg = com.obrago.app.data.model.SupportMessage(
                                            senderId = user!!.id,
                                            senderName = user!!.name,
                                            text = msgText.trim(),
                                            timestamp = System.currentTimeMillis(),
                                            isAdmin = false
                                        )
                                        scope.launch {
                                            ticketRepo.addMessageToTicket(activeTicket.id, activeTicket, replyMsg)
                                            msgText = ""
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send", tint = ObragoGreenDark)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLiveChat = false }) { Text("Close") }
            }
        )
    }

    // Feedback Dialog
    if (showFeedbackDialog) {
        var feedbackInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Send Feedback", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)) },
            text = {
                Column {
                    Text("We'd love to hear your suggestions to improve Obrago!", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = feedbackInput,
                        onValueChange = { feedbackInput = it },
                        label = { Text("Your Feedback") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (feedbackInput.isBlank()) {
                            Toast.makeText(context, "Please enter your feedback", Toast.LENGTH_SHORT).show()
                        } else {
                            user?.let { u ->
                                val feedbackDoc = mapOf(
                                    "userId" to u.id,
                                    "userName" to u.name,
                                    "feedback" to feedbackInput,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("feedback").add(feedbackDoc)
                            }
                            showFeedbackDialog = false
                            Toast.makeText(context, "Feedback sent to Admin!", Toast.LENGTH_SHORT).show()
                        }

                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Report Issue Dialog
    if (showReportIssueDialog) {
        var issueInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportIssueDialog = false },
            title = { Text("Report an Issue", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)) },
            text = {
                Column {
                    Text("Describe any bug or issue you encountered:", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = issueInput,
                        onValueChange = { issueInput = it },
                        label = { Text("Issue details") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (issueInput.isBlank()) {
                            Toast.makeText(context, "Please describe the issue", Toast.LENGTH_SHORT).show()
                        } else {
                            user?.let { u ->
                                val issueDoc = mapOf(
                                    "userId" to u.id,
                                    "userName" to u.name,
                                    "type" to "report_issue",
                                    "message" to issueInput,
                                    "timestamp" to System.currentTimeMillis(),
                                    "status" to "open"
                                )
                                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("support_tickets").add(issueDoc)
                            }
                            showReportIssueDialog = false
                            Toast.makeText(context, "Issue reported to support team & saved to Admin!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Report Issue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportIssueDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

// --- HELPER COMPONENTS ---

@Composable
private fun BookingStatItem(
    icon: ImageVector,
    count: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
        Text(label, fontSize = 11.sp, color = Color(0xFF6B7280))
    }
}

@Composable
private fun AccountRow(
    title: String,
    icon: ImageVector,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937),
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(start = 52.dp), color = Color(0xFFF3F4F6))
        }
    }
}

@Composable
private fun HeaderBar(title: String, onBack: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = ObragoGreenDark, modifier = Modifier.size(24.dp))
        }

        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))

        IconButton(onClick = { Toast.makeText(context, "Notifications", Toast.LENGTH_SHORT).show() }) {
            Box {
                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(26.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-1).dp)
                        .clip(CircleShape)
                        .background(ObragoGreenDark)
                )
            }
        }
    }
}

@Composable
private fun SafetyBanner(text: String, subText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0FDF4))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subText, fontSize = 12.sp, color = Color(0xFF6B7280))
        }
    }
}

private fun saveSelectedImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, "profile_avatar_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
private fun DetailRowItem(
    icon: ImageVector,
    label: String,
    value: String,
    isLocked: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val rowModifier = if (onClick != null && !isLocked) {
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    } else if (isLocked) {
        Modifier
            .fillMaxWidth()
            .clickable { Toast.makeText(context, "$label is locked for security", Toast.LENGTH_SHORT).show() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, fontSize = 14.sp, color = Color(0xFF6B7280), modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937))
        Spacer(modifier = Modifier.width(8.dp))
        if (isLocked) {
            Icon(Icons.Outlined.Lock, contentDescription = "Locked", tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
        } else if (onClick != null) {
            Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = ObragoGreenDark, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun PaymentCardItem(
    brand: String,
    number: String,
    expiry: String,
    holder: String,
    isDefault: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isSelected) ObragoGreenDark else Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp, 36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Text(brand, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(number, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    if (isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Default", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    if (expiry.isNotBlank()) {
                        Text(expiry, fontSize = 12.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(holder, fontSize = 12.sp, color = Color(0xFF6B7280))
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = ObragoGreenDark)
            )
        }
    }
}

@Composable
private fun AddPaymentOptionRow(icon: ImageVector, title: String, sub: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFECFDF5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(2.dp))
            Text(sub, fontSize = 12.sp, color = Color(0xFF6B7280))
        }

        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun AddressCardItem(
    icon: ImageVector,
    title: String,
    isDefault: Boolean,
    address: String,
    cityState: String,
    phone: String,
    onSetDefault: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isDefault) Color(0xFFA7F3D0) else Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECFDF5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))

                if (isDefault) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFECFDF5))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Default", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Set Default",
                        fontSize = 11.sp,
                        color = ObragoGreenDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSetDefault() }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete Address", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(address, fontSize = 13.sp, color = Color(0xFF4B5563), lineHeight = 18.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(cityState, fontSize = 12.sp, color = Color(0xFF6B7280))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(phone, fontSize = 12.sp, color = Color(0xFF6B7280))
                }

                Text(
                    "✏️ Edit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObragoGreenDark,
                    modifier = Modifier.clickable { onEdit() }
                )
            }
        }
    }
}

private data class SavedWorkerData(
    val name: String,
    val category: String,
    val rating: String,
    val reviews: String,
    val location: String,
    val price: String
)

@Composable
private fun WorkerCardItem(worker: SavedWorkerData) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(60.dp)) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                }
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(ObragoGreenDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(worker.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFECFDF5))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Verified", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(worker.category, fontSize = 12.sp, color = Color(0xFF4B5563))
                Text("⭐ ${worker.rating} (${worker.reviews} reviews)", fontSize = 11.sp, color = Color(0xFF6B7280))
                Text("📍 ${worker.location}", fontSize = 11.sp, color = Color(0xFF6B7280))
            }

            Column(horizontalAlignment = Alignment.End) {
                Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = Color(0xFF9CA3AF))
                Spacer(modifier = Modifier.height(8.dp))
                Text("PKR ${worker.price} /hr", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { Toast.makeText(context, "Messaging ${worker.name}", Toast.LENGTH_SHORT).show() },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, ObragoGreenDark),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Outlined.Chat, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Message", fontSize = 11.sp, color = ObragoGreenDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NotificationItemRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    sub: String,
    time: String,
    isUnread: Boolean
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { Toast.makeText(context, title, Toast.LENGTH_SHORT).show() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (isUnread) {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, end = 6.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(ObragoGreenDark)
            )
        } else {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, end = 6.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1D5DB))
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Text(time, fontSize = 11.sp, color = Color(0xFF9CA3AF))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(sub, fontSize = 12.sp, color = Color(0xFF6B7280), lineHeight = 16.sp)
        }

        Spacer(modifier = Modifier.width(6.dp))

        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SupportRowItem(icon: ImageVector, title: String, sub: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(2.dp))
            Text(sub, fontSize = 12.sp, color = Color(0xFF6B7280))
        }

        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TabItemText(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) ObragoGreenDark else Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(ObragoGreenDark)
            )
        }
    }
}

@Composable
private fun FilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) ObragoGreenDark else Color(0xFFF3F4F6))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF4B5563)
        )
    }
}

@Composable
private fun RealWorkerCardItem(worker: User) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(56.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECFDF5)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!worker.avatar.isNullOrBlank()) {
                        AsyncImage(model = worker.avatar, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(32.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(ObragoGreenDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(worker.name.ifBlank { "Registered Worker" }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFECFDF5))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Verified", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(worker.phone?.ifBlank { "Verified Provider" } ?: "Verified Provider", fontSize = 12.sp, color = Color(0xFF4B5563))
                Text("⭐ ${String.format("%.1f", worker.rating)} (${worker.completedJobs} jobs)", fontSize = 11.sp, color = Color(0xFF6B7280))
                Text("📍 ${worker.city ?: "Pakistan"}", fontSize = 11.sp, color = Color(0xFF6B7280))
            }

            Column(horizontalAlignment = Alignment.End) {
                OutlinedButton(
                    onClick = { Toast.makeText(context, "Messaging ${worker.name}", Toast.LENGTH_SHORT).show() },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, ObragoGreenDark),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Outlined.Chat, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Message", fontSize = 11.sp, color = ObragoGreenDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 7. Settings Screen
 */
@Composable
private fun SettingsScreenView(
    onBack: () -> Unit,
    onHomeBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val currentUser by SessionManager.currentUser.collectAsState()

    val currentLanguage by AppSettingsManager.currentLanguage.collectAsState()
    val isDarkMode by AppSettingsManager.isDarkMode.collectAsState()
    val currentCurrency by AppSettingsManager.currentCurrency.collectAsState()
    val pushNotifications by AppSettingsManager.pushNotificationsEnabled.collectAsState()
    val smsAlerts by AppSettingsManager.smsAlertsEnabled.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { ProfileBottomNavigation(onHomeBack = onHomeBack, onOpenSaved = { }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkMode) Color(0xFF0B0F19) else Color(0xFFFAFAFA))
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HeaderBar(title = AppSettingsManager.tr("settings"), onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            SafetyBanner(
                text = "App Settings & Preferences",
                subText = "Manage language, theme, currency, notifications and security preferences."
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. APP PREFERENCES
            Text("App Preferences", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF111827) else Color.White),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF1F2937) else Color(0xFFE5E7EB))
            ) {
                Column {
                    // Language Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLanguageDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Language, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(AppSettingsManager.tr("app_language"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1F2937))
                            Text("${currentLanguage.flag} ${currentLanguage.displayName}", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(currentLanguage.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                        }
                    }

                    HorizontalDivider(color = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFF3F4F6))

                    // Dark Mode Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(AppSettingsManager.tr("dark_theme"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1F2937))
                            Text(if (isDarkMode) "Dark theme enabled" else "Light theme enabled", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { enabled ->
                                AppSettingsManager.setDarkMode(enabled)
                                Toast.makeText(context, if (enabled) "Dark Theme Activated" else "Light Theme Activated", Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ObragoGreenDark)
                        )
                    }

                    HorizontalDivider(color = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFF3F4F6))

                    // Currency Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCurrencyDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Payments, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(AppSettingsManager.tr("default_currency"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1F2937))
                            Text("${currentCurrency.name} (${currentCurrency.symbol})", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${currentCurrency.code} ${currentCurrency.symbol}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. NOTIFICATIONS & ALERTS
            Text("Notification Preferences", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF111827) else Color.White),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF1F2937) else Color(0xFFE5E7EB))
            ) {
                Column {
                    SettingToggleRow("Push Notifications", "Receive push alerts for orders & messages", pushNotifications, isDarkMode) {
                        AppSettingsManager.setPushNotifications(it)
                    }
                    HorizontalDivider(color = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFF3F4F6))
                    SettingToggleRow("SMS Notifications", "Receive text alerts for booking status", smsAlerts, isDarkMode) {
                        AppSettingsManager.setSmsAlerts(it)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. SECURITY & PRIVACY
            Text("Security & Account", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF111827) else Color.White),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF1F2937) else Color(0xFFE5E7EB))
            ) {
                Column {
                    // Change Password
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showChangePasswordDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Change Password", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1F2937))
                            Text("Update account login password securely", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. STORAGE & SYSTEM
            Text("Storage & System", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF111827) else Color.White),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF1F2937) else Color(0xFFE5E7EB))
            ) {
                Column {
                    // Clear Cache Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.CleaningServices, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Clear Cache Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1F2937))
                            Text("Frees up local storage (14.2 MB)", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "Cache cleared successfully!", Toast.LENGTH_SHORT).show() },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, ObragoGreenDark),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Clear", fontSize = 12.sp, color = ObragoGreenDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFF3F4F6))

                    // App Version Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Obrago Version", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1F2937))
                            Text("v2.4.0 (Latest Version)", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Up to date", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. DANGER ZONE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteAccountDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Delete Account", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        Text("Permanently remove your Obrago profile and data", fontSize = 12.sp, color = Color(0xFF991B1B))
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 🌐 Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select App Language", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = currentLanguage == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFECFDF5) else Color(0xFFF9FAFB))
                                .clickable {
                                    AppSettingsManager.setLanguage(lang)
                                    showLanguageDialog = false
                                    Toast.makeText(context, "Language changed to ${lang.displayName}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang.flag, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                lang.displayName,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ObragoGreenDark else Color(0xFF1F2937),
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text("✓", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = ObragoGreenDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 💵 Currency Selection Dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppSettingsManager.AVAILABLE_CURRENCIES.forEach { curr ->
                        val isSelected = currentCurrency.code == curr.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFECFDF5) else Color(0xFFF9FAFB))
                                .clickable {
                                    AppSettingsManager.setCurrency(curr)
                                    showCurrencyDialog = false
                                    Toast.makeText(context, "Currency set to ${curr.name}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(curr.symbol, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                curr.name,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ObragoGreenDark else Color(0xFF1F2937),
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text("✓", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close", color = ObragoGreenDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 🔒 Change Password Dialog
    if (showChangePasswordDialog) {
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)) },
            text = {
                Column {
                    Text("Enter your current password and a new secure password.", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPass.length < 6) {
                            Toast.makeText(context, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show()
                        } else if (newPass != confirmPass) {
                            Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                        } else {
                            currentUser?.let { user ->
                                SessionManager.updateCurrentUser(user.copy(password = newPass))
                            }
                            showChangePasswordDialog = false
                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Update Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // ⚠️ Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone and all your bookings and wallet history will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        SessionManager.logout()
                        Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                        onHomeBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}


@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    isDarkMode: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF6B7280))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ObragoGreenDark)
        )
    }
}
