package com.obrago.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
fun AdminCustomersScreen(
    customers: List<User>,
    searchTerm: String,
    currency: String = "PKR ",
    onSearchChange: (String) -> Unit,
    onToggleBlock: (User) -> Unit,
    onRefundPenalty: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var selectedCustomerDetails by remember { mutableStateOf<User?>(null) }
    var confirmDeleteFor by remember { mutableStateOf<User?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Customers Directory (${customers.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchTerm,
            onValueChange = onSearchChange,
            placeholder = { Text("Search customer name, phone, email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (customers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No customers found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(customers) { customer ->
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
                                        model = customer.avatar.ifBlank { "https://api.dicebear.com/7.x/avataaars/svg?seed=${customer.id}" },
                                        contentDescription = customer.name,
                                        modifier = Modifier.size(44.dp).clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(customer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("${customer.phone ?: customer.email ?: "No Contact"}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }

                                Surface(
                                    color = if (customer.isBlocked == true) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = if (customer.isBlocked == true) "Blocked" else "Active",
                                        color = if (customer.isBlocked == true) Color(0xFFDC2626) else Color(0xFF059669),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if ((customer.penaltyFee ?: 0.0) > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Outstanding penalty: $currency${customer.penaltyFee}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { selectedCustomerDetails = customer },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Details", fontSize = 10.sp)
                                }

                                if ((customer.penaltyFee ?: 0.0) > 0) {
                                    OutlinedButton(
                                        onClick = { onRefundPenalty(customer.id) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Refund Penalty", fontSize = 10.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { onToggleBlock(customer) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (customer.isBlocked == true) "Unblock" else "Block", fontSize = 10.sp)
                                }

                                OutlinedButton(
                                    onClick = { confirmDeleteFor = customer },
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

    selectedCustomerDetails?.let { customer ->
        AlertDialog(
            onDismissRequest = { selectedCustomerDetails = null },
            title = { Text("Customer Profile: ${customer.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Name: ${customer.name}")
                    Text("Phone: ${customer.phone ?: "N/A"}")
                    Text("Email: ${customer.email ?: "N/A"}")
                    Text("Role: Customer")
                    Text("Penalty Fee: $currency${customer.penaltyFee ?: 0.0}")
                    Text("Status: ${if (customer.isBlocked == true) "BLOCKED" else "ACTIVE"}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCustomerDetails = null }) { Text("Close") }
            }
        )
    }

    confirmDeleteFor?.let { customer ->
        AlertDialog(
            onDismissRequest = { confirmDeleteFor = null },
            title = { Text("Delete Customer") },
            text = { Text("Permanently delete ${customer.name}'s account? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete(customer.id); confirmDeleteFor = null }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteFor = null }) { Text("Cancel") }
            }
        )
    }
}
