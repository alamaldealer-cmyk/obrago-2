package com.obrago.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.obrago.app.data.model.SupportMessage
import com.obrago.app.data.model.SupportTicket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SupportTicketRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val ticketsCollection get() = db.collection("support_tickets")

    fun observeAllTickets(): Flow<List<SupportTicket>> = callbackFlow {
        val reg = ticketsCollection
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(SupportTicket::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observeUserTickets(userId: String): Flow<List<SupportTicket>> = callbackFlow {
        val reg = ticketsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(SupportTicket::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.updatedAt } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun createTicket(ticket: SupportTicket): String {
        val id = if (ticket.id.isBlank()) "ticket_${System.currentTimeMillis()}" else ticket.id
        val now = System.currentTimeMillis()
        val doc = ticket.copy(id = id, createdAt = now, updatedAt = now)
        ticketsCollection.document(id).set(doc).await()
        return id
    }

    suspend fun addMessageToTicket(ticketId: String, currentTicket: SupportTicket, message: SupportMessage) {
        val updatedMessages = currentTicket.messages + message
        ticketsCollection.document(ticketId).update(
            mapOf(
                "messages" to updatedMessages,
                "updatedAt" to System.currentTimeMillis(),
                "status" to if (message.isAdmin) "in_progress" else "open"
            )
        ).await()
    }

    suspend fun updateTicketStatus(ticketId: String, newStatus: String) {
        ticketsCollection.document(ticketId).update(
            mapOf(
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }
}
