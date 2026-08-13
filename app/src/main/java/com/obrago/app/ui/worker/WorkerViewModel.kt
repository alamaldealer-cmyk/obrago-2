package com.obrago.app.ui.worker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obrago.app.data.model.Bid
import com.obrago.app.data.model.Job
import com.obrago.app.data.model.User
import com.obrago.app.data.repository.JobRepository
import com.obrago.app.data.repository.SessionManager
import com.obrago.app.data.repository.UserRepository
import com.obrago.app.util.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

data class WorkerUiState(
    val currentUser: User? = null,
    val jobs: List<Job> = emptyList(),
    val bids: List<Bid> = emptyList(),
    val allUsers: List<User> = emptyList(),

    val isOnline: Boolean = true,
    val selectedCity: String = "All Cities",
    val activeCategoryFilter: String = "all",
    val showAllRequests: Boolean = false,
    val jobsFilterTab: String = "all", // all | bids | completed
    val selectedJobForBid: Job? = null,
    val myLocation: List<Double>? = null,
    val errorMsg: String? = null
) {
    /** Mirrors activeJob in WorkerApp.tsx */
    val activeJob: Job?
        get() = currentUser?.let { u ->
            jobs.firstOrNull { it.workerId == u.id && it.status != "completed" && it.status != "cancelled" }
        }

    val completedWorkerJobs: List<Job>
        get() = currentUser?.let { u -> jobs.filter { it.workerId == u.id && it.status == "completed" } } ?: emptyList()

    val totalWorkerEarnings: Double
        get() = completedWorkerJobs.sumOf { job ->
            val acceptedBid = bids.firstOrNull { it.id == job.acceptedBidId }
            acceptedBid?.price ?: job.budget
        }

    val myBids: List<Bid>
        get() = currentUser?.let { u -> bids.filter { it.workerId == u.id } } ?: emptyList()

    val availableJobs: List<Job>
        get() = jobs.filter { job ->
            if (job.status != "bidding") return@filter false
            
            // Worker city filter: Worker only sees jobs in their specific city
            val workerProfileCity = currentUser?.city?.takeIf { it.isNotBlank() }
            val activeCity = if (selectedCity != "All Cities") selectedCity else (workerProfileCity ?: "All Cities")

            if (activeCity != "All Cities") {
                val jobCity = (job.city ?: job.location).trim()
                val matches = jobCity.contains(activeCity, ignoreCase = true) ||
                        activeCity.contains(jobCity, ignoreCase = true)
                if (!matches) return@filter false
            }
            if (activeCategoryFilter != "all" && job.category != activeCategoryFilter) return@filter false
            true
        }
}

class WorkerViewModel(
    private val jobRepository: JobRepository = JobRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerUiState())
    val uiState: StateFlow<WorkerUiState> = _uiState.asStateFlow()

    private val _newJobAlert = MutableStateFlow<Job?>(null)
    val newJobAlert: StateFlow<Job?> = _newJobAlert.asStateFlow()

    private var seenJobIds: MutableSet<String>? = null

    private var locationSyncJob: kotlinx.coroutines.Job? = null

    init {
        combine(
            SessionManager.currentUser,
            jobRepository.observeJobs(),
            jobRepository.observeBids(),
            userRepository.observeAllUsers()
        ) { user, jobs, bids, allUsers ->
            _uiState.value = _uiState.value.copy(
                currentUser = user,
                jobs = jobs,
                bids = bids,
                allUsers = allUsers
            )
            checkForNewJobAlert(jobs)
        }.launchIn(viewModelScope)
    }

    /**
     * Mirrors the "new job appeared while online" alert trigger that pairs
     * JobAlertToast.tsx (in-app banner) with triggerSystemJobAlert() (system
     * notification) in the web app. Only fires for jobs that appear AFTER the
     * first snapshot (so the worker isn't spammed with every existing job on
     * app launch), and only while the worker is online.
     */
    private fun checkForNewJobAlert(jobs: List<Job>) {
        val available = jobs.filter { it.status == "bidding" }
        val currentIds = available.map { it.id }.toSet()

        val previouslySeen = seenJobIds
        if (previouslySeen == null) {
            // First snapshot - just record the baseline, don't alert.
            seenJobIds = currentIds.toMutableSet()
            return
        }

        if (_uiState.value.isOnline) {
            val newJob = available.firstOrNull { it.id !in previouslySeen }
            if (newJob != null) {
                _newJobAlert.value = newJob
            }
        }
        seenJobIds = currentIds.toMutableSet()
    }

    fun consumeNewJobAlert() {
        _newJobAlert.value = null
    }

    fun toggleOnline() {
        _uiState.value = _uiState.value.copy(isOnline = !_uiState.value.isOnline)
    }

    fun setCity(city: String) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
    }

    fun setCategoryFilter(categoryId: String) {
        _uiState.value = _uiState.value.copy(activeCategoryFilter = categoryId)
    }

    fun toggleShowAllRequests() {
        _uiState.value = _uiState.value.copy(showAllRequests = !_uiState.value.showAllRequests)
    }

    fun setJobsFilterTab(tab: String) {
        _uiState.value = _uiState.value.copy(jobsFilterTab = tab)
    }

    fun selectJobForBid(job: Job?) {
        _uiState.value = _uiState.value.copy(selectedJobForBid = job)
    }

    fun refreshLocation(context: android.content.Context) {
        viewModelScope.launch {
            val latLng = LocationHelper.getCurrentLatLng(context)
            if (latLng != null) {
                _uiState.value = _uiState.value.copy(myLocation = listOf(latLng.first, latLng.second))
            }
        }
    }

    /** Mirrors submitBid() call site in WorkerSubmitBid, including the points deduction. */
    fun submitBid(job: Job, price: Double, etaMinutes: String, message: String) {
        val user = _uiState.value.currentUser ?: return
        val pointsCost = Math.ceil(job.budget * 0.05).toLong()
        if (user.points < pointsCost) return

        viewModelScope.launch {
            jobRepository.adjustPoints(user.id, -pointsCost).onSuccess { newPoints ->
                SessionManager.updateCurrentUser(user.copy(points = newPoints))
            }
            jobRepository.submitBid(
                Bid(
                    jobId = job.id,
                    workerId = user.id,
                    workerName = user.name,
                    workerRating = user.rating,
                    workerAvatar = user.avatar,
                    workerJobs = user.completedJobs,
                    price = price,
                    eta = "$etaMinutes mins",
                    message = message
                )
            )
            _uiState.value = _uiState.value.copy(selectedJobForBid = null)
        }
    }

    fun acceptCounterOffer(bid: Bid) {
        viewModelScope.launch {
            jobRepository.acceptCounterOffer(bid)
            _uiState.value = _uiState.value.copy(selectedJobForBid = null)
        }
    }

    fun markWorkerArrived(jobId: String) {
        viewModelScope.launch { jobRepository.markWorkerArrived(jobId) }
    }

    fun cancelJob(jobId: String, reason: String?) {
        viewModelScope.launch { jobRepository.cancelJob(jobId, reason) }
    }

    /** Mirrors the 10s periodic workerLocationCoords sync effect in WorkerActiveJob. */
    fun startLocationSync(context: android.content.Context, jobId: String) {
        stopLocationSync()
        locationSyncJob = viewModelScope.launch {
            while (true) {
                val latLng = LocationHelper.getCurrentLatLng(context)
                if (latLng != null) {
                    val coords = listOf(latLng.first, latLng.second)
                    _uiState.value = _uiState.value.copy(myLocation = coords)
                    jobRepository.updateWorkerLocation(jobId, coords)
                }
                kotlinx.coroutines.delay(10_000)
            }
        }
    }

    fun stopLocationSync() {
        locationSyncJob?.cancel()
        locationSyncJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationSync()
    }

    fun bidFor(jobId: String): Bid? {
        val user = _uiState.value.currentUser ?: return null
        return _uiState.value.bids.firstOrNull { it.jobId == jobId && it.workerId == user.id }
    }

    fun acceptedBidFor(job: Job): Bid? = _uiState.value.bids.firstOrNull { it.id == job.acceptedBidId }
}
