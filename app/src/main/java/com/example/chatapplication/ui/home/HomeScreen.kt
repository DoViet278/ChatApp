package com.example.chatapplication.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatapplication.Screen
import com.example.chatapplication.ui.viewmodel.ChatViewModel
import im.zego.zim.entity.ZIMAudioMessage
import im.zego.zim.entity.ZIMConversation
import im.zego.zim.entity.ZIMFileMessage
import im.zego.zim.entity.ZIMImageMessage
import im.zego.zim.entity.ZIMTextMessage
import im.zego.zim.entity.ZIMVideoMessage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: ChatViewModel = hiltViewModel()) {
    val conversations by viewModel.conversations.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("createChat") }) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            items(conversations) { convo ->
                ConversationItem(convo) {
                    navController.navigate("chatDetail/${convo.conversationID}")
                }
            }
        }
    }
}

@Composable
fun ConversationItem(convo: ZIMConversation, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(convo.conversationAvatarUrl),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = convo.conversationName ?: "Unknown",
                style = MaterialTheme.typography.titleMedium
            )

            val lastMsg = when (val msg = convo.lastMessage) {
                is ZIMTextMessage -> msg.message
                is ZIMImageMessage -> "[Image]"
                is ZIMAudioMessage -> "[Voice Message]"
                is ZIMVideoMessage -> "[Video]"
                is ZIMFileMessage -> "[File]"
                else -> ""
            }

            Text(
                text = lastMsg,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(convo.orderKey.toString())
            if (convo.unreadMessageCount > 0) {
                Badge { Text(convo.unreadMessageCount.toString()) }
            }
        }
    }
}

