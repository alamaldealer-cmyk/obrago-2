package com.obrago.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.obrago.app.data.model.User

@Composable
fun AdminUsersScreen(
    users: List<User>,
    searchTerm: String,
    currency: String = "Rs.",
    onSearchChange: (String) -> Unit,
    onToggleBlock: (User) -> Unit,
    onDelete: (String) -> Unit,
    onRefundPenalty: (String) -> Unit
) {
    var confirmDeleteFor by remember { mutableStateOf<User?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Manage Users", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = searchTerm,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by name, phone or email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users) { user ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = user.avatar.ifBlank { "https://api.dicebear.com/7.x/avataaars/svg?seed=${user.id}" },
                                contentDescription = user.name,
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("${user.role} • ${user.phone ?: user.email ?: ""}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Surface(
                            color = if (user.isBlocked == true) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                if (user.isBlocked == true) "Blocked" else "Active",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if ((user.penaltyFee ?: 0.0) > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Outstanding penalty: $currency${user.penaltyFee}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onToggleBlock(user) }, modifier = Modifier.weight(1f)) {
                            Text(if (user.isBlocked == true) "Unblock" else "Block", style = MaterialTheme.typography.labelSmall)
                        }
                        if ((user.penaltyFee ?: 0.0) > 0) {
                            OutlinedButton(onClick = { onRefundPenalty(user.id) }, modifier = Modifier.weight(1f)) {
                                Text("Refund Penalty", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        OutlinedButton(onClick = { confirmDeleteFor = user }, modifier = Modifier.weight(1f)) {
                            Text("Delete", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }

    confirmDeleteFor?.let { user ->
        AlertDialog(
            onDismissRequest = { confirmDeleteFor = null },
            title = { Text("Delete Account") },
            text = { Text("Permanently delete ${user.name}'s account? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete(user.id); confirmDeleteFor = null }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteFor = null }) { Text("Cancel") }
            }
        )
    }
}
