package com.obrago.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class Broadcast(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val targetRole: String = "all",
    val createdAt: Long = 0L
)

/**
 * Mirrors the receiving side of sendAdminBroadcast() in store.tsx: the admin
 * writes a doc to `broadcasts/{id}`, and every signed-in client listens for
 * new ones targeted at their role while the app is in the foreground.
 */
class BroadcastRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    /** Emits only broadcasts created after [sinceTimestamp], newest first. */
    fun observeBroadcastsSince(sinceTimestamp: Long): Flow<List<Broadcast>> = callbackFlow {
        val reg = db.collection("broadcasts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toObject(Broadcast::class.java) }
                    ?.filter { it.createdAt > sinceTimestamp } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }
}
