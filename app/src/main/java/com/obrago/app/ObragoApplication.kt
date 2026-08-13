package com.obrago.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.obrago.app.data.repository.SessionManager
import com.obrago.app.notifications.NotificationHelper

class ObragoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        SessionManager.init(this)
        NotificationHelper.ensureChannel(this)
    }
}
