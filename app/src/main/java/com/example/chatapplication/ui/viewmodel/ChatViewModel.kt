package com.example.chatapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.data.model.ChatMessage
import com.example.chatapplication.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun listenMessages(roomId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(roomId).collectLatest {
                _messages.value = it
            }
        }
    }

    fun sendMessage(
        roomId: String,
        senderId: String,
        text: String,
        senderName: String? = null,
        senderAvatar: String? = null
    ) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val msg = ChatMessage(
                message = text,
                senderId = senderId,
                senderName = senderName,
                senderAvatar = senderAvatar,
                timestamp = System.currentTimeMillis()
            )
            chatRepository.sendMessage(roomId, msg)
        }
    }
}

