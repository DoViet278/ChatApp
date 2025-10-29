package com.example.chatapplication.data.repository

import android.util.Log
import com.example.chatapplication.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun registerUser(name: String, email: String, password: String): Result<User> {
        return try {
            val existing = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("Email này đã được đăng ký."))
            }

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Không thể tạo người dùng."))

            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                avtUrl = "https://picsum.photos/id/1/200/300"
            )

            firestore.collection("users").document(firebaseUser.uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            Log.d("login","$email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val doc = firestore.collection("users").document(result.user!!.uid).get().await()
            val user = doc.toObject(User::class.java) ?: User(result.user!!.uid, "", email)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getCurrentUser(): User? {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (firebaseUser != null) {
            User(
                uid  = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: ""
            )
        } else null
    }

}