package com.obrago.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obrago.app.data.model.Bid
import com.obrago.app.data.model.CommunicationTarget
import com.obrago.app.data.model.Job
import com.obrago.app.data.model.User
import com.obrago.app.data.repository.JobRepository
import com.obrago.app.data.repository.SessionManager
import com.obrago.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

enum class CustomerScreen { HOME, POST_JOB, BIDDING, ACTIVE_JOB }

data class CustomerUiState(
    val currentUser: User? = null,
    val jobs: List<Job> = emptyList(),
    val bids: List<Bid> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val screen: CustomerScreen = CustomerScreen.HOME,
    val selectedCategoryId: String? = null,
    val activeChatTarget: CommunicationTarget? = null,
    val errorMsg: String? = null,
    val isBusy: Boolean = false
) {
    /** Mirrors `activeJob` computation in CustomerApp.tsx */
    val activeJob: Job?
        get() = currentUser?.let { u ->
            jobs.firstOrNull { it.customerId == u.id && it.status != "completed" && it.status != "cancelled" }
        }

    val resolvedScreen: CustomerScreen
        get() = when {
            activeJob == null -> if (screen == CustomerScreen.POST_JOB) CustomerScreen.POST_JOB else CustomerScreen.HOME
            activeJob!!.status == "bidding" -> CustomerScreen.BIDDING
            else -> CustomerScreen.ACTIVE_JOB
        }
}

class CustomerViewModel(
    private val jobRepository: JobRepository = JobRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    init {
        combine(
            SessionManager.currentUser,
            jobRepository.observeJobs(),
            jobRepository.observeBids(),
            userRepository.observeAllUsers()
        ) { user, jobs, bids, allUsers ->
            _uiState.value = _uiState.value.copy(currentUser = user, jobs = jobs, bids = bids, allUsers = allUsers)
        }.launchIn(viewModelScope)
    }

    fun selectCategory(categoryId: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId, screen = CustomerScreen.POST_JOB)
    }

    fun backToHome() {
        _uiState.value = _uiState.value.copy(selectedCategoryId = null, screen = CustomerScreen.HOME)
    }

    fun openChat(target: CommunicationTarget) {
        _uiState.value = _uiState.value.copy(activeChatTarget = target)
    }

    fun closeChat() {
        _uiState.value = _uiState.value.copy(activeChatTarget = null)
    }

    /** Mirrors postJob() call site in CustomerPostJob */
    fun postJob(
        category: String,
        description: String,
        location: String,
        city: String,
        locationCoords: List<Double>?,
        budget: Double
    ) {
        val user = _uiState.value.currentUser ?: return
        _uiState.value = _uiState.value.copy(isBusy = true, errorMsg = null)
        viewModelScope.launch {
            val result = jobRepository.postJob(user.id, category, description, location, city, locationCoords, budget)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isBusy = false, screen = CustomerScreen.HOME)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isBusy = false, errorMsg = e.message)
            }
        }
    }

    fun cancelJob(jobId: String, reason: String?) {
        viewModelScope.launch { jobRepository.cancelJob(jobId, reason) }
    }

    fun acceptBid(jobId: String, bid: Bid) {
        viewModelScope.launch { jobRepository.acceptBid(jobId, bid) }
    }

    fun submitCounterOffer(bidId: String, price: Double, message: String?) {
        viewModelScope.launch { jobRepository.submitCounterOffer(bidId, price, message) }
    }

    fun completeJob(jobId: String) {
        viewModelScope.launch { jobRepository.completeJob(jobId) }
    }

    fun submitRating(jobId: String, toUserId: String, stars: Int, comment: String) {
        val user = _uiState.value.currentUser ?: return
        val target = _uiState.value.allUsers.firstOrNull { it.id == toUserId }
        viewModelScope.launch {
            jobRepository.submitRating(
                jobId = jobId,
                fromUserId = user.id,
                toUserId = toUserId,
                stars = stars,
                comment = comment,
                targetCurrentRating = target?.rating ?: 5.0,
                targetCompletedJobs = target?.completedJobs ?: 0
            )
        }
    }

    fun cancelJobAfterArrival(job: Job, acceptedBid: Bid?) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val result = jobRepository.cancelJobAfterArrival(job, user, acceptedBid)
            result.onSuccess { updatedUser -> SessionManager.updateCurrentUser(updatedUser) }
        }
    }

    fun acceptedBidFor(job: Job): Bid? = _uiState.value.bids.firstOrNull { it.id == job.acceptedBidId }
    fun bidsFor(job: Job): List<Bid> = _uiState.value.bids.filter { it.jobId == job.id }.sortedByDescending { it.createdAt }
}
