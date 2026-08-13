package com.obrago.app.notifications

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Fetches the current FCM token and saves it on the logged-in user's Firestore
 * document (`users/{id}.fcmToken`), so a future Cloud Function can target
 * specific users/roles for server-sent push (new job alerts, broadcasts, etc).
 */
object FcmTokenManager {
    suspend fun registerTokenForUser(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("fcmToken", token)
                .await()
        } catch (e: Exception) {
            // Non-fatal - push registration failing shouldn't block the app
        }
    }
}
