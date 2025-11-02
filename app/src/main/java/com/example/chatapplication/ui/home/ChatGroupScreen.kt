package com.example.chatapplication.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatapplication.Screen
import com.example.chatapplication.data.model.ChatMessage
import com.example.chatapplication.data.model.User
import com.example.chatapplication.ui.viewmodel.ChatViewModel
import io.ktor.client.plugins.Sender
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    navController: NavController,
    currentUserId: String,
    roomId: String,
    viewModel: ChatViewModel
) {
    val messages by viewModel.messages.collectAsState()
    val usersInRoom by viewModel.usersInRoom.collectAsState()
    var input by remember { mutableStateOf("") }
    val chatRoom by viewModel.chatRoom.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(roomId) {
        viewModel.listenMessages(roomId)
        viewModel.listenChatRoomInfo(roomId)
    }

    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) messages
        else messages.filter {
            it.message.contains(searchQuery, ignoreCase = true)
        }
    }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = rememberAsyncImagePainter(chatRoom?.groupAvt),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = chatRoom?.groupName ?: "Group Chat", modifier = Modifier.clickable(onClick = {
                                navController.navigate(Screen.GroupInfo.createRoute(roomId))
                            }))
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm tin nhắn"
                            )
                        }
                    }
                )

                if (showSearch) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm tin nhắn...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        singleLine = true
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F2F5))
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val reversed =filteredMessages.reversed()
                items(reversed) { msg ->
                    val sender = usersInRoom.find { it.uid == msg.senderId }
                    val isMe = msg.senderId == currentUserId
                    MessageBubbleGroup(msg, sender, isMe)
                }
            }

            ChatInputBar(
                input = input,
                onValueChange = { input = it },
                onSend = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(roomId, currentUserId, input.trim())
                        input = ""
                    }
                }
            )
        }
    }
}

@Composable
fun MessageBubbleGroup(msg: ChatMessage,sender: User?, isMe: Boolean) {
    val msgTime = remember(msg.timestamp) {
        val msgDate = Date(msg.timestamp)
        val now = Calendar.getInstance()
        val msgCal = Calendar.getInstance().apply { time = msgDate }

        val sameDay = now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)

        if (sameDay) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(msgDate)
        } else {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(msgDate)
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMe) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = rememberAsyncImagePainter(sender?.avtUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.height(2.dp))
            }
        }

        Surface(
            color = if (isMe) Color(0xFF0084FF) else Color(0xFFE4E6EB),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 0.dp,
                bottomEnd = if (isMe) 0.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .widthIn(max = 260.dp)
            ) {
                if (!isMe) {
                    Text(
                        text = sender?.name ?: "Unknown",
                        color = Color(0xFF0084FF),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    text = msg.message,
                    color = if (isMe) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = msgTime,
                    color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = if (isMe) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
