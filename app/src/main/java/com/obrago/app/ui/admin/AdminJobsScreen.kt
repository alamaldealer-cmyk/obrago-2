package com.obrago.app.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrago.app.data.model.Job

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminJobsScreen(
    jobs: List<Job>,
    searchTerm: String,
    activeFilter: String,
    currency: String = "PKR ",
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onUpdateJobStatus: (String, String) -> Unit
) {
    var selectedJobForDetails by remember { mutableStateOf<Job?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Jobs & Bookings Control (${jobs.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchTerm,
            onValueChange = onSearchChange,
            placeholder = { Text("Search category, location, description...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Bidding", "Ongoing", "Completed", "Cancelled").forEach { filter ->
                FilterChip(
                    selected = activeFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (jobs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No jobs found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(jobs) { job ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedJobForDetails = job },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Assignment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        job.category.ifBlank { "Job Request" },
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                StatusBadge(job.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        job.description.ifBlank { "No description" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Location: ${job.location.ifBlank { job.city ?: "N/A" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "$currency${job.budget.toInt()}",
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (job.status != "completed") {
                                    OutlinedButton(
                                        onClick = { onUpdateJobStatus(job.id, "completed") },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Force Complete", fontSize = 11.sp)
                                    }
                                }
                                if (job.status != "cancelled") {
                                    OutlinedButton(
                                        onClick = { onUpdateJobStatus(job.id, "cancelled") },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancel Job", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedJobForDetails?.let { job ->
        AlertDialog(
            onDismissRequest = { selectedJobForDetails = null },
            title = { Text("Job Details: ${job.category}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Category: ${job.category}")
                    Text("Status: ${job.status.uppercase()}")
                    Text("Budget: $currency${job.budget}")
                    Text("Customer ID: ${job.customerId}")
                    Text("Worker ID: ${job.workerId ?: "Not Assigned"}")
                    Text("Location: ${job.location}")
                    Text("City: ${job.city ?: "N/A"}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Description: ${job.description}", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedJobForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, fg, label) = when (status) {
        "completed" -> Triple(Color(0xFFECFDF5), Color(0xFF059669), "Completed")
        "cancelled" -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), "Cancelled")
        "accepted", "in_progress", "worker_arrived" -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), "Ongoing")
        else -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Bidding")
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(
            text = label,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
