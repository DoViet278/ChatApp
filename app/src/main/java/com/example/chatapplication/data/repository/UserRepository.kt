package com.example.chatapplication.data.repository

import com.example.chatapplication.SupabaseManager
import com.example.chatapplication.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.UploadData
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val usersRef = db.collection("users")

    suspend fun getUser(uid: String): User? {
        val doc = usersRef.document(uid).get().await()
        return doc.toObject(User::class.java)
    }
    suspend fun updateUser(user: User) {
        usersRef.document(user.uid).set(user).await()
    }

    suspend fun uploadAvatar(inputSteam: InputStream, fileExtension: String): String {
        val bucket = SupabaseManager.client.storage.from("imgAvt")
        val fileName = "${UUID.randomUUID()}.$fileExtension"
        bucket.upload(path = fileName, data = inputSteam.readBytes())
        return bucket.publicUrl(fileName)
    }
}