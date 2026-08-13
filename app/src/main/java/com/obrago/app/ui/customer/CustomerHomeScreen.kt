package com.obrago.app.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrago.app.R
import com.obrago.app.data.model.AppData
import com.obrago.app.data.model.CommunicationTarget
import com.obrago.app.data.model.Job
import com.obrago.app.data.model.User
import com.obrago.app.ui.theme.ObragoGreen
import com.obrago.app.ui.theme.ObragoGreenDark

private val categoryIcons: Map<String, ImageVector> = mapOf(
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
    "more" to Icons.Outlined.GridView
)

val displayCategories = listOf(
    Pair("electrician", "Electrical"),
    Pair("plumber", "Plumbing"),
    Pair("carpenter", "Carpentry"),
    Pair("painter", "Painting"),
    Pair("ac", "AC Repair"),
    Pair("cleaner", "Cleaning"),
    Pair("mason", "Construction"),
    Pair("more", "More")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    currentUser: User?,
    userJobs: List<Job>,
    allUsers: List<User> = emptyList(),
    onCategorySelected: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenChat: (CommunicationTarget) -> Unit
) {
    var selectedTab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null, tint = if (selectedTab == 0) ObragoGreenDark else Color.Gray) },
                    label = { Text("Home", color = if (selectedTab == 0) ObragoGreenDark else Color.Gray) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = if (selectedTab == 1) ObragoGreenDark else Color.Gray) },
                    label = { Text("Bookings", color = if (selectedTab == 1) ObragoGreenDark else Color.Gray) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = if (selectedTab == 2) ObragoGreenDark else Color.Gray) },
                    label = { Text("Messages", color = if (selectedTab == 2) ObragoGreenDark else Color.Gray) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenProfile,
                    icon = { Icon(Icons.Outlined.PersonOutline, contentDescription = null, tint = Color.Gray) },
                    label = { Text("Profile", color = Color.Gray) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            }
        }
    ) { paddingValues ->
        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("O", fontWeight = FontWeight.Black, fontSize = 18.sp, color = ObragoGreenDark)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OBRAGO", fontWeight = FontWeight.Black, fontSize = 18.sp, color = ObragoGreenDark, letterSpacing = 1.sp)
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
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .align(Alignment.TopEnd)
                                .padding(1.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(ObragoGreenDark))
                        }
                    }
                }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header
            Text(
                text = "Welcome back! \uD83D\uDC4B",
                color = Color.DarkGray,
                fontSize = 16.sp
            )
            
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF1F2937))) {
                        append("Obra")
                    }
                    withStyle(style = SpanStyle(color = ObragoGreenDark)) {
                        append("go")
                    }
                },
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
                lineHeight = 48.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Find trusted labour or start earning with ease.",
                color = Color.Gray,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search services, workers or jobs...", color = Color.Gray) },
                leadingIcon = { 
                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = ObragoGreenDark) 
                },
                trailingIcon = {
                    Icon(Icons.Outlined.Tune, contentDescription = "Filter", tint = ObragoGreenDark)
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedBorderColor = ObragoGreenDark,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFECFDF5))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(0.6f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFD1FAE5))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = ObragoGreenDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Verified Professionals",
                                color = ObragoGreenDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color(0xFF1F2937))) {
                                    append("Quality Services\nYou Can ")
                                }
                                withStyle(style = SpanStyle(color = ObragoGreenDark)) {
                                    append("Trust")
                                }
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 28.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Skilled. Verified. Reliable.\nJust for you.",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { onCategorySelected("electrician") },
                            colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Find Services Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                    
                    Box(modifier = Modifier.weight(0.4f), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.hero_shield_3d_1786531595231),
                            contentDescription = "Shield",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Categories Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "View all",
                    fontSize = 14.sp,
                    color = ObragoGreenDark,
                    modifier = Modifier.clickable { }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Categories Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(220.dp), // Approx height for 2 rows
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(displayCategories) { cat ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onCategorySelected(cat.first) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                                .background(Color.White, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcons[cat.first] ?: Icons.Outlined.Build,
                                contentDescription = cat.second,
                                tint = ObragoGreenDark,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cat.second,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            maxLines = 1
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Handyman Section
            Text(
                text = "You might need a handyman",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar stack
                    Box(modifier = Modifier.width(64.dp).height(40.dp)) {
                        val colors = listOf(Color(0xFF3B82F6), Color(0xFFF59E0B), Color(0xFF10B981))
                        for (i in 2 downTo 0) {
                            Box(
                                modifier = Modifier
                                    .padding(start = (i * 12).dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors[i])
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Handyman", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("4.8 (230+) • 15 mins away", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = { onCategorySelected("carpenter") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ObragoGreenDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObragoGreenDark),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Book Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Earn More Section
            Text(
                text = "Earn More",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = ObragoGreenDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text("Become a Worker", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Create your profile and start\nearning with Obrago.", fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
                    }
                }
                
                OutlinedButton(
                    onClick = { /* Handle logic */ },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ObragoGreenDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObragoGreenDark),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Get Started", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        } else if (selectedTab == 1) {
            CustomerBookingsTab(userJobs = userJobs, paddingValues = paddingValues)
        } else if (selectedTab == 2) {
            CustomerMessagesTab(
                userJobs = userJobs,
                allUsers = allUsers,
                paddingValues = paddingValues,
                onOpenSupport = {
                    onOpenChat(
                        CommunicationTarget(
                            name = "Support & Messages",
                            avatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Support",
                            role = "Support",
                            phone = "03001234567"
                        )
                    )
                },
                onOpenChat = onOpenChat
            )
        }
    }
}

@Composable
fun CustomerBookingsTab(userJobs: List<Job>, paddingValues: PaddingValues) {
    var selectedFilter by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
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
                Text(text = "Bookings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Text(text = "Manage your bookings and\ntrack service progress", fontSize = 12.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 14.sp)
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
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.TopEnd)
                        .padding(1.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(ObragoGreenDark), contentAlignment = Alignment.Center) {
                        Text("2", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Pair("All Bookings", Icons.Outlined.CalendarToday),
                Pair("Ongoing", Icons.Outlined.PlayCircleOutline),
                Pair("Completed", Icons.Outlined.CheckCircle),
                Pair("Canceled", Icons.Outlined.Cancel)
            )
            
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedFilter == index
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedFilter = index },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.second,
                        contentDescription = tab.first,
                        tint = if (isSelected) ObragoGreenDark else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.first,
                        fontSize = 11.sp,
                        color = if (isSelected) ObragoGreenDark else Color.Gray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isSelected) {
                        Box(modifier = Modifier.height(2.dp).fillMaxWidth().background(ObragoGreenDark))
                    } else {
                        Box(modifier = Modifier.height(2.dp).fillMaxWidth().background(Color.Transparent))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search & Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search by service or worker...", color = Color.Gray, fontSize = 14.sp) },
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
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F2937)),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Icon(Icons.Outlined.FilterAlt, contentDescription = "Filter", tint = ObragoGreenDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filter", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = ObragoGreenDark)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val filteredJobs = when (selectedFilter) {
            1 -> userJobs.filter { it.status != "completed" && it.status != "cancelled" }
            2 -> userJobs.filter { it.status == "completed" }
            3 -> userJobs.filter { it.status == "cancelled" }
            else -> userJobs
        }
        
        if (filteredJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No bookings found.", color = Color.Gray)
            }
        } else {
            filteredJobs.forEach { job ->
                val title = job.category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() } + " Service"
                val date = if (job.createdAt > 0) {
                    java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(job.createdAt))
                } else {
                    "Just now"
                }
                
                val iconColor = when (job.status) {
                    "completed" -> Color(0xFF10B981) // Green
                    "cancelled" -> Color(0xFFEF4444) // Red
                    else -> Color(0xFFF59E0B) // Orange for ongoing
                }
                
                val iconBgColor = when (job.status) {
                    "completed" -> Color(0xFFD1FAE5) // Light Green
                    "cancelled" -> Color(0xFFFEE2E2) // Light Red
                    else -> Color(0xFFFEF3C7) // Light Orange
                }
                
                val statusText = job.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() }
                
                BookingCard(
                    category = job.category,
                    title = title,
                    description = job.description,
                    status = statusText,
                    date = date,
                    location = job.location.ifBlank { "Location not provided" },
                    workerName = if (job.workerId != null) "Worker Assigned" else "Searching...",
                    workerRating = "N/A",
                    iconColor = iconColor,
                    iconBgColor = iconBgColor
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Need Help Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFECFDF5))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.HeadsetMic,
                    contentDescription = "Support",
                    tint = ObragoGreenDark,
                    modifier = Modifier.size(32.dp)
                )
                // Small dot on headset
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .border(1.dp, Color.White, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Need Help?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Our support team is here to help you with your bookings.", fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = ObragoGreenDark),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Outlined.HeadsetMic, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Contact Support", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BookingCard(
    category: String,
    title: String,
    description: String,
    status: String,
    date: String,
    location: String,
    workerName: String,
    workerRating: String,
    iconColor: Color,
    iconBgColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcons[category] ?: Icons.Outlined.Build,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(36.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(iconBgColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 13.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                // Date & Location
                Column(modifier = Modifier.weight(0.6f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(date, fontSize = 12.sp, color = Color.DarkGray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(location, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
                
                // Worker Info
                Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(workerName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1F2937))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = iconColor, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(workerRating, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {},
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, iconColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = iconColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("View Details", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

data class ChatItem(
    val name: String,
    val avatarUrl: String,
    val serviceType: String,
    val serviceColor: Color,
    val lastMessage: String,
    val timestamp: String,
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val isGroup: Boolean = false,
    val target: CommunicationTarget? = null
)

@Composable
fun CustomerMessagesTab(
    userJobs: List<Job>,
    allUsers: List<User>,
    paddingValues: PaddingValues,
    onOpenSupport: () -> Unit,
    onOpenChat: (CommunicationTarget) -> Unit
) {
    val assignedJobs = userJobs.filter { it.workerId != null }
    val chats = assignedJobs.map { job ->
        val worker = allUsers.find { it.id == job.workerId }
        val workerName = worker?.name ?: "Unknown Worker"
        val workerAvatar = worker?.avatar?.ifBlank { "https://api.dicebear.com/7.x/avataaars/svg?seed=$workerName" } 
            ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=$workerName"
        val title = job.category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() } + " Service"
        
        val dateStr = if (job.createdAt > 0) {
            java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date(job.createdAt))
        } else {
            "Just now"
        }

        ChatItem(
            name = workerName,
            avatarUrl = workerAvatar,
            serviceType = title,
            serviceColor = Color(0xFF10B981),
            lastMessage = "Click here to view messages for this job", // Fallback text as we don't have real messages loaded
            timestamp = dateStr,
            isVerified = worker?.verificationStatus == "verified",
            isOnline = false, // Simplified
            unreadCount = 0,
            target = CommunicationTarget(
                name = workerName,
                avatar = workerAvatar,
                role = "Worker",
                phone = worker?.phone,
                jobId = job.id,
                userId = job.workerId
            )
        )
    }.distinctBy { it.target?.jobId }

    var selectedFilter by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(24.dp))
            
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
                    Text(text = "Messages", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Text(text = "Chat with your workers", fontSize = 12.sp, color = Color.Gray)
                }
                
                Icon(
                    imageVector = Icons.Outlined.Create,
                    contentDescription = "New Message",
                    tint = ObragoGreenDark,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Search & Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search messages...", color = Color.Gray, fontSize = 14.sp) },
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ObragoGreenDark),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(Icons.Outlined.FilterAlt, contentDescription = "Filter", tint = ObragoGreenDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filter", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tabs Row
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
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tab.first,
                            fontSize = 12.sp,
                            color = if (isSelected) ObragoGreenDark else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (tab.third) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Chat List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            if (chats.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No active chats yet", color = Color(0xFF1F2937), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Your messages with workers will appear here.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                items(chats) { chat ->
                    ChatListItem(chat = chat, onOpenChat = {
                        if (chat.target != null) {
                            onOpenChat(chat.target)
                        } else {
                            onOpenSupport()
                        }
                    })
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Need Help Banner
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
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFECFDF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HeadsetMic,
                            contentDescription = "Support",
                            tint = ObragoGreenDark,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Need Help?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Our support team is here to help you with your bookings.", fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFECFDF5))
                            .clickable { onOpenSupport() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contact Support", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ObragoGreenDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChatListItem(chat: ChatItem, onOpenChat: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenChat() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Box
        Box(modifier = Modifier.size(56.dp)) {
            if (chat.isGroup) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFFECFDF5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Groups, contentDescription = null, tint = ObragoGreenDark, modifier = Modifier.size(28.dp))
                }
            } else {
                coil.compose.AsyncImage(
                    model = chat.avatarUrl,
                    contentDescription = chat.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFFF3F4F6))
                )
            }
            
            // Status Dot
            if (!chat.isGroup) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
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
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(chat.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                    if (chat.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    }
                }
                Text(chat.timestamp, fontSize = 12.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            Text(chat.serviceType, fontSize = 12.sp, color = chat.serviceColor, fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    chat.lastMessage, 
                    fontSize = 14.sp, 
                    color = if (chat.unreadCount > 0) Color(0xFF1F2937) else Color.Gray, 
                    maxLines = 1, 
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(chat.unreadCount.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (chat.isMuted) {
                    Icon(Icons.Outlined.NotificationsOff, contentDescription = "Muted", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}