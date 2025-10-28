package com.example.chatapplication

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chatapplication.ui.home.ChatScreen
import com.example.chatapplication.ui.viewmodel.AuthViewModel
import com.example.chatapplication.ui.viewmodel.ChatViewModel
import com.example.chatapplication.ui.home.HomeScreen
import com.example.chatapplication.ui.home.ProfileScreen
import com.example.chatapplication.ui.login.LoginScreen
import com.example.chatapplication.ui.login.RegisterScreen
import com.example.chatapplication.ui.login.SplashScreen
import com.example.chatapplication.ui.viewmodel.HomeViewModel
import com.example.chatapplication.ui.viewmodel.ProfileViewModel


sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object ChatOneToOne : Screen("chatOne/{roomId}/{otherUserId}") {
        fun createRoute(roomId: String, otherUserId: String): String {
            return "chatOne/$roomId/$otherUserId"
        }
    }
    object ChatGroup : Screen("chatGroup/{roomId}") {
        fun createRoute(roomId: String): String {
            return "chatGroup/$roomId"
        }
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val chatViewModel : ChatViewModel = hiltViewModel()
    val profileViewModel : ProfileViewModel = hiltViewModel()
    val homeViewModel : HomeViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController, authViewModel)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }
        composable(Screen.Home.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            if (currentUser != null) {
                HomeScreen(
                    navController = navController,
                    currentUserId = currentUser!!.uid,
                    viewModel = homeViewModel
                )
            }
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController, profileViewModel,authViewModel)
        }

        composable(
            route = Screen.ChatOneToOne.route,
            arguments = listOf(
                navArgument("roomId") { defaultValue = "" },
                navArgument("otherUserId") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
            val currentUser by authViewModel.currentUser.collectAsState()

            if (currentUser != null && roomId.isNotEmpty()) {
                ChatScreen(
                    currentUserId = currentUser!!.uid,
                    otherUserId = otherUserId,
                    roomId = roomId,
                    viewModel = chatViewModel
                )
            }
        }

        composable(
            route = Screen.ChatGroup.route,
            arguments = listOf(
                navArgument("roomId") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val currentUser by authViewModel.currentUser.collectAsState()

            if (currentUser != null && roomId.isNotEmpty()) {
                ChatScreen(
                    currentUserId = currentUser!!.uid,
                    otherUserId = "",
                    roomId = roomId,
                    viewModel = chatViewModel
                )
            }
        }


    }
}