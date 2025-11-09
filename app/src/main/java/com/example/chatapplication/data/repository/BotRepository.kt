package com.example.chatapplication.data.repository

import com.example.chatapplication.data.model.MessageModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BotRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun userMessagesRef(userId: String) =
        firestore.collection("chatbot_history")
            .document(userId)
            .collection("messages")

    suspend fun saveMessage(message: MessageModel) {
        val data = hashMapOf(
            "message" to message.message,
            "role" to message.role,
            "timestamp" to message.timestamp,
            "userId" to message.userId
        )

        userMessagesRef(message.userId).add(data).await()
    }

    suspend fun getMessages(userId: String): List<MessageModel> {
        val snapshot = userMessagesRef(userId)
            .orderBy("timestamp")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            MessageModel(
                message = doc.getString("message") ?: "",
                role = doc.getString("role") ?: "",
                timestamp = doc.getLong("timestamp") ?: 0L,
                userId = doc.getString("userId") ?: ""
            )
        }
    }
}
