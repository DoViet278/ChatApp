package com.example.chatapplication.data.model

data class CallSession(
    val callId: String = "",
    val roomId: String = "",
    val callerId: String = "",
    val targetIds: List<String> = emptyList(),
    val callType: String = "voice",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "ongoing"
)
