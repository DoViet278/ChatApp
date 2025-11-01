package com.example.chatapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapplication.ui.home.HomeScreen
import com.example.chatapplication.ui.login.LoginScreen
import com.example.chatapplication.ui.login.RegisterScreen
import com.example.chatapplication.ui.theme.ChatApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            ChatApplicationTheme {
                val navController = rememberNavController()
                AppNavGraph(navController)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(it.uid)
            userRef.update("isOnline", true)
        }
    }

    override fun onStop() {
        super.onStop()
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(it.uid)
            userRef.update(
                mapOf(
                    "isOnline" to false,
                )
            )
        }
    }
}
