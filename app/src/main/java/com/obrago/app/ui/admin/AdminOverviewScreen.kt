package com.obrago.app.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private data class StatCardItem(val label: String, val value: String, val icon: ImageVector, val color: Color)

@Composable
fun AdminOverviewScreen(state: AdminUiState, currency: String = "PKR ") {
    val stats = listOf(
        StatCardItem("Total Customers", "${state.totalCustomers}", Icons.Default.People, Color(0xFF3B82F6)),
        StatCardItem("Total Workers", "${state.totalWorkers}", Icons.Default.Engineering, Color(0xFF10B981)),
        StatCardItem("Jobs Posted", "${state.totalJobsPosted}", Icons.Default.Assignment, Color(0xFFF59E0B)),
        StatCardItem("Total Earnings", "$currency${state.totalEarnings.toInt()}", Icons.Default.AccountBalanceWallet, Color(0xFF8B5CF6))
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Admin Analytics Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }

        // Stat Cards Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(stats[0], modifier = Modifier.weight(1f))
                    StatCard(stats[1], modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(stats[2], modifier = Modifier.weight(1f))
                    StatCard(stats[3], modifier = Modifier.weight(1f))
                }
            }
        }

        // Jobs Overview Line Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Jobs Overview Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LegendDot(color = Color(0xFF10B981), label = "Completed")
                            LegendDot(color = Color(0xFF3B82F6), label = "Ongoing")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Line Chart Canvas
                    JobsOverviewLineChart(
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                }
            }
        }

        // Jobs by Category Donut Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Jobs by Category", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        CategoryDonutChart(
                            modifier = Modifier.size(130.dp),
                            totalJobs = state.totalJobsPosted
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CategoryLegendItem(Color(0xFF10B981), "Plumbing & AC")
                            CategoryLegendItem(Color(0xFF3B82F6), "Electrician")
                            CategoryLegendItem(Color(0xFFF59E0B), "Cleaning")
                            CategoryLegendItem(Color(0xFF8B5CF6), "Carpentry")
                            CategoryLegendItem(Color(0xFFEC4899), "Others")
                        }
                    }
                }
            }
        }

        // Top Workers Widget
        if (state.workers.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Top Rated Workers", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        val topWorkers = state.workers.sortedByDescending { it.rating ?: 0.0 }.take(3)
                        topWorkers.forEachIndexed { index, worker ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("#${index + 1}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(24.dp))
                                    AsyncImage(
                                        model = worker.avatar.ifBlank { "https://api.dicebear.com/7.x/avataaars/svg?seed=${worker.id}" },
                                        contentDescription = worker.name,
                                        modifier = Modifier.size(36.dp).clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(worker.city ?: "Worker", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${worker.rating}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            if (index < topWorkers.size - 1) Divider(color = Color(0xFFF3F4F6))
                        }
                    }
                }
            }
        }

        // Recent Activity Feed
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recent System Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    ActivityItem(
                        icon = Icons.Default.PersonAdd,
                        title = "New Customers & Workers Registered",
                        time = "Real-time updates",
                        color = Color(0xFF3B82F6)
                    )
                    ActivityItem(
                        icon = Icons.Default.CheckCircle,
                        title = "${state.totalJobsCompleted} Total Jobs Completed",
                        time = "Platform total",
                        color = Color(0xFF10B981)
                    )
                    ActivityItem(
                        icon = Icons.Default.Badge,
                        title = "${state.pendingVerifications.size} Pending Worker Verifications",
                        time = "Requires admin review",
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(item: StatCardItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                color = item.color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(item.value, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text(item.label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun CategoryLegendItem(color: Color, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ActivityItem(icon: ImageVector, title: String, time: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = color.copy(alpha = 0.15f), shape = CircleShape) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp).size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(time, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun JobsOverviewLineChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val completedPoints = listOf(0.2f, 0.4f, 0.35f, 0.6f, 0.8f, 0.75f, 0.9f)
        val ongoingPoints = listOf(0.1f, 0.25f, 0.3f, 0.4f, 0.5f, 0.45f, 0.6f)

        fun getX(index: Int): Float = index * (width / (completedPoints.size - 1))
        fun getY(value: Float): Float = height - (value * height * 0.8f) - (height * 0.1f)

        // Draw Completed line
        val completedPath = Path().apply {
            moveTo(getX(0), getY(completedPoints[0]))
            for (i in 1 until completedPoints.size) {
                val x1 = getX(i - 1)
                val y1 = getY(completedPoints[i - 1])
                val x2 = getX(i)
                val y2 = getY(completedPoints[i])
                cubicTo((x1 + x2) / 2, y1, (x1 + x2) / 2, y2, x2, y2)
            }
        }
        drawPath(completedPath, color = Color(0xFF10B981), style = Stroke(width = 3.dp.toPx()))

        // Draw Ongoing line
        val ongoingPath = Path().apply {
            moveTo(getX(0), getY(ongoingPoints[0]))
            for (i in 1 until ongoingPoints.size) {
                val x1 = getX(i - 1)
                val y1 = getY(ongoingPoints[i - 1])
                val x2 = getX(i)
                val y2 = getY(ongoingPoints[i])
                cubicTo((x1 + x2) / 2, y1, (x1 + x2) / 2, y2, x2, y2)
            }
        }
        drawPath(ongoingPath, color = Color(0xFF3B82F6), style = Stroke(width = 3.dp.toPx()))
    }
}

@Composable
private fun CategoryDonutChart(modifier: Modifier = Modifier, totalJobs: Int) {
    Canvas(modifier = modifier) {
        val strokeWidth = 24.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        val chartSize = Size(diameter, diameter)

        val slices = listOf(
            35f to Color(0xFF10B981),
            25f to Color(0xFF3B82F6),
            20f to Color(0xFFF59E0B),
            12f to Color(0xFF8B5CF6),
            8f to Color(0xFFEC4899)
        )

        var startAngle = -90f
        for ((percent, color) in slices) {
            val sweepAngle = (percent / 100f) * 360f
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle - 3f, // Gap between slices
                useCenter = false,
                topLeft = topLeft,
                size = chartSize,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle
        }
    }
}
