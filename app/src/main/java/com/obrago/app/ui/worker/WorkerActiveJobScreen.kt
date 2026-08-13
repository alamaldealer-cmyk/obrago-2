package com.obrago.app.ui.worker

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.obrago.app.data.model.Bid
import com.obrago.app.data.model.CommunicationTarget
import com.obrago.app.data.model.Job
import com.obrago.app.data.model.User
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoTopBar
import com.obrago.app.ui.customer.CancelJobDialog
import com.obrago.app.ui.map.LiveMapView
import com.obrago.app.util.GeoUtils

@Composable
fun WorkerActiveJobScreen(
    job: Job,
    acceptedBid: Bid?,
    customerUser: User?,
    myLocation: List<Double>?,
    currency: String = "Rs.",
    onOpenChat: (CommunicationTarget) -> Unit,
    onCall: (String?) -> Unit,
    onMarkArrived: () -> Unit,
    onCancel: (reason: String?) -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    val workerCoords = myLocation ?: job.workerLocationCoords
    val distKm = if (workerCoords != null && job.locationCoords != null) {
        GeoUtils.getDistanceKm(workerCoords[0], workerCoords[1], job.locationCoords[0], job.locationCoords[1])
    } else null

    Column(modifier = Modifier.fillMaxSize()) {
        ObragoTopBar(title = "Active Job", onBack = {})

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                LiveMapView(
                    modifier = Modifier.fillMaxSize(),
                    customerLocation = job.locationCoords,
                    workerLocation = workerCoords
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .offset(y = (-20).dp)
                    .padding(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(
                            if (job.workerArrived == true) "Arrived" else (distKm?.let { if (it <= 0.05) "Arrived" else "${"%.1f".format(it)} km" } ?: "En Route"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (job.workerArrived == true) "Waiting for customer" else "Est. arrival: ${acceptedBid?.eta ?: "-"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("$currency${acceptedBid?.price?.toInt() ?: 0}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Agreed Price", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = customerUser?.avatar?.takeIf { it.isNotBlank() }
                                ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=Customer",
                            contentDescription = "Customer",
                            modifier = Modifier.size(44.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(customerUser?.name ?: "Customer", fontWeight = FontWeight.Bold)
                    }
                    Row {
                        IconButton(onClick = {
                            onOpenChat(
                                CommunicationTarget(
                                    name = customerUser?.name ?: "Customer",
                                    avatar = customerUser?.avatar ?: "",
                                    role = "Customer",
                                    phone = customerUser?.phone,
                                    jobId = job.id,
                                    userId = job.customerId
                                )
                            )
                        }) { Icon(Icons.Default.Message, contentDescription = "Chat") }
                        IconButton(onClick = { onCall(customerUser?.phone) }) { Icon(Icons.Default.Call, contentDescription = "Call") }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(14.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(job.location, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text("\"${job.description}\"", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (job.workerArrived != true) {
                    ObragoButton(text = "I Have Arrived", onClick = onMarkArrived)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { showCancelDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel Job", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("You have arrived.", fontWeight = FontWeight.Bold)
                        Text("Wait for the customer to complete the job and pay.", style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
