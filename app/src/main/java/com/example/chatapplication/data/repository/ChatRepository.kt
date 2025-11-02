package com.example.chatapplication.data.repository

import android.util.Log
import com.example.chatapplication.SupabaseManager
import com.example.chatapplication.data.model.ChatMessage
import com.example.chatapplication.data.model.ChatRoom
import com.example.chatapplication.data.model.User
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val chatRoomRef = db.collection("chatrooms")
    private val userRef = db.collection("users")

    suspend fun sendMessage(roomId: String, message: ChatMessage) {
        val docRef = chatRoomRef.document(roomId)
            .collection("chats")
            .document()

        val msgWithId = message.copy(messageId = docRef.id)

        docRef.set(msgWithId).await()

        chatRoomRef.document(roomId).update(
            mapOf(
                "lastMessage" to msgWithId.message,
                "lastMessageSenderId" to msgWithId.senderId,
                "lastMessageTimestamp" to msgWithId.timestamp,
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

//    suspend fun sendFileMessage(roomId: String, senderId: String, senderName: String, senderAvatar: String, fileUrl: String, fileType: String) {
//        val message = ChatMessage(
//            senderId = senderId,
//            senderName = senderName,
//            senderAvatar = senderAvatar,
//            messageType = fileType,
//            fileUrl = fileUrl
//        )
//        sendMessage(roomId, message)
//    }
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
            adminIds = listOf(currentUserId),
            groupName = groupName,
            group = true,
            groupAvt = "https://picsum.photos/id/1/200/300"
        )

        docRef.set(newRoom).await()
        return docRef.id
    }

    fun getChatRoomInfo(roomId: String) = callbackFlow {
        val listener = chatRoomRef.document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val room = snapshot?.toObject(ChatRoom::class.java)
                trySend(room)
            }
        awaitClose { listener.remove() }
    }

    fun getUsersInChatRoom(userIds: List<String>) = callbackFlow {
        if (userIds.isEmpty()) {
            trySend(emptyList<User>())
            close()
            return@callbackFlow
        }

        val listener = userRef
            .whereIn(FieldPath.documentId(), userIds)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users)
            }

        awaitClose { listener.remove() }
    }

    suspend fun deleteChatRoom(roomId: String) {
        val roomRef = db.collection("chatrooms").document(roomId)

        val messages = roomRef.collection("chats").get().await()
        for (doc in messages.documents) {
            doc.reference.delete().await()
        }

        roomRef.delete().await()
    }

    //update group
    suspend fun updateGroupName(roomId: String, newName: String) {
        db.collection("chatrooms")
            .document(roomId)
            .update("groupName", newName)
            .await()
    }

    suspend fun updateGroupAvatar(roomId: String, url: String) {
        db.collection("chatrooms")
            .document(roomId)
            .update("groupAvt", url)
            .await()
    }

    suspend fun addMemberByEmail(roomId: String, email: String): Boolean {
        val userSnap = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()

        if (userSnap.isEmpty) return false

        val uid = userSnap.documents[0].id

        db.collection("chatrooms")
            .document(roomId)
            .update("userIds", FieldValue.arrayUnion(uid))
            .await()

        return true
    }

    suspend fun removeMember(roomId: String, userId: String) {
        db.collection("chatrooms")
            .document(roomId)
            .update("userIds", FieldValue.arrayRemove(userId))
            .await()
    }

    suspend fun uploadAvatar(inputSteam: InputStream, fileExtension: String): String {
        val bucket = SupabaseManager.client.storage.from("imgAvt")
        val fileName = "${UUID.randomUUID()}.$fileExtension"
        bucket.upload(path = fileName, data = inputSteam.readBytes())
        return bucket.publicUrl(fileName)
    }


}
