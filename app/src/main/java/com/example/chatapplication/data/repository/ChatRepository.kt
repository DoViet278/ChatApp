package com.example.chatapplication.data.repository

import android.util.Log
import com.example.chatapplication.SupabaseManager
import com.example.chatapplication.data.model.CallSession
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

    private val callRef = db.collection("calls")

    suspend fun sendMessage(roomId: String, message: ChatMessage) {
        val roomRef = chatRoomRef.document(roomId)
        roomRef.collection("chats").add(message).await()

        roomRef.update(
            mapOf(
                "lastMessage" to message.message,
                "lastMessageSenderId" to message.senderId,
                "lastMessageTimestamp" to message.timestamp
            )
        )

        val room = roomRef.get().await().toObject(ChatRoom::class.java)

        room?.userIds?.forEach { uid ->
            if (uid != message.senderId) {
                roomRef.update("unread.$uid", FieldValue.increment(1)).await()
            }
        }
    }

    suspend fun markAsRead(roomId: String, userId: String) {
        chatRoomRef.document(roomId)
            .update("unread.$userId", 0)
            .await()
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
            .documents
            .mapNotNull { it.toObject(ChatRoom::class.java) to it.id }
            .find { (room, _) ->
                room!!.group == false && room!!.userIds.contains(otherUserId)
            }

        if (existing != null) {
            val (room, roomId) = existing
            return roomId to otherUserId
        }

        val docRef = chatRoomRef.document()
        val newRoom = ChatRoom(
            chatroomId = docRef.id,
            lastMessageSenderId = "",
            lastMessageTimestamp = System.currentTimeMillis(),
            userIds = listOf(currentUserId, otherUserId),
            group = false
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

    suspend fun uploadFileToSupabase(
        inputStream: InputStream,
        extension: String
    ): String {
        val bucket = SupabaseManager.client.storage.from("chat_files")
        val fileName = "chat_${UUID.randomUUID()}.$extension"
        bucket.upload(
            path = fileName,
            data = inputStream.readBytes(),
            upsert = false
        )
        return bucket.publicUrl(fileName)
    }

    suspend fun sendImageMessage(
        roomId: String,
        senderId: String,
        imageUrl: String
    ) {
        val message = ChatMessage(
            senderId = senderId,
            message = "[Ảnh]",
            type = "image",
            fileUrl = imageUrl
        )
        sendMessage(roomId, message)
    }

    suspend fun  sendCallMessage(
        roomId: String,
        senderId: String
    ){
        val message = ChatMessage(
            senderId = senderId,
            message = "[Cuộc gọi]",
            type = "call"
        )
        sendMessage(roomId, message)

    }

    suspend fun sendFileMessage(
        roomId: String,
        senderId: String,
        fileUrl: String,
        fileName: String,
        fileSize: Long
    ) {
        val message = ChatMessage(
            senderId = senderId,
            message = "[Tệp $fileName]",
            type = "file",
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize
        )
        sendMessage(roomId, message)
    }
    suspend fun sendVideoMessage(
        roomId: String,
        senderId: String,
        videoUrl: String
    ) {
        val message = ChatMessage(
            senderId = senderId,
            message = "[Video]",
            type = "video",
            fileUrl = videoUrl
        )
        sendMessage(roomId, message)
    }
    suspend fun sendAudioMessage(
        roomId: String,
        senderId: String,
        audioUrl: String
    ) {
        val message = ChatMessage(
            senderId = senderId,
            message = "[Audio]",
            type = "audio",
            fileUrl = audioUrl
        )
        sendMessage(roomId, message)
    }

    suspend fun uploadAndSendFileMessage(
        roomId: String,
        senderId: String,
        inputStream: InputStream,
        fileName: String,
        extension: String,
        size: Long
    ) {
        val url = uploadFileToSupabase(inputStream, extension)
        sendFileMessage(roomId, senderId, url, fileName, size)
    }

    //call
    suspend fun createCallLog(
        roomId: String,
        callId: String,
        callerId: String,
        targetIds: List<String>,
        callType: String
    ) {
        val log = hashMapOf(
            "callId" to callId,
            "roomId" to roomId,
            "callerId" to callerId,
            "targetIds" to targetIds,
            "callType" to callType,
            "timestamp" to System.currentTimeMillis(),
            "status" to "ongoing"
        )

        FirebaseFirestore.getInstance()
            .collection("chatrooms")
            .document(roomId)
            .collection("calls")
            .document(callId)
            .set(log)
    }

    suspend fun addCallMessage(
        roomId: String,
        callerId: String,
        callId: String,
        callType: String,
        status: String
    ) {
        val msg = ChatMessage(
            senderId = callerId,
            timestamp = System.currentTimeMillis(),
            type = "call",
            callId = callId,
            callType = callType,
            callStatus = status
        )

        FirebaseFirestore.getInstance()
            .collection("chatrooms")
            .document(roomId)
            .collection("messages")
            .add(msg)
    }

}
