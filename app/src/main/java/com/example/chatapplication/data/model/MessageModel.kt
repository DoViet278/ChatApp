package com.example.chatapplication.data.model

data class MessageModel(
    val message : String,
    val role : String,
    val timestamp: Long,
    val userId: String
)
