package com.obrago.app.ui.admin

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.obrago.app.data.repository.AppSettingsManager
import com.obrago.app.data.repository.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRoot(onLogout: () -> Unit) {
    val viewModel: AdminViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val currentCurrency by AppSettingsManager.currentCurrency.collectAsState()
    val currencySymbol = currentCurrency.symbol
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    var showQuitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showQuitDialog = true
    }

    if (showQuitDialog) {
        com.obrago.app.ui.common.QuitConfirmationDialog(
            onConfirmQuit = {
                showQuitDialog = false
                (context as? Activity)?.finish()
            },
            onDismiss = { showQuitDialog = false }
        )
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Admin Panel", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = {
                            SessionManager.logout()
                            onLogout()
                        }) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout")
                        }
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    AdminTab.entries.forEach { tab ->
                        FilterChip(
                            selected = state.activeTab == tab,
                            onClick = { viewModel.setTab(tab) },
                            label = { Text(tab.label()) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state.activeTab) {
                AdminTab.OVERVIEW -> AdminOverviewScreen(state, currency = currencySymbol)
                AdminTab.USERS -> AdminCustomersScreen(
                    customers = state.filteredCustomers,
                    searchTerm = state.searchTerm,
                    currency = currencySymbol,
                    onSearchChange = viewModel::setSearchTerm,
                    onToggleBlock = viewModel::toggleBlockUser,
                    onRefundPenalty = viewModel::refundPenalty,
                    onDelete = viewModel::deleteAccount
                )
                AdminTab.WORKERS -> AdminWorkersScreen(
                    workers = state.filteredWorkers,
                    searchTerm = state.searchTerm,
                    currency = currencySymbol,
                    onSearchChange = viewModel::setSearchTerm,
                    onVerifyWorker = viewModel::verifyWorker,
                    onToggleBlock = viewModel::toggleBlockUser,
                    onAdjustPoints = viewModel::adjustWorkerPoints,
                    onDelete = viewModel::deleteAccount
                )
                AdminTab.JOBS -> AdminJobsScreen(
                    jobs = state.filteredJobs,
                    searchTerm = state.searchTerm,
                    activeFilter = state.jobStatusFilter,
                    currency = currencySymbol,
                    onSearchChange = viewModel::setSearchTerm,
                    onFilterChange = viewModel::setJobStatusFilter,
                    onUpdateJobStatus = viewModel::updateJobStatus
                )
                AdminTab.SUPPORT -> AdminSupportScreen(
                    tickets = state.supportTickets,
                    onReplyToTicket = viewModel::replyToSupportTicket,
                    onUpdateStatus = viewModel::updateSupportTicketStatus
                )
                AdminTab.VERIFICATIONS -> AdminVerificationsScreen(
                    pendingWorkers = state.pendingVerifications,
                    onApprove = { viewModel.verifyWorker(it, true) },
                    onReject = { viewModel.verifyWorker(it, false) }
                )
                AdminTab.CATEGORIES -> AdminCategoriesScreen(
                    categories = state.categories,
                    onAddCategory = viewModel::addCategory
                )
                AdminTab.PAYMENTS -> AdminDepositsScreen(
                    deposits = state.depositRequests,
                    currency = currencySymbol,
                    onApprove = { viewModel.processDeposit(it, true) },
                    onReject = { viewModel.processDeposit(it, false) }
                )
                AdminTab.NOTIFICATIONS -> AdminNotificationsScreen(onSend = viewModel::sendBroadcast)
                AdminTab.SETTINGS -> AdminSettingsScreen(
                    settings = state.settings,
                    onSave = viewModel::saveSettings
                )
            }
        }
    }
}

private fun AdminTab.label(): String = when (this) {
    AdminTab.OVERVIEW -> "Dashboard"
    AdminTab.USERS -> "Customers"
    AdminTab.WORKERS -> "Workers"
    AdminTab.JOBS -> "Jobs & Bookings"
    AdminTab.SUPPORT -> "Support Tickets"
    AdminTab.VERIFICATIONS -> "Verifications"
    AdminTab.CATEGORIES -> "Categories"
    AdminTab.PAYMENTS -> "Payments & Deposits"
    AdminTab.NOTIFICATIONS -> "Broadcast"
    AdminTab.SETTINGS -> "Settings"
}
