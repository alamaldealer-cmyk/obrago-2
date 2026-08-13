package com.obrago.app.ui.worker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.NavigationBarItemDefaults
import com.obrago.app.data.model.CommunicationTarget
import com.obrago.app.notifications.NotificationHelper
import com.obrago.app.ui.chat.ChatScreen
import com.obrago.app.ui.chat.ChatViewModel
import com.obrago.app.ui.common.QuitConfirmationDialog
import com.obrago.app.ui.theme.ObragoGreenDark

private enum class WorkerTab { HOME, BOOKINGS, MESSAGES, JOBS, PROFILE, WALLET }

@Composable
fun WorkerRoot(onOpenProfile: () -> Unit) {
    val viewModel: WorkerViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var tab by remember { mutableStateOf(WorkerTab.HOME) }
    var showChat by remember { mutableStateOf(false) }
    var showQuitDialog by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showChat -> showChat = false
            tab != WorkerTab.HOME -> tab = WorkerTab.HOME
            else -> showQuitDialog = true
        }
    }

    if (showQuitDialog) {
        QuitConfirmationDialog(
            onConfirmQuit = {
                showQuitDialog = false
                (context as? Activity)?.finish()
            },
            onDismiss = { showQuitDialog = false }
        )
    }


    val newJobAlert by viewModel.newJobAlert.collectAsState()
    LaunchedEffect(newJobAlert?.id) {
        newJobAlert?.let { job ->
            val hasPermission = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasPermission || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                NotificationHelper.showJobAlert(context, job)
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPermission) viewModel.refreshLocation(context)
    }

    fun openChat(target: CommunicationTarget) {
        chatViewModel.open(target)
        showChat = true
    }

    fun callNumber(phone: String?) {
        val clean = (phone ?: "03001234567").filter { it.isDigit() || it == '+' }
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${clean.ifBlank { "03001234567" }}")))
    }

    val activeJob = state.activeJob

    // Keep pushing this worker's live location to Firestore while a job is active and not yet arrived
    LaunchedEffect(activeJob?.id, activeJob?.workerArrived) {
        if (activeJob != null && activeJob.workerArrived != true) {
            viewModel.startLocationSync(context, activeJob.id)
        } else {
            viewModel.stopLocationSync()
        }
    }

    if (showChat) {
        ChatScreen(viewModel = chatViewModel, onBack = { showChat = false })
        return
    }

    when {
        activeJob != null -> {
            val acceptedBid = viewModel.acceptedBidFor(activeJob)
            val customerUser = state.allUsers.firstOrNull { it.id == activeJob.customerId }
            WorkerActiveJobScreen(
                job = activeJob,
                acceptedBid = acceptedBid,
                customerUser = customerUser,
                myLocation = state.myLocation,
                onOpenChat = ::openChat,
                onCall = { phone -> callNumber(phone ?: customerUser?.phone) },
                onMarkArrived = { viewModel.markWorkerArrived(activeJob.id) },
                onCancel = { reason -> viewModel.cancelJob(activeJob.id, reason) }
            )
        }

        state.selectedJobForBid != null -> {
            val job = state.selectedJobForBid!!
            WorkerSubmitBidScreen(
                job = job,
                currentUser = state.currentUser,
                existingBid = viewModel.bidFor(job.id),
                myLocation = state.myLocation,
                onBack = { viewModel.selectJobForBid(null) },
                onSubmit = { price, eta, message -> viewModel.submitBid(job, price, eta, message) },
                onAcceptCounter = {
                    viewModel.bidFor(job.id)?.let { viewModel.acceptCounterOffer(it) }
                }
            )
        }

        else -> {
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                        when (tab) {
                            WorkerTab.HOME -> WorkerHomeScreen(
                                currentUser = state.currentUser,
                                availableJobs = state.availableJobs,
                                showAllRequests = state.showAllRequests,
                                isOnline = state.isOnline,
                                selectedCity = state.selectedCity,
                                activeCategoryFilter = state.activeCategoryFilter,
                                totalEarnings = state.totalWorkerEarnings,
                                completedCount = state.completedWorkerJobs.size,
                                onToggleOnline = viewModel::toggleOnline,
                                onToggleShowAll = viewModel::toggleShowAllRequests,
                                onSetCity = viewModel::setCity,
                                onSetCategory = viewModel::setCategoryFilter,
                                onSelectJob = { job -> viewModel.selectJobForBid(job) },
                                onOpenProfile = { tab = WorkerTab.PROFILE },
                                onOpenJobsTab = { tab = WorkerTab.JOBS },
                                onOpenSupport = {
                                    openChat(
                                        CommunicationTarget(
                                            name = "Obrago Support",
                                            avatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Support",
                                            role = "Support",
                                            phone = "03001234567"
                                        )
                                    )
                                },
                                onOpenWallet = { tab = WorkerTab.WALLET }
                            )

                            WorkerTab.BOOKINGS -> WorkerBookingsScreen(
                                currentUser = state.currentUser,
                                activeJob = activeJob,
                                completedJobs = state.completedWorkerJobs,
                                allJobs = state.jobs,
                                bids = state.bids,
                                allUsers = state.allUsers,
                                onOpenActiveJob = { /* Handled automatically when activeJob != null */ },
                                onOpenChat = ::openChat,
                                onCall = { phone -> callNumber(phone) },
                                onBackToHome = { tab = WorkerTab.HOME }
                            )

                            WorkerTab.MESSAGES -> WorkerMessagesScreen(
                                currentUser = state.currentUser,
                                allJobs = state.jobs,
                                allUsers = state.allUsers,
                                bids = state.bids,
                                onOpenChat = ::openChat,
                                onOpenSupport = {
                                    openChat(
                                        CommunicationTarget(
                                            name = "Obrago Support",
                                            avatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Support",
                                            role = "Support",
                                            phone = "03001234567"
                                        )
                                    )
                                }
                            )

                            WorkerTab.JOBS -> WorkerJobsScreen(
                                myBids = state.myBids,
                                completedJobs = state.completedWorkerJobs,
                                availableJobs = state.availableJobs,
                                allJobs = state.jobs,
                                jobsFilterTab = state.jobsFilterTab,
                                activeCategoryFilter = state.activeCategoryFilter,
                                onSetCategory = viewModel::setCategoryFilter,
                                onSetTab = viewModel::setJobsFilterTab,
                                onOpenCounterOffer = { job -> viewModel.selectJobForBid(job) },
                                onSelectJob = { job -> viewModel.selectJobForBid(job) },
                                onBack = { tab = WorkerTab.HOME }
                            )

                            WorkerTab.WALLET -> WorkerWalletScreen(
                                currentUser = state.currentUser,
                                totalEarnings = state.totalWorkerEarnings,
                                completedJobsCount = state.completedWorkerJobs.size,
                                onBack = { tab = WorkerTab.HOME }
                            )

                            WorkerTab.PROFILE -> WorkerProfileScreen(
                                currentUser = state.currentUser,
                                totalEarnings = state.totalWorkerEarnings,
                                completedJobsCount = state.completedWorkerJobs.size,
                                onOpenWallet = { tab = WorkerTab.WALLET },
                                onOpenSupport = {
                                    openChat(
                                        CommunicationTarget(
                                            name = "Obrago Support",
                                            avatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Support",
                                            role = "Support",
                                            phone = "03001234567"
                                        )
                                    )
                                },
                                onLoggedOut = onOpenProfile,
                                onBack = { tab = WorkerTab.HOME }
                            )
                        }
                    }

                    NavigationBar(
                        containerColor = Color.White,
                        contentColor = Color.Gray
                    ) {
                        NavigationBarItem(
                            selected = tab == WorkerTab.HOME,
                            onClick = { tab = WorkerTab.HOME },
                            icon = { Icon(if (tab == WorkerTab.HOME) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = null, tint = if (tab == WorkerTab.HOME) ObragoGreenDark else Color.Gray) },
                            label = { Text("Home", color = if (tab == WorkerTab.HOME) ObragoGreenDark else Color.Gray) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                        )
                        NavigationBarItem(
                            selected = tab == WorkerTab.BOOKINGS,
                            onClick = { tab = WorkerTab.BOOKINGS },
                            icon = { Icon(if (tab == WorkerTab.BOOKINGS) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarToday, contentDescription = null, tint = if (tab == WorkerTab.BOOKINGS) ObragoGreenDark else Color.Gray) },
                            label = { Text("Bookings", color = if (tab == WorkerTab.BOOKINGS) ObragoGreenDark else Color.Gray) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                        )
                        NavigationBarItem(
                            selected = tab == WorkerTab.MESSAGES,
                            onClick = { tab = WorkerTab.MESSAGES },
                            icon = { Icon(if (tab == WorkerTab.MESSAGES) Icons.Filled.Message else Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = if (tab == WorkerTab.MESSAGES) ObragoGreenDark else Color.Gray) },
                            label = { Text("Messages", color = if (tab == WorkerTab.MESSAGES) ObragoGreenDark else Color.Gray) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                        )
                        NavigationBarItem(
                            selected = tab == WorkerTab.JOBS,
                            onClick = { tab = WorkerTab.JOBS },
                            icon = { Icon(if (tab == WorkerTab.JOBS) Icons.Filled.Work else Icons.Outlined.WorkOutline, contentDescription = null, tint = if (tab == WorkerTab.JOBS) ObragoGreenDark else Color.Gray) },
                            label = { Text("Jobs", color = if (tab == WorkerTab.JOBS) ObragoGreenDark else Color.Gray) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                        )
                        NavigationBarItem(
                            selected = tab == WorkerTab.PROFILE,
                            onClick = { tab = WorkerTab.PROFILE },
                            icon = { Icon(if (tab == WorkerTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person, contentDescription = null, tint = if (tab == WorkerTab.PROFILE) ObragoGreenDark else Color.Gray) },
                            label = { Text("Profile", color = if (tab == WorkerTab.PROFILE) ObragoGreenDark else Color.Gray) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                        )
                    }
                }

                JobAlertBanner(
                    job = newJobAlert,
                    onDismiss = { viewModel.consumeNewJobAlert() },
                    onOpenJob = { job ->
                        tab = WorkerTab.HOME
                        viewModel.selectJobForBid(job)
                        viewModel.consumeNewJobAlert()
                    }
                )
            }
        }
    }
}
