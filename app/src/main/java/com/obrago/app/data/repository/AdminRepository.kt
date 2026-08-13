package com.obrago.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.obrago.app.data.model.AdminSettings
import com.obrago.app.data.model.Category
import com.obrago.app.data.model.DepositRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Mirrors the admin-only functions in src/store.tsx / src/AdminPanel.tsx:
 * verifyWorker, toggleBlockUser, deleteAccount, addCategory,
 * updateAdminSettings, processDepositRequest, refundPenalty, sendAdminBroadcast.
 */
class AdminRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val usersCollection get() = db.collection("users")
    private val categoriesCollection get() = db.collection("categories")
    private val depositRequestsCollection get() = db.collection("depositRequests")
    private val settingsDoc get() = db.collection("settings").document("admin")

    fun observeCategories(): Flow<List<Category>> = callbackFlow {
        val reg = categoriesCollection.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val list = snap?.documents?.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                Category(
                    id = doc.id,
                    name = d["name"] as? String ?: "",
                    icon = d["icon"] as? String ?: "Hammer",
                    isLongProject = d["isLongProject"] as? Boolean ?: false,
                    duration = d["duration"] as? String,
                    upfrontFee = (d["upfrontFee"] as? Number)?.toDouble()
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    fun observeDepositRequests(): Flow<List<DepositRequest>> = callbackFlow {
        val reg = depositRequestsCollection.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toObject(DepositRequest::class.java)?.copy(id = it.id) } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun observeAdminSettings(): Flow<AdminSettings> = callbackFlow {
        val reg = settingsDoc.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.toObject(AdminSettings::class.java) ?: AdminSettings())
        }
        awaitClose { reg.remove() }
    }

    /** Mirrors verifyWorker(). */
    suspend fun verifyWorker(userId: String, status: String) {
        usersCollection.document(userId).update("verificationStatus", status).await()
    }

    /** Mirrors toggleBlockUser(). */
    suspend fun toggleBlockUser(userId: String, currentlyBlocked: Boolean) {
        usersCollection.document(userId).update("isBlocked", !currentlyBlocked).await()
    }

    /** Mirrors deleteAccount(). */
    suspend fun deleteAccount(userId: String) {
        usersCollection.document(userId).delete().await()
    }

    /** Mirrors addCategory(). */
    suspend fun addCategory(category: Category): Result<Category> {
        val id = "cat_${System.currentTimeMillis()}"
        val withId = category.copy(id = id)
        return try {
            categoriesCollection.document(id).set(
                mapOf(
                    "id" to id,
                    "name" to withId.name,
                    "icon" to withId.icon,
                    "isLongProject" to withId.isLongProject,
                    "duration" to withId.duration,
                    "upfrontFee" to withId.upfrontFee
                ).filterValues { it != null }
            ).await()
            Result.success(withId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Mirrors updateAdminSettings(). */
    suspend fun updateAdminSettings(settings: AdminSettings) {
        settingsDoc.set(settings.toFirestoreMap(), SetOptions.merge()).await()
    }

    /** Mirrors processDepositRequest(): approving credits points to the user. */
    suspend fun processDepositRequest(request: DepositRequest, approve: Boolean, currentUserPoints: Long?) {
        if (approve && currentUserPoints != null) {
            usersCollection.document(request.userId).update("points", currentUserPoints + request.amount.toLong()).await()
        }
        depositRequestsCollection.document(request.id).update("status", if (approve) "approved" else "rejected").await()
    }

    /** Mirrors refundPenalty(). */
    suspend fun refundPenalty(userId: String) {
        usersCollection.document(userId).update("penaltyFee", 0.0).await()
    }

    /** Update job status as admin (e.g., cancel or force complete). */
    suspend fun updateJobStatus(jobId: String, newStatus: String) {
        db.collection("jobs").document(jobId).update("status", newStatus).await()
    }

    /** Manually adjust worker wallet points as admin. */
    suspend fun adjustUserPoints(userId: String, currentPoints: Long, delta: Long) {
        val newPoints = (currentPoints + delta).coerceAtLeast(0)
        usersCollection.document(userId).update("points", newPoints).await()
    }

    /** Mirrors sendAdminBroadcast(). */
    suspend fun sendAdminBroadcast(title: String, message: String, targetRole: String) {
        val id = "bcast_${System.currentTimeMillis()}"
        db.collection("broadcasts").document(id).set(
            mapOf(
                "id" to id,
                "title" to title,
                "message" to message,
                "targetRole" to targetRole,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }
}
