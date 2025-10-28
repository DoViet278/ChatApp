package com.example.chatapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.data.model.ChatRoom
import com.example.chatapplication.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    fun listenChatRooms(userId: String) {
        viewModelScope.launch {
            chatRepository.getUserChatRooms(userId).collectLatest {
                _chatRooms.value = it
            }
        }
    }

    fun createOneToOneChat(
        currentUserId: String,
        email: String,
        onResult: (String, String) -> Unit
    ) {
        viewModelScope.launch {
            val (chatId, otherUserId) = chatRepository.createOneToOneChatByEmail(currentUserId, email)
            onResult(chatId, otherUserId)
        }
    }

    fun createGroupChat(
        currentUserId: String,
        memberEmails: List<String>,
        groupName: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            val chatId = chatRepository.createGroupChatByEmails(currentUserId, memberEmails, groupName)
            onResult(chatId)
        }
    }

}
