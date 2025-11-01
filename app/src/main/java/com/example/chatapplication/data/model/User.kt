package com.example.chatapplication.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val sdt: String = "",
    val birthday: String = "",
    val avtUrl: String = "",
    val isOnline: Boolean = false,
)
