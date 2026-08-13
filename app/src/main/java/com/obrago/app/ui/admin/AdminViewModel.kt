package com.obrago.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obrago.app.data.model.AdminSettings
import com.obrago.app.data.model.Category
import com.obrago.app.data.model.DepositRequest
import com.obrago.app.data.model.Job
import com.obrago.app.data.model.SupportMessage
import com.obrago.app.data.model.SupportTicket
import com.obrago.app.data.model.User
import com.obrago.app.data.repository.AdminRepository
import com.obrago.app.data.repository.JobRepository
import com.obrago.app.data.repository.SupportTicketRepository
import com.obrago.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

enum class AdminTab { OVERVIEW, USERS, WORKERS, JOBS, SUPPORT, VERIFICATIONS, CATEGORIES, PAYMENTS, NOTIFICATIONS, SETTINGS }

data class AdminUiState(
    val activeTab: AdminTab = AdminTab.OVERVIEW,
    val allUsers: List<User> = emptyList(),
    val jobs: List<Job> = emptyList(),
    val categories: List<Category> = emptyList(),
    val depositRequests: List<DepositRequest> = emptyList(),
    val supportTickets: List<SupportTicket> = emptyList(),
    val settings: AdminSettings = AdminSettings(),
    val searchTerm: String = "",
    val jobStatusFilter: String = "All",
    val isBusy: Boolean = false,
    val toastMessage: String? = null
) {
    val pendingVerifications: List<User>
        get() = allUsers.filter { it.role == "worker" && it.verificationStatus == "pending" }

    val customers: List<User> get() = allUsers.filter { it.role == "customer" }
    val workers: List<User> get() = allUsers.filter { it.role == "worker" }

    val totalCustomers: Int get() = customers.size
    val totalWorkers: Int get() = workers.size
    val totalJobsPosted: Int get() = jobs.size
    val totalJobsCompleted: Int get() = jobs.count { it.status == "completed" }
    val totalEarnings: Double get() = jobs.filter { it.status == "completed" }.sumOf { it.budget }
    val pendingDeposits: List<DepositRequest> get() = depositRequests.filter { it.status == "pending" }

    val filteredUsers: List<User>
        get() = if (searchTerm.isBlank()) allUsers else allUsers.filter {
            it.name.contains(searchTerm, ignoreCase = true) ||
                (it.phone?.contains(searchTerm) == true) ||
                (it.email?.contains(searchTerm, ignoreCase = true) == true)
        }

    val filteredCustomers: List<User>
        get() = if (searchTerm.isBlank()) customers else customers.filter {
            it.name.contains(searchTerm, ignoreCase = true) ||
                (it.phone?.contains(searchTerm) == true) ||
                (it.email?.contains(searchTerm, ignoreCase = true) == true)
        }

    val filteredWorkers: List<User>
        get() = if (searchTerm.isBlank()) workers else workers.filter {
            it.name.contains(searchTerm, ignoreCase = true) ||
                (it.phone?.contains(searchTerm) == true) ||
                (it.email?.contains(searchTerm, ignoreCase = true) == true) ||
                (it.city?.contains(searchTerm, ignoreCase = true) == true)
        }

    val filteredJobs: List<Job>
        get() = jobs.filter { job ->
            val matchesFilter = when (jobStatusFilter) {
                "Ongoing" -> job.status == "accepted" || job.status == "worker_arrived" || job.status == "in_progress"
                "Completed" -> job.status == "completed"
                "Cancelled" -> job.status == "cancelled"
                "Bidding" -> job.status == "bidding" || job.status == "searching"
                else -> true
            }
            val matchesSearch = if (searchTerm.isBlank()) true else {
                job.category.contains(searchTerm, ignoreCase = true) ||
                    job.description.contains(searchTerm, ignoreCase = true) ||
                    job.location.contains(searchTerm, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }
}

class AdminViewModel(
    private val adminRepository: AdminRepository = AdminRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val jobRepository: JobRepository = JobRepository(),
    private val supportTicketRepository: SupportTicketRepository = SupportTicketRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        combine(
            userRepository.observeAllUsers(),
            jobRepository.observeJobs(),
            adminRepository.observeCategories(),
            adminRepository.observeDepositRequests(),
            adminRepository.observeAdminSettings(),
            supportTicketRepository.observeAllTickets()
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val users = args[0] as List<User>
            @Suppress("UNCHECKED_CAST")
            val jobs = args[1] as List<Job>
            @Suppress("UNCHECKED_CAST")
            val cats = args[2] as List<Category>
            @Suppress("UNCHECKED_CAST")
            val deposits = args[3] as List<DepositRequest>
            @Suppress("UNCHECKED_CAST")
            val settings = args[4] as AdminSettings
            @Suppress("UNCHECKED_CAST")
            val tickets = args[5] as List<SupportTicket>

            _uiState.value = _uiState.value.copy(
                allUsers = users, jobs = jobs, categories = cats,
                depositRequests = deposits, settings = settings, supportTickets = tickets
            )
        }.launchIn(viewModelScope)
    }

    fun replyToSupportTicket(ticketId: String, ticket: SupportTicket, replyText: String) {
        viewModelScope.launch {
            val msg = SupportMessage(
                senderId = "admin",
                senderName = "Admin Support",
                text = replyText,
                timestamp = System.currentTimeMillis(),
                isAdmin = true
            )
            supportTicketRepository.addMessageToTicket(ticketId, ticket, msg)
            _uiState.value = _uiState.value.copy(toastMessage = "Reply sent to user")
        }
    }

    fun updateSupportTicketStatus(ticketId: String, newStatus: String) {
        viewModelScope.launch {
            supportTicketRepository.updateTicketStatus(ticketId, newStatus)
            _uiState.value = _uiState.value.copy(toastMessage = "Ticket status updated to $newStatus")
        }
    }

    fun setTab(tab: AdminTab) { _uiState.value = _uiState.value.copy(activeTab = tab) }
    fun setSearchTerm(v: String) { _uiState.value = _uiState.value.copy(searchTerm = v) }
    fun setJobStatusFilter(f: String) { _uiState.value = _uiState.value.copy(jobStatusFilter = f) }
    fun consumeToast() { _uiState.value = _uiState.value.copy(toastMessage = null) }

    fun updateJobStatus(jobId: String, newStatus: String) {
        viewModelScope.launch {
            adminRepository.updateJobStatus(jobId, newStatus)
            _uiState.value = _uiState.value.copy(toastMessage = "Job status updated to $newStatus")
        }
    }

    fun adjustWorkerPoints(userId: String, currentPoints: Long, delta: Long) {
        viewModelScope.launch {
            adminRepository.adjustUserPoints(userId, currentPoints, delta)
            _uiState.value = _uiState.value.copy(toastMessage = "Points updated")
        }
    }

    fun verifyWorker(userId: String, approve: Boolean) {
        viewModelScope.launch {
            adminRepository.verifyWorker(userId, if (approve) "verified" else "rejected")
            _uiState.value = _uiState.value.copy(toastMessage = if (approve) "Worker verified" else "Worker rejected")
        }
    }

    fun toggleBlockUser(user: User) {
        viewModelScope.launch { adminRepository.toggleBlockUser(user.id, user.isBlocked ?: false) }
    }

    fun deleteAccount(userId: String) {
        viewModelScope.launch {
            adminRepository.deleteAccount(userId)
            _uiState.value = _uiState.value.copy(toastMessage = "Account deleted")
        }
    }

    fun addCategory(name: String, icon: String, isLongProject: Boolean, duration: String?, upfrontFee: Double?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            adminRepository.addCategory(Category(id = "", name = name, icon = icon, isLongProject = isLongProject, duration = duration, upfrontFee = upfrontFee))
            _uiState.value = _uiState.value.copy(toastMessage = "Category added")
        }
    }

    fun saveSettings(settings: AdminSettings) {
        viewModelScope.launch {
            adminRepository.updateAdminSettings(settings)
            _uiState.value = _uiState.value.copy(toastMessage = "Settings saved")
        }
    }

    fun processDeposit(request: DepositRequest, approve: Boolean) {
        val user = _uiState.value.allUsers.firstOrNull { it.id == request.userId }
        viewModelScope.launch {
            adminRepository.processDepositRequest(request, approve, user?.points)
            _uiState.value = _uiState.value.copy(toastMessage = if (approve) "Deposit approved" else "Deposit rejected")
        }
    }

    fun refundPenalty(userId: String) {
        viewModelScope.launch {
            adminRepository.refundPenalty(userId)
            _uiState.value = _uiState.value.copy(toastMessage = "Penalty refunded")
        }
    }

    fun sendBroadcast(title: String, message: String, target: String) {
        if (title.isBlank() || message.isBlank()) return
        viewModelScope.launch {
            adminRepository.sendAdminBroadcast(title, message, target)
            _uiState.value = _uiState.value.copy(toastMessage = "Broadcast sent")
        }
    }
}
