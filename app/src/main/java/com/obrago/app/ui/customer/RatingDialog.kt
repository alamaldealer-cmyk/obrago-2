package com.obrago.app.ui.customer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.obrago.app.ui.common.ObragoInput

@Composable
fun RatingDialog(
    workerName: String,
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, comment: String) -> Unit
) {
    var stars by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate $workerName") },
        text = {
            Column {
                Row {
                    (1..5).forEach { i ->
                        IconButton(onClick = { stars = i }) {
                            Icon(
                                if (i <= stars) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ObragoInput(
                    label = "Comment (Optional)",
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = "How was your experience?"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(stars, comment) }) { Text("Submit") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        }
    )
}
