package com.obrago.app.ui.worker

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrago.app.data.model.User
import com.obrago.app.data.repository.SessionManager
import com.obrago.app.data.repository.UserRepository
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoInput
import com.obrago.app.ui.theme.ObragoGreenDark
import kotlinx.coroutines.launch

@Composable
fun WorkerWalletScreen(
    currentUser: User?,
    totalEarnings: Double,
    completedJobsCount: Int,
    currency: String = "PKR",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }

    var showTopUpDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    // Dummy transaction history
    val transactions = remember {
        listOf(
            Triple("Points Top Up", "+100 Pts (JazzCash)", "- PKR 500"),
            Triple("Bid Fee Deducted", "Job #4982 Offer", "- 25 Pts"),
            Triple("Job Earnings", "AC Repair Completed", "+ PKR 3,500"),
            Triple("Bid Fee Deducted", "Job #3120 Offer", "- 15 Pts"),
            Triple("Points Top Up", "+200 Pts (Easypaisa)", "- PKR 1,000")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Header Bar
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Color(0xFF1F2937))
                }
                Text(
                    text = "Earnings & Wallet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Dark Card: Total Earnings & Points
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF01170D)),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Available Points Balance", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${currentUser?.points ?: 0} Pts",
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Button(
                                onClick = { showTopUpDialog = true },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Top Up", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color(0xFF06381E))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Earnings", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                                Text("$currency ${totalEarnings.toInt()}", color = Color(0xFF34D399), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Jobs Completed", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                                Text("$completedJobsCount Jobs", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Status", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                                Text("Verified Pro ✓", color = Color(0xFF60A5FA), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Actions: Top Up Points & Request Payout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTopUpDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFECFDF5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.AddCard, contentDescription = null, tint = ObragoGreenDark)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Buy Points", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1F2937))
                                Text("Required for bids", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showWithdrawDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Payments, contentDescription = null, tint = Color(0xFF3B82F6))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Withdraw", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1F2937))
                                Text("Bank / JazzCash", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Bidding Points System Notice
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("How Points Work in Obrago", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF92400E))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "5% of job budget in points is deducted when submitting an offer. Example: For PKR 1,000 job, 50 points are used.",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            // Recent Wallet Transactions
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Recent Wallet Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
            }

            items(transactions) { (title, subtitle, amount) ->
                val isPositive = amount.startsWith("+")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isPositive) Color(0xFFECFDF5) else Color(0xFFFEF2F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPositive) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isPositive) ObragoGreenDark else Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1F2937))
                                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Text(
                            text = amount,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isPositive) ObragoGreenDark else Color(0xFFEF4444)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Top-Up Points Dialog
    if (showTopUpDialog) {
        var selectedPack by remember { mutableStateOf(100) } // 100 Pts = PKR 500
        var paymentMethod by remember { mutableStateOf("Easypaisa") }

        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = { Text("Buy Obrago Points", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Points Package:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    listOf(
                        50 to "50 Points (PKR 250)",
                        100 to "100 Points (PKR 500) - Most Popular",
                        250 to "250 Points (PKR 1,200) - Best Value"
                    ).forEach { (pts, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedPack == pts) Color(0xFFECFDF5) else Color(0xFFF9FAFB))
                                .border(1.dp, if (selectedPack == pts) ObragoGreenDark else Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                .clickable { selectedPack = pts }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedPack == pts, onClick = { selectedPack = pts })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1F2937))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Payment Method:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    listOf("Easypaisa", "JazzCash", "Debit/Credit Card").forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = method }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = paymentMethod == method, onClick = { paymentMethod = method })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(method, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = currentUser
                        if (user != null) {
                            coroutineScope.launch {
                                userRepository.adjustPoints(user.id, selectedPack.toLong())
                                SessionManager.updateCurrentUser(user.copy(points = user.points + selectedPack))
                                Toast.makeText(context, "$selectedPack Points Added via $paymentMethod!", Toast.LENGTH_LONG).show()
                                showTopUpDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Pay & Add Points", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopUpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Withdraw Earnings Dialog
    if (showWithdrawDialog) {
        var withdrawAmount by remember { mutableStateOf("1000") }
        var withdrawAccount by remember { mutableStateOf("03001234567") }

        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Withdraw Earnings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter amount to transfer to your linked account:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = { Text("Amount ($currency)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = withdrawAccount,
                        onValueChange = { withdrawAccount = it },
                        label = { Text("Easypaisa / JazzCash / Bank Account") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Withdrawal request of $currency $withdrawAmount submitted successfully!", Toast.LENGTH_LONG).show()
                        showWithdrawDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Confirm Payout", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
