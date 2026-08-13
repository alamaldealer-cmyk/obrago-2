package com.obrago.app.ui.customer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.obrago.app.data.model.Bid
import com.obrago.app.ui.common.ObragoInput

@Composable
fun CounterOfferDialog(
    bid: Bid,
    currency: String = "Rs.",
    onDismiss: () -> Unit,
    onSubmit: (price: Double, note: String?) -> Unit
) {
    var counterPrice by remember { mutableStateOf(bid.price.toInt().toString()) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("InDrive Price Negotiation") },
        text = {
            Column {
                Text("Worker's current bid is $currency${bid.price.toInt()}. Propose your target budget below:")
                Spacer(modifier = Modifier.height(10.dp))
                ObragoInput(
                    label = "Your Counter Price ($currency)",
                    value = counterPrice,
                    onValueChange = { counterPrice = it },
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(10.dp))
                ObragoInput(
                    label = "Note for Worker (Optional)",
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "e.g. Work is urgent, can start now?"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val price = counterPrice.toDoubleOrNull()
                if (price != null && price > 0) {
                    onSubmit(price, note.ifBlank { null })
                }
            }) { Text("Send Counter Proposal") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
