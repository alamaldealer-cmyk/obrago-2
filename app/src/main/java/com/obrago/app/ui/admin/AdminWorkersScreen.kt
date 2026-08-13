package com.obrago.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.obrago.app.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWorkersScreen(
    workers: List<User>,
    searchTerm: String,
    currency: String = "PKR ",
    onSearchChange: (String) -> Unit,
    onVerifyWorker: (String, Boolean) -> Unit,
    onToggleBlock: (User) -> Unit,
    onAdjustPoints: (String, Long, Long) -> Unit,
    onDelete: (String) -> Unit
) {
    var selectedWorkerDetails by remember { mutableStateOf<User?>(null) }
    var adjustPointsForWorker by remember { mutableStateOf<User?>(null) }
    var pointsDeltaText by remember { mutableStateOf("100") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Workers Directory (${workers.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchTerm,
            onValueChange = onSearchChange,
            placeholder = { Text("Search worker name, phone, city...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (workers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No workers found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(workers) { worker ->
                    val status = worker.verificationStatus ?: "pending"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = worker.avatar.ifBlank { "https://api.dicebear.com/7.x/avataaars/svg?seed=${worker.id}" },
                                        contentDescription = worker.name,
                                        modifier = Modifier.size(44.dp).clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(worker.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "Location: ${worker.city ?: worker.country ?: "Pakistan"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text("${worker.phone ?: worker.email ?: "No Contact"}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }

                                Surface(
                                    color = if (worker.isBlocked == true) Color(0xFFFEF2F2) else if (status == "verified") Color(0xFFECFDF5) else Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = if (worker.isBlocked == true) "Blocked" else if (status == "verified") "Verified" else "Pending",
                                        color = if (worker.isBlocked == true) Color(0xFFDC2626) else if (status == "verified") Color(0xFF059669) else Color(0xFFD97706),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${worker.rating}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Jobs: ${worker.completedJobs}", fontSize = 12.sp, color = Color.Gray)
                                }

                                Text("Points: ${worker.points}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { selectedWorkerDetails = worker },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Details", fontSize = 10.sp)
                                }

                                OutlinedButton(
                                    onClick = { adjustPointsForWorker = worker },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Points", fontSize = 10.sp)
                                }

                                OutlinedButton(
                                    onClick = { onToggleBlock(worker) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (worker.isBlocked == true) "Unblock" else "Block", fontSize = 10.sp)
                                }

                                OutlinedButton(
                                    onClick = { onDelete(worker.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Delete", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedWorkerDetails?.let { worker ->
        AlertDialog(
            onDismissRequest = { selectedWorkerDetails = null },
            title = { Text("Worker Profile: ${worker.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("City: ${worker.city ?: "N/A"}")
                    Text("Phone: ${worker.phone ?: "N/A"}")
                    Text("Email: ${worker.email ?: "N/A"}")
                    Text("CNIC: ${worker.cnic ?: "N/A"}")
                    Text("Verification: ${(worker.verificationStatus ?: "pending").uppercase()}")
                    Text("Completed Jobs: ${worker.completedJobs}")
                    Text("Wallet Points: ${worker.points}")
                    Text("Rating: ${worker.rating} ★")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedWorkerDetails = null }) {
                    Text("Close")
                }
            }
        )
    }

    adjustPointsForWorker?.let { worker ->
        AlertDialog(
            onDismissRequest = { adjustPointsForWorker = null },
            title = { Text("Adjust Wallet Points for ${worker.name}") },
            text = {
                Column {
                    Text("Current Points: ${worker.points}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pointsDeltaText,
                        onValueChange = { pointsDeltaText = it },
                        label = { Text("Points Amount") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        val amount = pointsDeltaText.toLongOrNull() ?: 0L
                        onAdjustPoints(worker.id, worker.points, amount)
                        adjustPointsForWorker = null
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Add")
                    }
                    TextButton(onClick = {
                        val amount = pointsDeltaText.toLongOrNull() ?: 0L
                        onAdjustPoints(worker.id, worker.points, -amount)
                        adjustPointsForWorker = null
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Deduct")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { adjustPointsForWorker = null }) { Text("Cancel") }
            }
        )
    }
}
