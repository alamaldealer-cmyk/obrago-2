package com.obrago.app.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Mirrors the OTP logic in src/AuthFlow.tsx (handleSendOtp / handleVerifyOtp).
 *
 * Behaviour kept identical to the web app:
 *  - Tries real Firebase Phone Auth SMS OTP first.
 *  - If Firebase SMS fails (quota, unverified number, emulator, etc.) it falls back
 *    to a locally generated 6-digit code (or the universal test code "123456"),
 *    exactly like the original React code did with `fallbackCode`.
 */
sealed class OtpEvent {
    data class CodeSent(val verificationId: String) : OtpEvent()
    data class AutoVerified(val credential: PhoneAuthCredential) : OtpEvent()
    data class Failed(val message: String) : OtpEvent()
    data class FallbackMode(val fallbackCode: String) : OtpEvent()
}

class PhoneAuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    /** Generates the same kind of 6-digit local fallback code used by the web app. */
    fun generateFallbackCode(): String = (100000..999999).random().toString()

    fun sendOtp(activity: Activity, fullPhoneNumber: String): Flow<OtpEvent> = callbackFlow {
        val fallback = generateFallbackCode()

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                trySend(OtpEvent.AutoVerified(credential))
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // Same fallback behaviour as the web app's catch block:
                // don't hard-fail the whole flow, allow manual/fallback OTP entry
                // unless it's clearly an invalid phone number.
                if (e.message?.contains("INVALID_PHONE_NUMBER") == true ||
                    e.message?.contains("invalid-phone-number") == true
                ) {
                    trySend(OtpEvent.Failed("Invalid phone number format. Please check your phone digits."))
                } else {
                    trySend(OtpEvent.FallbackMode(fallback))
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                resendToken = token
                trySend(OtpEvent.CodeSent(verificationId))
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(fullPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        awaitClose { }
    }

    /**
     * Verifies user-entered OTP. Mirrors handleVerifyOtp: real Firebase confirmation
     * first, falling back to the local fallback code or the universal "123456" test code.
     */
    suspend fun verifyOtp(
        verificationId: String?,
        enteredCode: String,
        fallbackCode: String
    ): Result<Boolean> {
        // Fallback / offline path — identical rule to the web app.
        if (enteredCode == "123456" || enteredCode == fallbackCode) {
            return Result.success(true)
        }

        if (verificationId == null) {
            return Result.failure(Exception("Incorrect or expired 6-digit OTP code."))
        }

        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, enteredCode)
            auth.signInWithCredential(credential).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
