package com.obrago.app.ui.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.obrago.app.data.model.AppData
import com.obrago.app.data.model.Bid
import com.obrago.app.data.model.Job
import com.obrago.app.data.model.User
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoInput
import com.obrago.app.ui.common.ObragoTopBar
import com.obrago.app.ui.map.LiveMapView
import com.obrago.app.util.GeoUtils

@Composable
fun WorkerSubmitBidScreen(
    job: Job,
    currentUser: User?,
    existingBid: Bid?,
    myLocation: List<Double>?,
    currency: String = "Rs.",
    onBack: () -> Unit,
    onSubmit: (price: Double, etaMinutes: String, message: String) -> Unit,
    onAcceptCounter: () -> Unit
) {
    var price by remember { mutableStateOf(job.budget.toInt().toString()) }
    var eta by remember { mutableStateOf("15") }
    var message by remember { mutableStateOf("I am nearby and can start immediately. Expert in this field.") }

    val category = AppData.CATEGORIES.firstOrNull { it.id == job.category }
    val pointsCost = kotlin.math.ceil(job.budget * 0.05).toLong()
    val hasEnoughPoints = (currentUser?.points ?: 0) >= pointsCost

    val workerCoords = myLocation ?: job.workerLocationCoords
    val distKm = if (workerCoords != null && job.locationCoords != null) {
        GeoUtils.getDistanceKm(workerCoords[0], workerCoords[1], job.locationCoords[0], job.locationCoords[1])
    } else null

    Column(modifier = Modifier.fillMaxSize()) {
        ObragoTopBar(title = "Submit Offer", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                LiveMapView(
                    modifier = Modifier.fillMaxSize(),
                    customerLocation = job.locationCoords,
                    workerLocation = workerCoords
                )
            }
            Text(
                distKm?.let { "${"%.1f".format(it)} km away" } ?: "Customer location",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(6.dp))
                Text(job.location, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("${category?.name ?: job.category}  •  Est. Budget: $currency${job.budget.toInt()}", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("\"${job.description}\"", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(20.dp))
            Text("Your Offer Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(10.dp))

            if (existingBid != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Text("You Already Sent an Offer", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Your Bid:")
                        Text("$currency${existingBid.price.toInt()}", fontWeight = FontWeight.Bold)
                    }

                    if (existingBid.counterPrice != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("NEW COUNTER OFFER", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        Text("\"${existingBid.counterMessage ?: ""}\"", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Customer Counter Price:")
                            Text("$currency${existingBid.counterPrice.toInt()}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        ObragoButton(text = "Accept Counter Offer", onClick = onAcceptCounter)
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Waiting for customer to respond...", style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                ObragoInput(
                    label = "Your Price ($currency)", value = price, onValueChange = { price = it },
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(12.dp))
                ObragoInput(
                    label = "Arrival Time (Minutes)", value = eta, onValueChange = { eta = it },
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(12.dp))
                ObragoInput(
                    label = "Message to Customer", value = message, onValueChange = { message = it }
                )

                Spacer(modifier = Modifier.height(20.dp))
                ObragoButton(
                    text = if (hasEnoughPoints) "Send Offer (-$pointsCost Pts)" else "Not Enough Points (${currentUser?.points ?: 0}/$pointsCost)",
                    enabled = price.isNotBlank() && eta.isNotBlank() && hasEnoughPoints
                ) {
                    onSubmit(price.toDoubleOrNull() ?: job.budget, eta, message)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (hasEnoughPoints)
                        "This will deduct $pointsCost points from your wallet (5% of budget)."
                    else
                        "You need $pointsCost points to bid on this job. Please Top Up in Profile.",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
