package com.example.chatapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.data.model.ChatRoom
import com.example.chatapplication.data.model.User
import com.example.chatapplication.data.repository.ChatRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class GroupChatInfoViewModel @Inject constructor(
    private val repo: ChatRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _chatRoom = MutableStateFlow<ChatRoom?>(null)
    val chatRoom: StateFlow<ChatRoom?> = _chatRoom

    private val _members = MutableStateFlow<List<User>>(emptyList())
    val members: StateFlow<List<User>> = _members

    fun loadGroup(roomId: String) {
        db.collection("chatrooms").document(roomId)
            .addSnapshotListener { snapshot, _ ->
                val room = snapshot?.toObject(ChatRoom::class.java)
                _chatRoom.value = room

                viewModelScope.launch {
                    room?.let {
                        val users = it.userIds.mapNotNull { uid ->
                            db.collection("users").document(uid).get().await()
                                .toObject(User::class.java)?.copy(uid = uid)
                        }
                        _members.value = users
                    }
                }
            }
    }

    fun updateName(roomId: String, newName: String) {
        viewModelScope.launch { repo.updateGroupName(roomId, newName) }
    }

    fun uploadAvatar(roomId: String, inputStream: InputStream, extension: String) {
        viewModelScope.launch {
            try {
                val url = repo.uploadAvatar(inputStream, extension)
                repo.updateGroupAvatar(roomId, url)
            } catch (e: Exception) {
                Log.e("GroupInfoVM", "Upload lỗi: ${e.message}")
            }
        }
    }

    fun addMember(roomId: String, email: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repo.addMemberByEmail(roomId, email)
            onDone(ok)
        }
    }

    fun removeMember(roomId: String, uid: String) {
        viewModelScope.launch {
            repo.removeMember(roomId, uid)
        }
    }

    fun addAdmin(roomId: String, userId: String) {
        viewModelScope.launch {
            db.collection("chatrooms")
                .document(roomId)
                .update("adminIds", FieldValue.arrayUnion(userId))
        }
    }

    fun removeAdmin(roomId: String, userId: String) {
        viewModelScope.launch {
            db.collection("chatrooms")
                .document(roomId)
                .update("adminIds", FieldValue.arrayRemove(userId))
        }
    }

}
