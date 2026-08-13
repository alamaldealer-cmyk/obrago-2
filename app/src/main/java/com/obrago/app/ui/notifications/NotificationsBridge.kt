package com.obrago.app.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obrago.app.data.repository.Broadcast
import com.obrago.app.data.repository.BroadcastRepository
import com.obrago.app.data.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * App-wide bridge that watches for new admin broadcasts (sendAdminBroadcast in
 * store.tsx) targeted at the current user's role, for as long as the app is in
 * the foreground - same "live while open" delivery model the original web app
 * used (see notificationService.ts / triggerSystemCustomNotification).
 */
class NotificationsBridge(
    private val broadcastRepository: BroadcastRepository = BroadcastRepository()
) : ViewModel() {

    private val sessionStartedAt = System.currentTimeMillis()

    private val _latestBroadcast = MutableStateFlow<Broadcast?>(null)
    val latestBroadcast: StateFlow<Broadcast?> = _latestBroadcast.asStateFlow()

    init {
        combine(
            SessionManager.currentUser,
            broadcastRepository.observeBroadcastsSince(sessionStartedAt)
        ) { user, broadcasts ->
            if (user == null) return@combine null
            broadcasts.firstOrNull { it.targetRole == "all" || it.targetRole == user.role }
        }.onEach { match ->
            if (match != null && match.id != _latestBroadcast.value?.id) {
                _latestBroadcast.value = match
            }
        }.launchIn(viewModelScope)
    }

    fun consume() {
        _latestBroadcast.value = null
    }
}
