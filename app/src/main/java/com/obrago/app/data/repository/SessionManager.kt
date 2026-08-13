package com.obrago.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.obrago.app.data.model.User
import com.obrago.app.notifications.FcmTokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * App-wide equivalent of `currentUser` / `isLoggedIn` in src/store.tsx.
 */
object SessionManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("obrago_session", Context.MODE_PRIVATE)
        val savedUserId = prefs?.getString("user_id", null)
        
        if (savedUserId != null) {
            scope.launch {
                try {
                    val snapshot = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(savedUserId)
                        .get()
                        .await()
                    
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        _currentUser.value = user
                        _isLoggedIn.value = true
                        // Ensure FCM token stays fresh on app launch
                        FcmTokenManager.registerTokenForUser(user.id)
                    } else {
                        logout()
                    }
                } catch (e: Exception) {
                    Log.e("SessionManager", "Failed to restore session", e)
                }
            }
        }
    }

    fun login(user: User) {
        _currentUser.value = user
        _isLoggedIn.value = true
        prefs?.edit()?.putString("user_id", user.id)?.apply()
        
        // Register this device's FCM token against the user doc so a future
        // Cloud Function can target them for server-sent push.
        scope.launch { FcmTokenManager.registerTokenForUser(user.id) }
    }

    fun updateCurrentUser(user: User) {
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        prefs?.edit()?.remove("user_id")?.apply()
    }
}
