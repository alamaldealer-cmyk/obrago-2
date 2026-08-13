package com.obrago.app.ui.worker

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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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

data class WorkerChatItem(
    val name: String,
    val avatarUrl: String,
    val serviceType: String,
    val serviceColor: Color,
    val lastMessage: String,
    val timestamp: String,
    val isVerified: Boolean = false,
    val isOnline: Boolean = true,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val isGroup: Boolean = false,
    val isSupport: Boolean = false,
    val target: CommunicationTarget? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerMessagesScreen(
    currentUser: User?,
    allJobs: List<Job>,
    allUsers: List<User>,
    bids: List<Bid>,
    onOpenChat: (CommunicationTarget) -> Unit,
    onOpenSupport: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) }

    // Build chat list for worker
    val workerId = currentUser?.id ?: ""
    val myBiddedJobIds = bids.filter { it.workerId == workerId }.map { it.jobId }.toSet()
    val relevantJobs = allJobs.filter { job ->
        job.workerId == workerId || myBiddedJobIds.contains(job.id)
    }

    val customerChats = relevantJobs.mapNotNull { job ->
        val customer = allUsers.find { it.id == job.customerId }
        val customerName = customer?.name ?: "Customer"
        val customerAvatar = customer?.avatar?.ifBlank { "https://api.dicebear.com/7.x/avataaars/svg?seed=$customerName" }
            ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=$customerName"
        val categoryTitle = job.category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } + " Job"

        val dateStr = if (job.createdAt > 0) {
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(job.createdAt))
        } else {
            "Today"
        }

        val lastMsg = when (job.status) {
            "in_progress" -> "Active Job: Customer is waiting for service"
            "completed" -> "Job completed successfully"
            "bidding" -> "Offer sent: Budget PKR ${job.budget.toInt()}"
            else -> "Tap to open chat discussion"
        }

        WorkerChatItem(
            name = customerName,
            avatarUrl = customerAvatar,
            serviceType = "$categoryTitle (${job.city ?: "Lahore"})",
            serviceColor = ObragoGreenDark,
            lastMessage = lastMsg,
            timestamp = dateStr,
            isVerified = true,
            isOnline = true,
            unreadCount = if (job.status == "in_progress") 1 else 0,
            target = CommunicationTarget(
                name = customerName,
                avatar = customerAvatar,
                role = "Customer",
                phone = customer?.phone,
                jobId = job.id,
                userId = customer?.id
            )
        )
    }.distinctBy { it.target?.jobId ?: it.name }

    // Support chat item
    val supportChat = WorkerChatItem(
        name = "Obrago Support",
        avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Support",
        serviceType = "Official Obrago Helpdesk",
        serviceColor = Color(0xFF3B82F6),
        lastMessage = "Welcome to Obrago Worker Support. How can we help?",
        timestamp = "Always Active",
        isVerified = true,
        isOnline = true,
        isSupport = true,
        unreadCount = 0,
        target = CommunicationTarget(
            name = "Obrago Support",
            avatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Support",
            role = "Support",
            phone = "03001234567"
        )
    )

    val allChats = listOf(supportChat) + customerChats

    // Filtered chats based on search and selected tab
    val filteredChats = allChats.filter { chat ->
        val matchesSearch = searchQuery.isBlank() ||
                chat.name.contains(searchQuery, ignoreCase = true) ||
                chat.serviceType.contains(searchQuery, ignoreCase = true) ||
                chat.lastMessage.contains(searchQuery, ignoreCase = true)

        val matchesTab = when (selectedFilter) {
            1 -> !chat.isSupport // Bookings/Jobs only
            2 -> chat.unreadCount > 0 // Unread
            3 -> chat.lastMessage.contains("completed", ignoreCase = true) // Archive/Completed
            else -> true // All
        }

        matchesSearch && matchesTab
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFF1F2937))) {
                            append("Obra")
                        }
                        withStyle(style = SpanStyle(color = ObragoGreenDark)) {
                            append("go")
                        }
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Messages",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Chat with your clients",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.Create,
                    contentDescription = "New Message",
                    tint = ObragoGreenDark,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search & Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search messages...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color.Gray) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = ObragoGreenDark,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedButton(
                    onClick = { searchQuery = "" },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ObragoGreenDark),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(Icons.Outlined.FilterAlt, contentDescription = "Filter", tint = ObragoGreenDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Filter", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    Triple("All", Icons.Outlined.ChatBubbleOutline, false),
                    Triple("Bookings", Icons.Outlined.CalendarToday, false),
                    Triple("Unread", Icons.Outlined.ChatBubbleOutline, true),
                    Triple("Archive", Icons.Outlined.Inventory2, false)
                )

                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedFilter == index
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFFECFDF5) else Color.Transparent)
                            .clickable { selectedFilter = index }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.second,
                            contentDescription = tab.first,
                            tint = if (isSelected) ObragoGreenDark else Color.Gray,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tab.first,
                            fontSize = 12.sp,
                            color = if (isSelected) ObragoGreenDark else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (tab.third) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Items List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            if (filteredChats.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No active conversations",
                            color = Color(0xFF1F2937),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your messages with clients will appear here.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(filteredChats) { chat ->
                    WorkerChatItemRow(
                        chat = chat,
                        onOpenChat = {
                            if (chat.target != null) {
                                onOpenChat(chat.target)
                            } else {
                                onOpenSupport()
                            }
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Need Help Support Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFD1FAE5), RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFECFDF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HeadsetMic,
                            contentDescription = "Support",
                            tint = ObragoGreenDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Need Help?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1F2937)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Our support team is here to assist you with clients.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFECFDF5))
                            .clickable { onOpenSupport() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Support",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ObragoGreenDark
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            tint = ObragoGreenDark,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun WorkerChatItemRow(
    chat: WorkerChatItem,
    onOpenChat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenChat() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Box with Status Indicator
        Box(modifier = Modifier.size(52.dp)) {
            if (chat.isSupport) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HeadsetMic,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(26.dp)
                    )
                }
            } else {
                AsyncImage(
                    model = chat.avatarUrl,
                    contentDescription = chat.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                )
            }

            // Online Status Dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(if (chat.isOnline) Color(0xFF10B981) else Color.Gray)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Chat Info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chat.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1F2937)
                    )
                    if (chat.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Text(
                    text = chat.timestamp,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = chat.serviceType,
                fontSize = 12.sp,
                color = chat.serviceColor,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.lastMessage,
                    fontSize = 13.sp,
                    color = if (chat.unreadCount > 0) Color(0xFF1F2937) else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

                if (chat.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
