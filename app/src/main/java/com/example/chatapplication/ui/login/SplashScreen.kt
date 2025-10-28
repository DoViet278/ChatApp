package com.example.chatapplication.ui.login

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatapplication.R
import com.example.chatapplication.Screen
import com.example.chatapplication.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, viewModel: AuthViewModel) {
    val user by viewModel.currentUser.collectAsState()

    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.6f,
        animationSpec = tween(durationMillis = 1200, easing = {
            OvershootInterpolator(2f).getInterpolation(it)
        })
    )
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200)
    )

    LaunchedEffect(true) {
        startAnimation = true
        delay(2000)
        if (user != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Chat App",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Kết nối dễ dàng, trò chuyện mọi lúc",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
    }
}

