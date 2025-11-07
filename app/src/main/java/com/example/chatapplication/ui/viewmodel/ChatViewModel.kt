package com.example.chatapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.data.model.ChatMessage
import com.example.chatapplication.data.model.ChatRoom
import com.example.chatapplication.data.model.User
import com.example.chatapplication.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _chatRoom = MutableStateFlow<ChatRoom?>(null)
    val chatRoom: StateFlow<ChatRoom?> = _chatRoom

    private val _usersInRoom = MutableStateFlow<List<User>>(emptyList())
    val usersInRoom: StateFlow<List<User>> = _usersInRoom

    fun listenMessages(roomId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(roomId).collectLatest {
                _messages.value = it
            }
        }
    }

    fun sendMessage(roomId: String, senderId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(
                roomId,
                ChatMessage(
                    message = text,
                    senderId = senderId,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun markAsRead(roomId: String, userId: String) {
        viewModelScope.launch {
            chatRepository.markAsRead(roomId, userId)
        }
    }

    fun listenChatRoomInfo(roomId: String) {
        viewModelScope.launch {
            chatRepository.getChatRoomInfo(roomId).collect { room ->
                _chatRoom.value = room
                room?.let {
                    listenUsersInRoom(it.userIds)
                }
            }
        }
    }

    private fun listenUsersInRoom(userIds: List<String>) {
        viewModelScope.launch {
            chatRepository.getUsersInChatRoom(userIds).collect { users ->
                _usersInRoom.value = users
            }
        }
    }

    fun sendImage(roomId: String, userId: String, input: InputStream, ext: String) {
        viewModelScope.launch {
            val url = chatRepository.uploadFileToSupabase(input, ext)
            chatRepository.sendImageMessage(roomId, userId, url)
        }
    }

    fun sendFile(roomId: String, userId: String, input: InputStream, name: String, ext: String, size: Long) {
        viewModelScope.launch {
            val url = chatRepository.uploadFileToSupabase(input, ext)
            chatRepository.sendFileMessage(roomId, userId, url, name, size)
        }
    }

    fun sendAudio(roomId: String, userId: String, input: InputStream, ext: String) {
        viewModelScope.launch {
            val url = chatRepository.uploadFileToSupabase(input, ext)
            chatRepository.sendAudioMessage(roomId, userId, url)
        }
    }

    fun sendVideo(roomId: String, userId: String, input: InputStream, ext: String) {
        viewModelScope.launch {
            val url = chatRepository.uploadFileToSupabase(input, ext)
            chatRepository.sendVideoMessage(roomId, userId, url)
        }
    }

    //call
    fun startVoiceCall(roomId: String, myId: String, otherId: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val callId = chatRepository.startOneToOneCall(roomId, myId, otherId, "voice")
            onResult(callId)
        }
    }

    fun startVideoCall(roomId: String, myId: String, otherId: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val callId = chatRepository.startOneToOneCall(roomId, myId, otherId, "video")
            onResult(callId)
        }
    }

}

