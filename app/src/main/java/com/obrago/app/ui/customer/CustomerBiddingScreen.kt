package com.obrago.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.obrago.app.data.model.Bid
import com.obrago.app.data.model.Job
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoTopBar
import com.obrago.app.ui.map.LiveMapView

@Composable
fun CustomerBiddingScreen(
    job: Job,
    bids: List<Bid>,
    currency: String = "Rs.",
    customerLocation: List<Double>?,
    onCancel: (reason: String?) -> Unit,
    onAccept: (Bid) -> Unit,
    onCounter: (Bid) -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ObragoTopBar(title = "Finding Workers", onBack = { showCancelDialog = true })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                LiveMapView(
                    modifier = Modifier.fillMaxSize(),
                    customerLocation = customerLocation ?: job.locationCoords,
                    biddingWorkers = bids
                )
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                ) {
                    Text(
                        "Broadcasting Request...",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Offers Received (${bids.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(12.dp))

            if (bids.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text("Offers will appear here", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            bids.forEach { bid ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            AsyncImage(
                                model = bid.workerAvatar,
                                contentDescription = bid.workerName,
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(bid.workerName, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                                    Text(" ${bid.workerRating}", style = MaterialTheme.typography.labelSmall)
                                    Text("  •  ${bid.workerJobs} jobs", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$currency${bid.price.toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            bid.counterPrice?.let {
                                Text("Countered: $currency${it.toInt()}", style = MaterialTheme.typography.labelSmall)
                            }
                            Text(bid.eta, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (bid.message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
                            Text("\"${bid.message}\"", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        ObragoButton(text = "Accept Offer", modifier = Modifier.weight(1f)) { onAccept(bid) }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { onCounter(bid) }) { Text("Counter 💬") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCancelDialog) {
        CancelJobDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = { reason -> onCancel(reason); showCancelDialog = false }
        )
    }
}
