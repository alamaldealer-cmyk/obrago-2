package com.obrago.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.obrago.app.data.model.ChatMessage
import com.obrago.app.data.model.CommunicationTarget
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import com.obrago.app.data.model.Job

/**
 * Mirrors the Firestore chat structure in src/CommunicationModals.tsx:
 * chats/{chatId}/messages, where chatId is `job_{jobId}` when tied to a job,
 * or the two user ids sorted + joined otherwise.
 */
class ChatRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun chatIdFor(target: CommunicationTarget, currentUserId: String): String {
        return when {
            target.jobId != null -> "job_${target.jobId}"
            target.userId != null -> listOf(currentUserId, target.userId).sorted().joinToString("_")
            else -> "general_chat"
        }
    }

    fun observeMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val reg = db.collection("chats").document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun fetchJob(jobId: String): Job? {
        return try {
            val doc = db.collection("jobs").document(jobId).get().await()
            doc.toObject(Job::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendMessage(
        chatId: String,
        text: String,
        senderId: String,
        senderName: String,
        imageUrl: String? = null
    ) {
        val now = System.currentTimeMillis()
        val messagesRef = db.collection("chats").document(chatId).collection("messages")
        val msgData = mutableMapOf<String, Any>(
            "text" to text,
            "senderId" to senderId,
            "senderName" to senderName,
            "createdAt" to now
        )
        if (!imageUrl.isNullOrBlank()) {
            msgData["imageUrl"] = imageUrl
        }
        messagesRef.add(msgData).await()

        val lastMsg = if (!imageUrl.isNullOrBlank() && text.isBlank()) "📷 Photo" else text
        db.collection("chats").document(chatId).set(
            mapOf(
                "id" to chatId,
                "lastMessage" to lastMsg,
                "updatedAt" to now
            )
        ).await()
    }
}
