package com.example.chatapplication.data.repository

import android.util.Log
import com.example.chatapplication.data.model.ChatMessage
import com.example.chatapplication.data.model.ChatRoom
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val chatRoomRef = db.collection("chatrooms")
    private val userRef = db.collection("users")

    suspend fun sendMessage(roomId: String, message: ChatMessage) {
        chatRoomRef.document(roomId)
            .collection("chats")
            .add(message)
            .await()

        chatRoomRef.document(roomId).update(
            mapOf(
                "lastMessageSenderId" to message.senderId,
                "lastMessageTimestamp" to message.timestamp
            )
        )
    }

    fun getMessages(roomId: String) = callbackFlow {
        val listener = chatRoomRef.document(roomId)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    fun getUserChatRooms(userId: String) = callbackFlow {
        val listener = chatRoomRef
            .whereArrayContains("userIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val rooms = snapshot?.toObjects(ChatRoom::class.java) ?: emptyList()
                trySend(rooms)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createOneToOneChatByEmail(
        currentUserId: String,
        otherUserEmail: String
    ): Pair<String, String> {
        val userSnapshot = userRef
            .whereEqualTo("email", otherUserEmail)
            .get()
            .await()

        if (userSnapshot.isEmpty) {
            Log.e("ChatRepo", "Không tìm thấy user với email $otherUserEmail")
            return "" to ""
        }

        val otherUserId = userSnapshot.documents.first().id

        val existing = chatRoomRef
            .whereArrayContains("userIds", currentUserId)
            .get()
            .await()
            .documents.find { doc ->
                val users = doc.get("userIds") as? List<*>
                users?.contains(otherUserId) == true
            }

        if (existing != null) {
            return existing.id to otherUserId
        }

        val docRef = chatRoomRef.document()
        val newRoom = ChatRoom(
            chatroomId = docRef.id,
            lastMessageSenderId = "",
            lastMessageTimestamp = System.currentTimeMillis(),
            userIds = listOf(currentUserId, otherUserId)
        )

        docRef.set(newRoom).await()
        return docRef.id to otherUserId
    }

    suspend fun createGroupChatByEmails(
        currentUserId: String,
        memberEmails: List<String>,
        groupName: String
    ): String {
        if (memberEmails.isEmpty()) return ""

        val snapshot = userRef.whereIn("email", memberEmails).get().await()
        val memberIds = snapshot.documents.map { it.id }

        val userIds = mutableListOf(currentUserId)
        userIds.addAll(memberIds)

        val docRef = chatRoomRef.document()
        val newRoom = ChatRoom(
            chatroomId = docRef.id,
            lastMessageSenderId = "",
            lastMessageTimestamp = System.currentTimeMillis(),
            userIds = userIds,
            groupName = groupName,
            isGroup = true
        )

        docRef.set(newRoom).await()
        return docRef.id
    }

}
