package com.example.chatapplication.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.Constants
import com.example.chatapplication.data.model.ChatMessage
import com.example.chatapplication.data.model.MessageModel
import com.example.chatapplication.data.repository.BotRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val chatRepository: BotRepository
) : ViewModel() {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    val generativeModel : GenerativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = Constants.apiKey
    )


    fun loadHistory(userId: String) {
        viewModelScope.launch {
            try {
                val history = chatRepository.getMessages(userId)
                messageList.clear()
                messageList.addAll(history)
            } catch (e: Exception) {
                messageList.add(
                    MessageModel(
                        message = "Error loading history: ${e.message}",
                        role = "model",
                        timestamp = System.currentTimeMillis(),
                        userId = userId
                    )
                )
            }
        }
    }


    fun sendMessage(userId: String, text: String) {
        viewModelScope.launch {
            try {
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) { text(it.message) }
                    }
                )

                val userMsg = MessageModel(
                    message = text,
                    role = "user",
                    timestamp = System.currentTimeMillis(),
                    userId = userId
                )
                messageList.add(userMsg)
                chatRepository.saveMessage(userMsg)

                val typingMsg = MessageModel(
                    message = "Typing...",
                    role = "model",
                    timestamp = System.currentTimeMillis(),
                    userId = userId
                )
                messageList.add(typingMsg)

                val response = chat.sendMessage(text)

                messageList.remove(typingMsg)

                val botMsg = MessageModel(
                    message = response.text.orEmpty(),
                    role = "model",
                    timestamp = System.currentTimeMillis(),
                    userId = userId
                )
                messageList.add(botMsg)
                chatRepository.saveMessage(botMsg)

            } catch (e: Exception) {
                messageList.add(
                    MessageModel(
                        message = "Error: ${e.message}",
                        role = "model",
                        timestamp = System.currentTimeMillis(),
                        userId = userId
                    )
                )
            }
        }
    }
}
