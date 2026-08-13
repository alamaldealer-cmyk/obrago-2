package com.obrago.app.notifications

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.obrago.app.data.repository.SessionManager

/**
 * Native FCM equivalent of the (disabled-on-web) setupFirebasePushNotifications()
 * in notificationService.ts. The original web app deliberately skipped real FCM
 * registration to avoid a crash on generic APK builds without google-services.json.
 * This native project ships with a real google-services.json, so real push works.
 *
 * To actually deliver server-sent push (e.g. when Admin sends a broadcast, or
 * when a new job is posted while the intended worker's app is killed/backgrounded),
 * you'll want a small Cloud Function that listens on the `broadcasts` and `jobs`
 * collections and calls the FCM Admin SDK - this service only handles the
 * client side (token registration + displaying incoming messages).
 */
class ObragoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = SessionManager.currentUser.value?.id ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("fcmToken", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "Obrago"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        NotificationHelper.showNotification(applicationContext, title, body)
    }
}
