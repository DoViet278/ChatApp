package com.example.chatapplication.data.model

data class ChatRoom(
    val chatroomId: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageTimestamp: Long = 0L,
    val isGroup: Boolean = false,
    val groupName: String? = null,
    val userIds: List<String> = emptyList()
)
