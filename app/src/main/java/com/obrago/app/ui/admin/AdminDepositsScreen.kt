package com.obrago.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.obrago.app.data.model.DepositRequest

@Composable
fun AdminDepositsScreen(
    deposits: List<DepositRequest>,
    currency: String = "Rs.",
    onApprove: (DepositRequest) -> Unit,
    onReject: (DepositRequest) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Deposit Requests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text("${deposits.count { it.status == "pending" }} pending", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (deposits.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                Text("No deposit requests", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(deposits) { req ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(req.userName, fontWeight = FontWeight.Bold)
                            Text("${req.method.uppercase()} • TrxID: ${req.trxId}", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$currency${req.amount.toInt()}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Surface(
                                color = when (req.status) {
                                    "approved" -> MaterialTheme.colorScheme.primaryContainer
                                    "rejected" -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.tertiaryContainer
                                },
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(req.status.uppercase(), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }

                    if (req.status == "pending") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onApprove(req) }, modifier = Modifier.weight(1f)) { Text("Approve") }
                            OutlinedButton(onClick = { onReject(req) }, modifier = Modifier.weight(1f)) { Text("Reject", color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }
}
