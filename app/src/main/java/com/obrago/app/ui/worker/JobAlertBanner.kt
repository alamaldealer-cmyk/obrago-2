package com.obrago.app.ui.worker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.obrago.app.data.model.Job
import kotlinx.coroutines.delay

/** Native equivalent of JobAlertToast.tsx - shows for 10s then auto-dismisses. */
@Composable
fun JobAlertBanner(
    job: Job?,
    currency: String = "Rs.",
    onDismiss: () -> Unit,
    onOpenJob: (Job) -> Unit
) {
    LaunchedEffect(job?.id) {
        if (job != null) {
            delay(10_000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = job != null,
        enter = slideInVertically { -it },
        exit = slideOutVertically { -it }
    ) {
        if (job != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF111827))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF22C55E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(12.dp))
                                Text(" New Job Alert", color = Color(0xFF22C55E), fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
                            }
                            Text(
                                job.description.ifBlank { job.category },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF9CA3AF))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1F2937))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(14.dp))
                        Text(" ${job.location}", color = Color(0xFFD1D5DB), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                    Text("$currency ${job.budget.toInt()}", color = Color(0xFF4ADE80), fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onOpenJob(job); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View & Bid Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
