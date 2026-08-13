package com.obrago.app.ui.admin

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.obrago.app.data.model.SupportMessage
import com.obrago.app.data.model.SupportTicket
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportScreen(
    tickets: List<SupportTicket>,
    onReplyToTicket: (String, SupportTicket, String) -> Unit,
    onUpdateStatus: (String, String) -> Unit
) {
    val context = LocalContext.current
    var activeFilter by remember { mutableStateOf("All") }
    var searchTerm by remember { mutableStateOf("") }
    var selectedTicket by remember { mutableStateOf<SupportTicket?>(null) }
    var replyText by remember { mutableStateOf("") }

    val filteredTickets = remember(tickets, activeFilter, searchTerm) {
        tickets.filter { ticket ->
            val matchesFilter = when (activeFilter) {
                "Open" -> ticket.status == "open"
                "In Progress" -> ticket.status == "in_progress"
                "Resolved" -> ticket.status == "resolved"
                else -> true
            }
            val matchesSearch = if (searchTerm.isBlank()) true else {
                ticket.userName.contains(searchTerm, ignoreCase = true) ||
                    ticket.userPhone.contains(searchTerm) ||
                    ticket.subject.contains(searchTerm, ignoreCase = true) ||
                    ticket.messages.any { it.text.contains(searchTerm, ignoreCase = true) }
            }
            matchesFilter && matchesSearch
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Support & Contact Tickets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text("${filteredTickets.size} messages / tickets", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchTerm,
            onValueChange = { searchTerm = it },
            placeholder = { Text("Search name, phone, message subject...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Open", "In Progress", "Resolved").forEach { filter ->
                FilterChip(
                    selected = activeFilter == filter,
                    onClick = { activeFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredTickets.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No support tickets found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTickets) { ticket ->
                    val lastMsg = ticket.messages.lastOrNull()?.text ?: "No messages yet"
                    val formattedTime = remember(ticket.updatedAt) {
                        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(ticket.updatedAt))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTicket = ticket },
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
                                    Surface(
                                        color = if (ticket.userRole == "worker") Color(0xFFEFF6FF) else Color(0xFFECFDF5),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            ticket.userRole.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ticket.userRole == "worker") Color(0xFF2563EB) else Color(0xFF059669),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(ticket.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }

                                StatusBadge(ticket.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Subject: ${ticket.subject}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(lastMsg, fontSize = 12.sp, color = Color.Gray, maxLines = 2)

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(formattedTime, fontSize = 11.sp, color = Color.Gray)

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Call Button
                                    if (ticket.userPhone.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ticket.userPhone}"))
                                                try { context.startActivity(intent) } catch (e: Exception) {
                                                    Toast.makeText(context, "Call ${ticket.userPhone}", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFECFDF5))
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                        }

                                        // WhatsApp Button
                                        IconButton(
                                            onClick = {
                                                val cleanPhone = ticket.userPhone.replace("+", "").replace(" ", "")
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone"))
                                                try { context.startActivity(intent) } catch (e: Exception) {
                                                    Toast.makeText(context, "WhatsApp ${ticket.userPhone}", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFEFF6FF))
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Button(
                                        onClick = { selectedTicket = ticket },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Reply", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Chat / Reply Dialog
    selectedTicket?.let { ticket ->
        AlertDialog(
            onDismissRequest = { selectedTicket = null },
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ticket: ${ticket.subject}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        StatusBadge(ticket.status)
                    }
                    Text("User: ${ticket.userName} (${ticket.userRole}) • Phone: ${ticket.userPhone.ifBlank { "N/A" }}", fontSize = 12.sp, color = Color.Gray)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ticket.messages) { msg ->
                            val isAdminMsg = msg.isAdmin
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isAdminMsg) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Surface(
                                    color = if (isAdminMsg) MaterialTheme.colorScheme.primaryContainer else Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    tonalElevation = 1.dp
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            if (isAdminMsg) "Admin (You)" else msg.senderName.ifBlank { ticket.userName },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isAdminMsg) MaterialTheme.colorScheme.onPrimaryContainer else Color.DarkGray
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(msg.text, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AssistChip(
                                onClick = { onUpdateStatus(ticket.id, "in_progress") },
                                label = { Text("In Progress", fontSize = 10.sp) },
                                leadingIcon = { if (ticket.status == "in_progress") Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                            AssistChip(
                                onClick = { onUpdateStatus(ticket.id, "resolved") },
                                label = { Text("Resolved", fontSize = 10.sp) },
                                leadingIcon = { if (ticket.status == "resolved") Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Type reply to user...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onReplyToTicket(ticket.id, ticket, replyText.trim())
                                    replyText = ""
                                }
                            },
                            modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTicket = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, fg, label) = when (status) {
        "resolved" -> Triple(Color(0xFFECFDF5), Color(0xFF059669), "Resolved")
        "in_progress" -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), "In Progress")
        else -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Open")
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(
            text = label,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
