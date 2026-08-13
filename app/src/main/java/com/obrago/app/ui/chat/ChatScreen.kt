package com.obrago.app.ui.chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.obrago.app.data.model.CommunicationTarget
import com.obrago.app.data.model.Job
import com.obrago.app.data.repository.SessionManager
import com.obrago.app.ui.theme.ObragoGreenDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(viewModel: ChatViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val myId = SessionManager.currentUser.collectAsState().value?.id ?: "guest"
    var showJobDetailsDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.sendImage(context, uri)
        }
    }

    Scaffold(
        topBar = {
            ChatHeaderBar(
                target = state.target,
                onBack = onBack,
                onCall = { phone ->
                    val clean = phone?.filter { it.isDigit() || it == '+' } ?: "03001234567"
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${clean.ifBlank { "03001234567" }}"))
                    context.startActivity(intent)
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                draft = state.draft,
                onDraftChange = viewModel::setDraft,
                onSend = viewModel::send,
                onAttach = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Job Context Banner (Top Card in Chat)
            state.associatedJob?.let { job ->
                JobContextBannerCard(
                    job = job,
                    onViewDetails = { showJobDetailsDialog = true }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Centered Date Divider Badge
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFF3F4F6),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Today",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Chat Messages
                items(state.messages) { msg ->
                    val isMine = msg.senderId == myId
                    ChatMessageBubbleItem(
                        msg = msg,
                        isMine = isMine,
                        targetAvatar = state.target?.avatar
                    )
                }
            }
        }
    }

    // Job Details Dialog
    if (showJobDetailsDialog && state.associatedJob != null) {
        val job = state.associatedJob!!
        AlertDialog(
            onDismissRequest = { showJobDetailsDialog = false },
            title = {
                Text(
                    text = job.description.ifBlank { "Job Details" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category: ${job.category.capitalize(Locale.ROOT)}", fontSize = 14.sp, color = Color(0xFF4B5563))
                    Text("Location: ${job.location.ifBlank { job.city ?: "N/A" }}", fontSize = 14.sp, color = Color(0xFF4B5563))
                    Text("Budget: PKR ${job.budget.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                    Text("Status: ${job.status.uppercase()}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showJobDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

/** Header Bar matching the design screenshot */
@Composable
private fun ChatHeaderBar(
    target: CommunicationTarget?,
    onBack: () -> Unit,
    onCall: (String?) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ObragoGreenDark
                )
            }

            // User Avatar with Online Dot
            Box(modifier = Modifier.size(44.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!target?.avatar.isNullOrBlank()) {
                        AsyncImage(
                            model = target?.avatar,
                            contentDescription = target?.name,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Green Online Dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(ObragoGreenDark)
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // User Name and Subtitle/Category
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = target?.name ?: "User",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Text(
                    text = target?.role?.takeIf { it.isNotBlank() } ?: "Service Provider",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ObragoGreenDark
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ObragoGreenDark)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Online",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ObragoGreenDark
                    )
                }
            }

            // Phone Icon
            IconButton(onClick = { onCall(target?.phone) }) {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = "Call",
                    tint = ObragoGreenDark,
                    modifier = Modifier.size(22.dp)
                )
            }

            // More Options Icon
            IconButton(onClick = { /* More options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/** Card Banner for Job Context at top of Chat */
@Composable
private fun JobContextBannerCard(
    job: Job,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFECFDF5)),
                contentAlignment = Alignment.Center
            ) {
                val categoryIcon = when (job.category.lowercase()) {
                    "electrician" -> Icons.Outlined.FlashOn
                    "plumber" -> Icons.Outlined.WaterDrop
                    else -> Icons.Outlined.Build
                }
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = ObragoGreenDark,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Job Title, Date & Location Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.description.ifBlank { "Service Request" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(job.createdAt),
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.location.ifBlank { job.city ?: "Lahore" },
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // View Details Pill Button
            Button(
                onClick = onViewDetails,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFECFDF5),
                    contentColor = ObragoGreenDark
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                elevation = null
            ) {
                Text(
                    text = "View Details",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Chat Message Bubble for Incoming & Outgoing */
@Composable
private fun ChatMessageBubbleItem(
    msg: com.obrago.app.data.model.ChatMessage,
    isMine: Boolean,
    targetAvatar: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        if (!isMine) {
            // Participant Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                if (!targetAvatar.isNullOrBlank()) {
                    AsyncImage(
                        model = targetAvatar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            // Message Card Bubble
            Surface(
                shape = if (isMine) {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
                },
                color = if (isMine) Color(0xFFE2F8EE) else Color.White,
                border = if (!isMine) BorderStroke(1.dp, Color(0xFFF3F4F6)) else null,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    )
                ) {
                    if (!msg.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = msg.imageUrl,
                            contentDescription = "Attached Photo",
                            modifier = Modifier
                                .widthIn(max = 240.dp)
                                .heightIn(max = 240.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        if (msg.text.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    if (msg.text.isNotBlank()) {
                        Text(
                            text = msg.text,
                            fontSize = 14.sp,
                            color = Color(0xFF1F2937),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Timestamp and Read Receipts
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = formatTime(msg.createdAt),
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )

                if (isMine) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✓✓",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ObragoGreenDark
                    )
                }
            }
        }
    }
}

/** Bottom Floating Input Bar */
@Composable
private fun ChatInputBar(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onAttach,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AttachFile,
                            contentDescription = "Attach File",
                            tint = ObragoGreenDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    TextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        placeholder = { Text("Type a message...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 4
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Circular Green Send Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(ObragoGreenDark)
                    .clickable { onSend() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    if (timestamp <= 0L) return "10:15 AM"
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return "20 May 2025, 10:00 AM"
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
