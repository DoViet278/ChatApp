package com.example.chatapplication

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chatapplication.ui.viewmodel.AuthViewModel
import com.example.chatapplication.ui.viewmodel.ChatViewModel
import com.example.chatapplication.ui.home.HomeScreen
import com.example.chatapplication.ui.home.ProfileScreen
import com.example.chatapplication.ui.login.LoginScreen
import com.example.chatapplication.ui.login.RegisterScreen
import com.example.chatapplication.ui.viewmodel.ProfileViewModel


sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile/{uid}")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val chatViewModel : ChatViewModel = hiltViewModel()
    val profileViewModel : ProfileViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController, chatViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController, profileViewModel,authViewModel)
        }
    }
}