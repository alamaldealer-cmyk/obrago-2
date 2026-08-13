package com.obrago.app.ui.worker

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.obrago.app.data.model.Bid
import com.obrago.app.data.model.CommunicationTarget
import com.obrago.app.data.model.Job
import com.obrago.app.data.model.User
import com.obrago.app.ui.theme.ObragoGreenDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkerBookingsScreen(
    currentUser: User?,
    activeJob: Job?,
    completedJobs: List<Job>,
    allJobs: List<Job>,
    bids: List<Bid>,
    allUsers: List<User>,
    currency: String = "PKR",
    onOpenActiveJob: () -> Unit,
    onOpenChat: (CommunicationTarget) -> Unit,
    onCall: (String?) -> Unit,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Active", "Completed"

    // All jobs where this worker was assigned
    val myWorkerJobs = remember(allJobs, currentUser) {
        currentUser?.let { u ->
            allJobs.filter { it.workerId == u.id }
        } ?: emptyList()
    }

    val filteredJobs = remember(myWorkerJobs, selectedFilter) {
        when (selectedFilter) {
            "Active" -> myWorkerJobs.filter { it.status != "completed" && it.status != "cancelled" }
            "Completed" -> myWorkerJobs.filter { it.status == "completed" }
            else -> myWorkerJobs
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Top Header
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Bookings & Jobs",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "Manage your assigned service requests",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Surface(
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${myWorkerJobs.size} Total",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ObragoGreenDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Active", "Completed").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            onClick = { selectedFilter = filter },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) ObragoGreenDark else Color(0xFFF3F4F6),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF4B5563)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Content List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Prominent Active Job Banner if there is an active job en route
            if (activeJob != null && (selectedFilter == "All" || selectedFilter == "Active")) {
                item {
                    val customerUser = allUsers.firstOrNull { it.id == activeJob.customerId }
                    val acceptedBid = bids.firstOrNull { it.id == activeJob.acceptedBidId }
                    val agreedPrice = acceptedBid?.price ?: activeJob.budget

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenActiveJob() },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF042817)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF34D399))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (activeJob.workerArrived == true) "ARRIVED AT SITE" else "ON THE WAY (ACTIVE)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399)
                                    )
                                }

                                Text(
                                    text = "$currency ${agreedPrice.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = activeJob.description.ifBlank { activeJob.category.replaceFirstChar { it.uppercase() } },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activeJob.location.ifBlank { "Customer Location" },
                                    fontSize = 12.sp,
                                    color = Color(0xFFD1D5DB),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color(0xFF0F3E28))
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = customerUser?.avatar?.takeIf { it.isNotBlank() }
                                            ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=Customer",
                                        contentDescription = "Customer Avatar",
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = customerUser?.name ?: "Customer",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = customerUser?.phone ?: "Tap to Call",
                                            fontSize = 11.sp,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            onOpenChat(
                                                CommunicationTarget(
                                                    name = customerUser?.name ?: "Customer",
                                                    avatar = customerUser?.avatar ?: "",
                                                    role = "Customer",
                                                    phone = customerUser?.phone,
                                                    jobId = activeJob.id,
                                                    userId = activeJob.customerId
                                                )
                                            )
                                        },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0F3E28))
                                    ) {
                                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { onCall(customerUser?.phone) },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0F3E28))
                                    ) {
                                        Icon(Icons.Outlined.Phone, contentDescription = "Call", tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                                    }

                                    Button(
                                        onClick = onOpenActiveJob,
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text("Open Map", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (filteredJobs.isEmpty() && activeJob == null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No bookings found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Submit offers on available jobs to get booked!",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                val nonActiveList = filteredJobs.filter { it.id != activeJob?.id }
                items(nonActiveList) { job ->
                    val customerUser = allUsers.firstOrNull { it.id == job.customerId }
                    val acceptedBid = bids.firstOrNull { it.id == job.acceptedBidId }
                    val price = acceptedBid?.price ?: job.budget

                    val dateStr = if (job.createdAt > 0) {
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(job.createdAt))
                    } else "Recent Job"

                    val isCompleted = job.status == "completed"
                    val isCancelled = job.status == "cancelled"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (isCompleted) Color(0xFFECFDF5) else if (isCancelled) Color(0xFFFEF2F2) else Color(0xFFEFF6FF),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isCompleted) Icons.Filled.CheckCircle else if (isCancelled) Icons.Outlined.Cancel else Icons.Outlined.AccessTime,
                                                contentDescription = null,
                                                tint = if (isCompleted) ObragoGreenDark else if (isCancelled) Color(0xFFEF4444) else Color(0xFF3B82F6),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isCompleted) "Completed" else if (isCancelled) "Cancelled" else "Scheduled",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCompleted) ObragoGreenDark else if (isCancelled) Color(0xFFEF4444) else Color(0xFF3B82F6)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(dateStr, fontSize = 11.sp, color = Color.Gray)
                                }

                                Text(
                                    text = "$currency ${price.toInt()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ObragoGreenDark
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = job.description.ifBlank { job.category.replaceFirstChar { it.uppercase() } },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(job.location, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = customerUser?.avatar?.takeIf { it.isNotBlank() }
                                            ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=Customer",
                                        contentDescription = "Customer",
                                        modifier = Modifier.size(32.dp).clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(customerUser?.name ?: "Customer", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                                }

                                TextButton(
                                    onClick = {
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
                                    }
                                ) {
                                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = ObragoGreenDark)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Message", fontSize = 12.sp, color = ObragoGreenDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
