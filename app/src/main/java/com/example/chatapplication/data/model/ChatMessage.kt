package com.example.chatapplication.data.model

data class ChatMessage(
    val message: String = "",
    val senderId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "text",
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    //call
    val callId: String? = null,
    val callType: String? = null,
    val callStatus: String? = null
)
