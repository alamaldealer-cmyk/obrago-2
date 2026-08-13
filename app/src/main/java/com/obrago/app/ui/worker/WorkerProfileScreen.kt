package com.obrago.app.ui.worker

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.obrago.app.data.model.AppData
import com.obrago.app.data.model.User
import com.obrago.app.data.repository.SessionManager
import com.obrago.app.data.repository.UserRepository
import com.obrago.app.ui.theme.ObragoGreenDark
import kotlinx.coroutines.launch

@Composable
fun WorkerProfileScreen(
    currentUser: User?,
    totalEarnings: Double,
    completedJobsCount: Int,
    currency: String = "PKR",
    onOpenWallet: () -> Unit,
    onOpenSupport: () -> Unit,
    onLoggedOut: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }

    var serviceRadiusKm by remember { mutableFloatStateOf(15f) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showBankDetailsDialog by remember { mutableStateOf(false) }

    // Editable profile state
    var editName by remember { mutableStateOf(currentUser?.name ?: "") }
    var editPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var editCity by remember { mutableStateOf(currentUser?.city ?: "Lahore") }
    var editCategory by remember { mutableStateOf("Electrician & Specialist") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Top Header
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Color(0xFF1F2937))
                    }
                    Text("Worker Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                }

                TextButton(onClick = { showEditProfileDialog = true }) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = ObragoGreenDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 13.sp, color = ObragoGreenDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = currentUser?.avatar?.takeIf { it.isNotBlank() }
                                    ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=worker",
                                contentDescription = "Worker Avatar",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, ObragoGreenDark, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(ObragoGreenDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Verified", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentUser?.name ?: "Worker Professional",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )

                        Text(
                            text = "Service Professional • ${currentUser?.city ?: "Lahore"}",
                            fontSize = 13.sp,
                            color = ObragoGreenDark,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentUser?.rating ?: 5.0} ($completedJobsCount Completed Jobs)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
            }

            // Wallet & Points Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWallet() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF042817))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Bidding Points Wallet", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                            Text("${currentUser?.points ?: 0} Pts", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            Text("Total Earnings: $currency ${totalEarnings.toInt()}", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onOpenWallet,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                        ) {
                            Text("Manage Wallet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Verification & CNIC Status Section
            item {
                Text("Identity & Documents Verification", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F2937))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        VerificationRow(icon = Icons.Outlined.Badge, title = "CNIC Identity Card", subtitle = "35202-*******-1", status = "Verified ✓", isVerified = true)
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        VerificationRow(icon = Icons.Outlined.Security, title = "Police Character Certificate", subtitle = "Issued by Punjab Police", status = "Verified ✓", isVerified = true)
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        VerificationRow(icon = Icons.Outlined.Handyman, title = "Skill & Vocational License", subtitle = "Electrician / Technical License", status = "Uploaded", isVerified = false)
                    }
                }
            }

            // Service Radius Setting
            item {
                Text("Service Area & Distance", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F2937))
            }

            item {
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
                            Text("Service Distance Radius", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1F2937))
                            Text("${serviceRadiusKm.toInt()} km", fontWeight = FontWeight.Black, color = ObragoGreenDark, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = serviceRadiusKm,
                            onValueChange = { serviceRadiusKm = it },
                            valueRange = 5f..50f,
                            colors = SliderDefaults.colors(
                                thumbColor = ObragoGreenDark,
                                activeTrackColor = ObragoGreenDark
                            )
                        )
                        Text(
                            "You will receive job alerts within ${serviceRadiusKm.toInt()} km of your location.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Settings & Actions
            item {
                Text("Account & Payouts", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F2937))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ProfileSettingRow(icon = Icons.Outlined.AccountBalance, title = "Payout Bank / Easypaisa Account", subtitle = "Linked: 0300****567", onClick = { showBankDetailsDialog = true })
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        ProfileSettingRow(icon = Icons.Outlined.HelpOutline, title = "Help & Support 24/7", subtitle = "Contact Obrago Support Team", onClick = onOpenSupport)
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        ProfileSettingRow(icon = Icons.Outlined.PrivacyTip, title = "Terms of Service & Privacy Policy", subtitle = "Worker Guidelines", onClick = {
                            Toast.makeText(context, "Obrago Worker Terms & Safety Guidelines", Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            }

            // Logout Button
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            SessionManager.logout()
                            onLoggedOut()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFEF4444)),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout Worker Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Worker Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCity,
                        onValueChange = { editCity = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = currentUser
                        if (user != null) {
                            coroutineScope.launch {
                                val updated = user.copy(name = editName, phone = editPhone, city = editCity)
                                userRepository.updateUserProfile(updated)
                                SessionManager.updateCurrentUser(updated)
                                Toast.makeText(context, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                                showEditProfileDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Bank Details Dialog
    if (showBankDetailsDialog) {
        var bankName by remember { mutableStateOf("Easypaisa / JazzCash") }
        var accountTitle by remember { mutableStateOf(currentUser?.name ?: "") }
        var accountNumber by remember { mutableStateOf("03001234567") }

        AlertDialog(
            onDismissRequest = { showBankDetailsDialog = false },
            title = { Text("Payout Account Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Bank / Wallet Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = accountTitle, onValueChange = { accountTitle = it }, label = { Text("Account Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = accountNumber, onValueChange = { accountNumber = it }, label = { Text("Account / Phone Number") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Payout Account Updated!", Toast.LENGTH_SHORT).show()
                        showBankDetailsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Save Account", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBankDetailsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun VerificationRow(icon: ImageVector, title: String, subtitle: String, status: String, isVerified: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isVerified) Color(0xFFECFDF5) else Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (isVerified) ObragoGreenDark else Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1F2937))
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }

        Surface(
            color = if (isVerified) Color(0xFFECFDF5) else Color(0xFFEFF6FF),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isVerified) ObragoGreenDark else Color(0xFF3B82F6),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ProfileSettingRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4B5563), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1F2937))
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}
