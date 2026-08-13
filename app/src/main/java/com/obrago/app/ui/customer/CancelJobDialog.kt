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
import androidx.compose.ui.unit.dp
import com.obrago.app.ui.common.ObragoInput

@Composable
fun CancelJobDialog(
    onDismiss: () -> Unit,
    onConfirm: (reason: String?) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Request") },
        text = {
            Column {
                Text("Are you sure you want to cancel this request?")
                Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))
                ObragoInput(
                    label = "Reason (Optional)",
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = "e.g. Changed my mind, taking too long..."
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason.ifBlank { null }) }) { Text("Confirm Cancel") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Go Back") }
        }
    )
}
