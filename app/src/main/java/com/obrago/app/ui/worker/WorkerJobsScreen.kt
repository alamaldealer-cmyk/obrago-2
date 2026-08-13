package com.obrago.app.ui.worker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrago.app.data.model.Bid
import com.obrago.app.data.model.Job
import com.obrago.app.ui.theme.ObragoGreenDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val categoryIcons: Map<String, ImageVector> = mapOf(
    "all" to Icons.Outlined.GridView,
    "construction" to Icons.Outlined.Construction,
    "plumbing" to Icons.Outlined.WaterDrop,
    "plumber" to Icons.Outlined.WaterDrop,
    "electrical" to Icons.Outlined.Bolt,
    "electrician" to Icons.Outlined.Bolt,
    "carpentry" to Icons.Outlined.Handyman,
    "carpenter" to Icons.Outlined.Handyman,
    "painting" to Icons.Outlined.FormatPaint,
    "painter" to Icons.Outlined.FormatPaint,
    "ac_repair" to Icons.Outlined.AcUnit,
    "ac" to Icons.Outlined.AcUnit,
    "cleaning" to Icons.Outlined.CleaningServices,
    "cleaner" to Icons.Outlined.CleaningServices,
    "mechanic" to Icons.Outlined.Build,
    "mason" to Icons.Outlined.Construction,
    "more" to Icons.Outlined.Apps
)

@Composable
fun WorkerJobsScreen(
    myBids: List<Bid>,
    completedJobs: List<Job>,
    availableJobs: List<Job>,
    allJobs: List<Job>,
    jobsFilterTab: String,
    activeCategoryFilter: String,
    currency: String = "PKR",
    onSetCategory: (String) -> Unit,
    onSetTab: (String) -> Unit,
    onOpenCounterOffer: (Job) -> Unit,
    onSelectJob: (Job) -> Unit,
    onBack: () -> Unit
) {
    val currentTab = if (jobsFilterTab == "bids") "applied" else if (jobsFilterTab == "saved") "saved" else "available"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(24.dp))
            
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
                    Text(text = "Jobs", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Text(text = "Find jobs and earn more", fontSize = 12.sp, color = Color.Gray)
                }
                
                Box {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = ObragoGreenDark,
                        modifier = Modifier.size(28.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(ObragoGreenDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("2", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search jobs by service or keyword...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = ObragoGreenDark,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).height(50.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ObragoGreenDark, containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(Icons.Outlined.FilterAlt, contentDescription = "Filter", tint = ObragoGreenDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filter", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Dropdown", tint = ObragoGreenDark)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            val categories = listOf(
                "all" to "All",
                "construction" to "Construction",
                "plumbing" to "Plumbing",
                "electrical" to "Electrical",
                "carpentry" to "Carpentry",
                "more" to "More"
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.forEach { (id, label) ->
                    val isSelected = activeCategoryFilter == id || (activeCategoryFilter.isEmpty() && id == "all")
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(80.dp)
                                .height(88.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFF0FDF4) else Color.White)
                                .border(1.dp, if (isSelected) Color(0xFFD1FAE5) else Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                                .clickable { onSetCategory(if (id == "all") "" else id) }
                                .padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = categoryIcons[id] ?: Icons.Outlined.Build,
                                contentDescription = label,
                                tint = if (isSelected) ObragoGreenDark else Color(0xFF4B5563),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                color = if (isSelected) ObragoGreenDark else Color(0xFF4B5563),
                                maxLines = 1
                            )
                        }
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(modifier = Modifier.width(40.dp).height(2.dp).background(ObragoGreenDark))
                        } else {
                            Spacer(modifier = Modifier.height(8.dp)) // Reserve space so layout doesn't jump
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0FDF4))
                    .border(1.dp, Color(0xFFD1FAE5), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, ObragoGreenDark, RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WorkOutline,
                        contentDescription = "Jobs",
                        tint = ObragoGreenDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Get more jobs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Verified", tint = ObragoGreenDark, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Complete your profile to get better job opportunities.", fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Button(
                    onClick = { /* Complete Profile */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Complete Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .height(52.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    Triple("available", "Available Jobs", Icons.Outlined.WorkOutline),
                    Triple("applied", "Applied Jobs", Icons.Outlined.AccessTime),
                    Triple("saved", "Saved Jobs", Icons.Outlined.BookmarkBorder)
                )
                
                tabs.forEachIndexed { index, (id, label, icon) ->
                    val isSelected = currentTab == id
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSetTab(if (id == "applied") "bids" else if (id == "saved") "saved" else "all") },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) ObragoGreenDark else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                color = if (isSelected) ObragoGreenDark else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        if (isSelected) {
                            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(ObragoGreenDark))
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.Transparent))
                        }
                    }
                    
                    if (index < tabs.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(0.5f)
                                .background(Color(0xFFE5E7EB))
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            when (currentTab) {
                "available" -> {
                    val filteredJobs = if (activeCategoryFilter.isNotEmpty() && activeCategoryFilter != "all") {
                        availableJobs.filter { it.category.equals(activeCategoryFilter, ignoreCase = true) }
                    } else {
                        availableJobs
                    }
                    
                    if (filteredJobs.isEmpty()) {
                        item {
                            EmptyStateWorker("No available jobs found", "Try changing filters or check back later.")
                        }
                    } else {
                        items(filteredJobs) { job ->
                            WorkerJobCard(
                                job = job,
                                currency = currency,
                                onClick = { onSelectJob(job) }
                            )
                        }
                    }
                }
                "applied" -> {
                    if (myBids.isEmpty()) {
                        item {
                            EmptyStateWorker("No applied jobs yet", "You haven't submitted any offers yet.")
                        }
                    } else {
                        items(myBids) { bid ->
                            val parentJob = allJobs.firstOrNull { it.id == bid.jobId }
                            if (parentJob != null) {
                                WorkerJobCard(
                                    job = parentJob,
                                    currency = currency,
                                    onClick = { onOpenCounterOffer(parentJob) },
                                    bidPrice = bid.price,
                                    isApplied = true
                                )
                            }
                        }
                    }
                }
                "saved" -> {
                    item {
                        EmptyStateWorker("No saved jobs", "You haven't saved any jobs yet.")
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFFD1FAE5), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Safe",
                        tint = ObragoGreenDark,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("100% Safe & Secure", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Your safety is our priority. All jobs are verified.", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = ObragoGreenDark)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun EmptyStateWorker(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.WorkOutline,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, color = Color(0xFF1F2937), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(subtitle, color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun WorkerJobCard(
    job: Job, 
    currency: String, 
    onClick: () -> Unit,
    bidPrice: Double? = null,
    isApplied: Boolean = false
) {
    val categoryLower = job.category.lowercase()
    val icon = categoryIcons[categoryLower] ?: Icons.Outlined.Build
    
    val title = job.description.ifBlank { "Service Request" }
    val displayTitle = if (title.length > 30) title.take(30) + "..." else title
    val categoryDisplay = job.category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    
    val dateStr = if (job.createdAt > 0) {
        val format = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        format.format(Date(job.createdAt))
    } else {
        "Today, 10:00 AM"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0FDF4)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = categoryDisplay,
                tint = ObragoGreenDark,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(displayTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(4.dp))
            Text(categoryDisplay, fontSize = 14.sp, color = ObragoGreenDark, fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(job.location.ifBlank { "Location not provided" }, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(dateStr, fontSize = 12.sp, color = Color.Gray)
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            if (!isApplied) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFD1FAE5))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("New", color = ObragoGreenDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFF3F4F6))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Applied", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isApplied && bidPrice != null) {
                Text("$currency ${bidPrice.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ObragoGreenDark)
                Text("Your Bid", fontSize = 10.sp, color = Color.Gray)
            } else {
                Text(if (job.budget > 0) "$currency ${job.budget.toInt()}" else "Open", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ObragoGreenDark)
                Text("Fixed Price", fontSize = 10.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Save", tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
