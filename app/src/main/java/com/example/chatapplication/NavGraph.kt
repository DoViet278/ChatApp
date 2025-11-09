package com.example.chatapplication

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chatapplication.ui.home.ChatBotScreen
import com.example.chatapplication.ui.home.ChatOneScreen
import com.example.chatapplication.ui.home.GroupChatInfoScreen
import com.example.chatapplication.ui.home.GroupChatScreen
import com.example.chatapplication.ui.viewmodel.AuthViewModel
import com.example.chatapplication.ui.viewmodel.ChatViewModel
import com.example.chatapplication.ui.home.HomeScreen
import com.example.chatapplication.ui.home.ProfileScreen
import com.example.chatapplication.ui.login.LoginScreen
import com.example.chatapplication.ui.login.RegisterScreen
import com.example.chatapplication.ui.login.SplashScreen
import com.example.chatapplication.ui.viewmodel.HomeViewModel
import com.example.chatapplication.ui.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth


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
    object GroupInfo : Screen("group_info/{roomId}") {
        fun createRoute(roomId: String) = "group_info/$roomId"
    }

    object Call : Screen("call/{callId}") {
        fun createRoute(callId: String) = "call/$callId"
    }

    object VideoCall : Screen("video_call/{callId}") {
        fun createRoute(callId: String) = "video_call/$callId"
    }

    object ChatBot : Screen("chat_bot")

}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val chatViewModel : ChatViewModel = hiltViewModel()
    val profileViewModel : ProfileViewModel = hiltViewModel()
    val homeViewModel : HomeViewModel = hiltViewModel()
    val splashFinished = remember { mutableStateOf(false) }


    if (splashFinished.value) {
        val currentUser by authViewModel.currentUser.collectAsState()

        LaunchedEffect(currentUser) {
            if (currentUser == null) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }


    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController, authViewModel, onFinish = { splashFinished.value = true })
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

        composable(Screen.ChatBot.route) {
            ChatBotScreen(onBack = { navController.popBackStack() })
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
                ChatOneScreen(
                    currentUserId = currentUser!!.uid,
                    otherUserId = otherUserId,
                    roomId = roomId,
                    onBack = { navController.popBackStack() },
                    viewModel = chatViewModel,
                    homeViewModel = homeViewModel
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
                GroupChatScreen(
                    navController = navController,
                    currentUserId = currentUser!!.uid,
                    roomId = roomId,
                    onBack = { navController.popBackStack() },
                    viewModel = chatViewModel
                )
            }
        }

        composable(
            route = Screen.GroupInfo.route,
            arguments = listOf(
                navArgument("roomId") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val currentUser by authViewModel.currentUser.collectAsState()

            if (currentUser != null && roomId.isNotEmpty()) {
                GroupChatInfoScreen(
                    roomId = roomId,
                    navController = navController
                )
            }
        }

    }
}