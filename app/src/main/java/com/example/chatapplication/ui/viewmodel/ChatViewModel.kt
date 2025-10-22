package com.example.chatapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import im.zego.zim.entity.ZIMConversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ZIMConversation>>(emptyList())
    val conversations: StateFlow<List<ZIMConversation>> = _conversations

    fun loadConversations() {
        viewModelScope.launch {
            _conversations.value = repository.getConversations()
        }
    }
}
