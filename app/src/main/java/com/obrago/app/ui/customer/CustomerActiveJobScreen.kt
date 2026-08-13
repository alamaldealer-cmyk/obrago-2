package com.obrago.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.obrago.app.data.model.Bid
import com.obrago.app.data.model.CommunicationTarget
import com.obrago.app.data.model.Job
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoTopBar
import com.obrago.app.ui.map.LiveMapView

@Composable
fun CustomerActiveJobScreen(
    job: Job,
    acceptedBid: Bid,
    customerAvatar: String?,
    customerLocation: List<Double>?,
    currency: String = "Rs.",
    onOpenChat: (CommunicationTarget) -> Unit,
    onCall: (String?) -> Unit,
    onComplete: () -> Unit,
    onCancel: (reason: String?) -> Unit,
    onCancelAfterArrival: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ObragoTopBar(title = "Job Active", onBack = {})

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                LiveMapView(
                    modifier = Modifier.fillMaxSize(),
                    customerLocation = customerLocation ?: job.locationCoords,
                    workerLocation = job.workerLocationCoords
                )
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (job.workerArrived == true) Icons.Default.LocationOn else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                if (job.workerArrived == true) "Status" else "Worker Arriving In",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                if (job.workerArrived == true) "Worker has arrived" else acceptedBid.eta,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = acceptedBid.workerAvatar,
                            contentDescription = acceptedBid.workerName,
                            modifier = Modifier.size(56.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(acceptedBid.workerName, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                Text(" ${acceptedBid.workerRating}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Row {
                        IconButton(onClick = {
                            onOpenChat(
                                CommunicationTarget(
                                    name = acceptedBid.workerName,
                                    avatar = acceptedBid.workerAvatar,
                                    role = "Worker",
                                    jobId = job.id,
                                    userId = acceptedBid.workerId
                                )
                            )
                        }) { Icon(Icons.Default.Message, contentDescription = "Chat") }
                        IconButton(onClick = { onCall(null) }) { Icon(Icons.Default.Call, contentDescription = "Call") }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Text("JOB DETAILS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailRow("Service", job.category.replaceFirstChar { it.uppercase() })
                    DetailRow("Agreed Price", "$currency${acceptedBid.price.toInt()}")
                    DetailRow("Address", job.location)
                }

                Spacer(modifier = Modifier.height(16.dp))
                ObragoButton(text = "Mark as Completed & Pay", onClick = onComplete)

                Spacer(modifier = Modifier.height(10.dp))
                if (!showCancelConfirm) {
                    OutlinedButton(
                        onClick = {
                            if (job.workerArrived != true) showCancelDialog = true else showCancelConfirm = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Job ${if (job.workerArrived == true) "(Worker Arrived)" else ""}")
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(16.dp)
                    ) {
                        Text("Cancel Job & Penalty Notice", fontWeight = FontWeight.Bold)
                        Text(
                            "Since the worker has already arrived, a penalty fee of 50% ($currency${(acceptedBid.price * 0.5).toInt()}) will be recorded for compensation.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row {
                            OutlinedButton(onClick = { showCancelConfirm = false }, modifier = Modifier.weight(1f)) { Text("Go Back") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = onCancelAfterArrival, modifier = Modifier.weight(1f)) { Text("Confirm Cancel") }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showCancelDialog) {
        CancelJobDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = { reason -> onCancel(reason); showCancelDialog = false }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
