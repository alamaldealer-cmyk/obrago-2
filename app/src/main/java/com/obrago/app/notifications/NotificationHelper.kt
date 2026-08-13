package com.obrago.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.obrago.app.MainActivity
import com.obrago.app.data.model.Job

/**
 * Native equivalent of src/notificationService.ts: notification channel setup,
 * the alert chime + vibration pattern, and system notification/lock-screen
 * alerts for broadcasts and new nearby jobs.
 */
object NotificationHelper {

    const val CHANNEL_ID = "obrago_alerts"
    private const val CHANNEL_NAME = "Obrago Alerts"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Job alerts, broadcasts and account notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 100, 300, 100, 400)
            }
            manager?.createNotificationChannel(channel)
        }
    }

    /** Mirrors playJobAlertChime(): a short multi-tone alert sound. */
    fun playChime() {
        try {
            val tone = ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 90)
            tone.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 350)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                tone.release()
            }, 500)
        } catch (e: Exception) {
            // Non-fatal - device may not support ToneGenerator
        }
    }

    /** Mirrors the navigator.vibrate([300,100,300,100,400]) pattern. */
    fun vibrate(context: Context) {
        val pattern = longArrayOf(0, 300, 100, 300, 100, 400)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(VibratorManager::class.java)
                manager?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            // Non-fatal
        }
    }

    /** Mirrors triggerSystemCustomNotification(): chime + vibrate + lock-screen notification. */
    fun showNotification(context: Context, title: String, body: String, notificationId: Int = System.currentTimeMillis().toInt()) {
        ensureChannel(context)
        playChime()
        vibrate(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted - caller should have requested it already
        }
    }

    /** Mirrors triggerSystemJobAlert(): new nearby job alert for workers. */
    fun showJobAlert(context: Context, job: Job, currency: String = "Rs") {
        val title = "\uD83D\uDEA8 New Job Alert: ${job.description.ifBlank { job.category }}"
        val body = "\uD83D\uDCB0 Budget: $currency ${job.budget.toInt()}\n\uD83D\uDCCD ${job.location}\n\u26A1 Open Obrago to view and bid!"
        showNotification(context, title, body, notificationId = job.id.hashCode())
    }
}
