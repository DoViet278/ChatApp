package com.example.chatapplication

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chatapplication.ui.auth.AuthViewModel
import com.example.chatapplication.ui.home.HomeScreen
import com.example.chatapplication.ui.home.ProfileScreen
import com.example.chatapplication.ui.login.LoginScreen
import com.example.chatapplication.ui.login.RegisterScreen


sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController, authViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController, authViewModel)
        }
    }
}