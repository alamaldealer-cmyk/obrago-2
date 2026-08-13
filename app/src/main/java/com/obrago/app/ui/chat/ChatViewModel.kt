package com.obrago.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obrago.app.data.model.ChatMessage
import com.obrago.app.data.model.CommunicationTarget
import com.obrago.app.data.repository.ChatRepository
import com.obrago.app.data.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import android.content.Context
import android.net.Uri
import com.obrago.app.data.model.Job
import java.io.File
import java.io.FileOutputStream

data class ChatUiState(
    val target: CommunicationTarget? = null,
    val associatedJob: Job? = null,
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = ""
)

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentChatId: String? = null

    fun open(target: CommunicationTarget) {
        _uiState.value = _uiState.value.copy(target = target, associatedJob = null, messages = emptyList())
        val userId = SessionManager.currentUser.value?.id ?: "guest"
        val chatId = chatRepository.chatIdFor(target, userId)
        currentChatId = chatId

        if (!target.jobId.isNullOrBlank()) {
            viewModelScope.launch {
                val job = chatRepository.fetchJob(target.jobId)
                if (job != null) {
                    _uiState.value = _uiState.value.copy(associatedJob = job)
                }
            }
        }

        chatRepository.observeMessages(chatId)
            .onEach { msgs -> _uiState.value = _uiState.value.copy(messages = msgs) }
            .launchIn(viewModelScope)
    }

    fun setDraft(v: String) {
        _uiState.value = _uiState.value.copy(draft = v)
    }

    fun send() {
        val text = _uiState.value.draft.trim()
        val chatId = currentChatId ?: return
        if (text.isBlank()) return
        val user = SessionManager.currentUser.value
        val senderId = user?.id ?: "guest"
        val senderName = user?.name ?: "Guest User"

        _uiState.value = _uiState.value.copy(draft = "")
        viewModelScope.launch {
            chatRepository.sendMessage(chatId, text, senderId, senderName)
        }
    }

    fun sendImage(context: Context, uri: Uri) {
        val chatId = currentChatId ?: return
        val user = SessionManager.currentUser.value
        val senderId = user?.id ?: "guest"
        val senderName = user?.name ?: "Guest User"

        viewModelScope.launch {
            val savedPath = saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                chatRepository.sendMessage(
                    chatId = chatId,
                    text = "",
                    senderId = senderId,
                    senderName = senderName,
                    imageUrl = savedPath
                )
            }
        }
    }

    private fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.filesDir, "chat_media_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
