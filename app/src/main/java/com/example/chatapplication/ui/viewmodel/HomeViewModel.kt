package com.example.chatapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.data.model.ChatRoom
import com.example.chatapplication.data.model.User
import com.example.chatapplication.data.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _onlineUsers = MutableStateFlow<List<User>>(emptyList())
    val onlineUsers: StateFlow<List<User>> = _onlineUsers
    private val userCache = mutableMapOf<String, User>()

    init {
        listenOnlineUsers()
    }

    fun cacheUser(user: User) {
        userCache[user.uid] = user
    }

    fun getCachedUserName(id: String?): String {
        return id?.let { userCache[it]?.name } ?: ""
    }

    private fun listenOnlineUsers() {
        db.collection("users")
            .whereEqualTo("isOnline", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("HomeVM", "Lỗi lấy user online: ${e.message}")
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
                _onlineUsers.value = users
            }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("HomeVM", "Lỗi lấy user: ${e.message}")
            null
        }
    }

    fun listenChatRooms(userId: String) {
        viewModelScope.launch {
            chatRepository.getUserChatRooms(userId).collectLatest {
                _chatRooms.value = it
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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
