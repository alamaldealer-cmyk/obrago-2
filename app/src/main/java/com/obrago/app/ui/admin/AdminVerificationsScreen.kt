package com.obrago.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.obrago.app.data.model.User

@Composable
fun AdminVerificationsScreen(
    pendingWorkers: List<User>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Worker ID Verifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text("${pendingWorkers.size} pending review", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (pendingWorkers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                Text("No pending verifications", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(pendingWorkers) { worker ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = worker.avatar.ifBlank { "https://api.dicebear.com/7.x/avataaars/svg?seed=${worker.id}" },
                            contentDescription = worker.name,
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(worker.name, fontWeight = FontWeight.Bold)
                            Text("CNIC: ${worker.cnic ?: "N/A"}", style = MaterialTheme.typography.labelSmall)
                            Text(worker.phone ?: "", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        worker.idFrontPic?.let {
                            AsyncImage(model = it, contentDescription = "ID Front", modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(12.dp)))
                        }
                        worker.idBackPic?.let {
                            AsyncImage(model = it, contentDescription = "ID Back", modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(12.dp)))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { onApprove(worker.id) }, modifier = Modifier.weight(1f)) {
                            Text("Approve")
                        }
                        OutlinedButton(onClick = { onReject(worker.id) }, modifier = Modifier.weight(1f)) {
                            Text("Reject", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
