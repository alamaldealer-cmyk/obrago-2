package com.obrago.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoInput

@Composable
fun AdminNotificationsScreen(onSend: (title: String, message: String, target: String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("all") }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Broadcast Notification", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text("Send a push notification to all users, or just customers/workers.", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))

        ObragoInput(label = "Title", value = title, onValueChange = { title = it }, placeholder = "e.g. New Feature!")
        Spacer(modifier = Modifier.height(12.dp))
        ObragoInput(label = "Message", value = message, onValueChange = { message = it }, placeholder = "Write your announcement...")

        Spacer(modifier = Modifier.height(12.dp))
        Text("Target Audience", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("all" to "Everyone", "customer" to "Customers", "worker" to "Workers").forEach { (id, label) ->
                FilterChip(selected = target == id, onClick = { target = id }, label = { Text(label) })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        ObragoButton(text = "Send Broadcast", enabled = title.isNotBlank() && message.isNotBlank()) {
            onSend(title, message, target)
            title = ""
            message = ""
        }
    }
}
