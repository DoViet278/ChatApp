package com.example.chatapplication.data.model

data class ChatMessage(
    val messageId: String = "",
    val message: String = "",
    val senderId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
