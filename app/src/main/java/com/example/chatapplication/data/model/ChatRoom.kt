package com.example.chatapplication.data.model

data class ChatRoom(
    val chatroomId: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageTimestamp: Long = 0L,
    val group: Boolean = false,
    val groupName: String? = null,
    val groupAvt: String? = null,
    val userIds: List<String> = emptyList(),
    val adminIds: List<String> = emptyList(),
    val lastMessage: String = ""
)
