package com.obrago.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.obrago.app.data.model.Bid
import com.obrago.app.data.model.Job
import com.obrago.app.data.model.JobStatus
import com.obrago.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Mirrors the job/bid lifecycle functions in src/store.tsx:
 * postJob, cancelJob, acceptBid, markWorkerArrived, cancelJobAfterArrival,
 * completeJob, submitBid, submitCounterOffer.
 */
class JobRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val jobsCollection get() = db.collection("jobs")
    private val bidsCollection get() = db.collection("bids")
    private val usersCollection get() = db.collection("users")

    fun observeJobs(): Flow<List<Job>> = callbackFlow {
        val reg = jobsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.documents?.mapNotNull { it.toObject(Job::class.java) } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun observeBids(): Flow<List<Bid>> = callbackFlow {
        val reg = bidsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.documents?.mapNotNull { it.toObject(Bid::class.java) } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    /** Mirrors postJob(). */
    suspend fun postJob(
        customerId: String,
        category: String,
        description: String,
        location: String,
        city: String,
        locationCoords: List<Double>?,
        budget: Double
    ): Result<Job> {
        val newJob = Job(
            id = "job_${System.currentTimeMillis()}",
            customerId = customerId,
            category = category,
            description = description,
            location = location,
            city = city,
            locationCoords = locationCoords,
            budget = budget,
            status = JobStatus.BIDDING.value,
            createdAt = System.currentTimeMillis()
        )
        return try {
            jobsCollection.document(newJob.id).set(newJob.toFirestoreMap()).await()
            Result.success(newJob)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Mirrors cancelJob(). */
    suspend fun cancelJob(jobId: String, reason: String?) {
        try {
            val updates = mutableMapOf<String, Any>("status" to JobStatus.CANCELLED.value)
            if (!reason.isNullOrBlank()) updates["cancelReason"] = reason
            jobsCollection.document(jobId).update(updates).await()
        } catch (e: Exception) {
            // Same fallback the web app uses if updateDoc fails
            try { jobsCollection.document(jobId).delete().await() } catch (_: Exception) {}
        }
    }

    /** Mirrors acceptBid(). */
    suspend fun acceptBid(jobId: String, bid: Bid) {
        jobsCollection.document(jobId).update(
            mapOf(
                "status" to JobStatus.ACCEPTED.value,
                "workerId" to bid.workerId,
                "acceptedBidId" to bid.id
            )
        ).await()
    }

    /** Mirrors markWorkerArrived() - used by the worker-side app in a later phase, kept here for completeness. */
    suspend fun markWorkerArrived(jobId: String) {
        jobsCollection.document(jobId).update("workerArrived", true).await()
    }

    /**
     * Mirrors cancelJobAfterArrival(): applies a 50% penalty to the customer
     * (deducted from points, added to penaltyFee) or a 5% compensation to the
     * worker, depending on who is currently logged in - exactly like the web app.
     */
    suspend fun cancelJobAfterArrival(job: Job, currentUser: User, acceptedBid: Bid?): Result<User> {
        return try {
            val penaltyAmount = job.budget * 0.5
            val compensationAmount = Math.ceil(job.budget * 0.05)

            var updatedUser = currentUser
            if (currentUser.role == "customer" && currentUser.id == job.customerId) {
                val newPts = maxOf(0L, currentUser.points - penaltyAmount.toLong())
                val newPenalty = (currentUser.penaltyFee ?: 0.0) + penaltyAmount
                usersCollection.document(currentUser.id)
                    .update(mapOf("points" to newPts, "penaltyFee" to newPenalty)).await()
                updatedUser = currentUser.copy(points = newPts, penaltyFee = newPenalty)
            } else if (currentUser.role == "worker" && acceptedBid != null && acceptedBid.workerId == currentUser.id) {
                val newPts = currentUser.points + compensationAmount.toLong()
                usersCollection.document(currentUser.id).update("points", newPts).await()
                updatedUser = currentUser.copy(points = newPts)
            }

            jobsCollection.document(job.id).update("status", JobStatus.CANCELLED.value).await()
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Mirrors completeJob(). */
    suspend fun completeJob(jobId: String) {
        jobsCollection.document(jobId).update("status", JobStatus.COMPLETED.value).await()
    }

    /** Mirrors submitBid() - used by the worker-side app in a later phase, kept here for completeness. */
    suspend fun submitBid(bid: Bid): Result<Bid> {
        val newBid = bid.copy(id = "bid_${System.currentTimeMillis()}", createdAt = System.currentTimeMillis())
        return try {
            bidsCollection.document(newBid.id).set(newBid.toFirestoreMap()).await()
            Result.success(newBid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Mirrors submitCounterOffer(). */
    suspend fun submitCounterOffer(bidId: String, counterPrice: Double, counterMessage: String?) {
        bidsCollection.document(bidId).update(
            mapOf(
                "counterPrice" to counterPrice,
                "counterMessage" to (counterMessage ?: "Counter proposal: $counterPrice")
            )
        ).await()
    }

    /** Mirrors submitRating(): saves the rating doc and updates the target user's average rating. */
    suspend fun submitRating(jobId: String, fromUserId: String, toUserId: String, stars: Int, comment: String, targetCurrentRating: Double, targetCompletedJobs: Long): Result<Unit> {
        return try {
            val ratingId = "rat_${System.currentTimeMillis()}"
            db.collection("ratings").document(ratingId).set(
                mapOf(
                    "id" to ratingId,
                    "jobId" to jobId,
                    "fromUserId" to fromUserId,
                    "toUserId" to toUserId,
                    "stars" to stars,
                    "comment" to comment,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()

            val newAvg = if (targetCurrentRating > 0) {
                Math.round(((targetCurrentRating + stars) / 2.0) * 10) / 10.0
            } else stars.toDouble()

            usersCollection.document(toUserId).update(
                mapOf("rating" to newAvg, "completedJobs" to targetCompletedJobs + 1)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Mirrors acceptCounterOffer(): worker accepts the customer's counter price. */
    suspend fun acceptCounterOffer(bid: Bid) {
        bidsCollection.document(bid.id).update(
            mapOf(
                "price" to (bid.counterPrice ?: bid.price),
                "counterPrice" to null,
                "counterMessage" to null
            )
        ).await()
    }

    /** Mirrors the periodic workerLocationCoords sync in WorkerActiveJob (src/WorkerApp.tsx). */
    suspend fun updateWorkerLocation(jobId: String, coords: List<Double>) {
        jobsCollection.document(jobId).update("workerLocationCoords", coords).await()
    }

    /** Mirrors addPoints()/deductPoints() applied to the current user. */
    suspend fun adjustPoints(userId: String, delta: Long): Result<Long> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val current = snapshot.getLong("points") ?: 0L
            val updated = maxOf(0L, current + delta)
            usersCollection.document(userId).update("points", updated).await()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
