package com.example.chatapplication.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import coil.compose.rememberAsyncImagePainter
import com.example.chatapplication.data.model.ChatMessage
import com.example.chatapplication.ui.viewmodel.ChatViewModel
import com.example.chatapplication.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.chatapplication.data.model.User


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatOneScreen(
    currentUserId: String,
    otherUserId: String,
    roomId: String,
    viewModel: ChatViewModel,
    homeViewModel: HomeViewModel
) {
    val messages by viewModel.messages.collectAsState()
    var input by remember { mutableStateOf("") }
    var otherUser by remember { mutableStateOf<User?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(roomId) {
        viewModel.listenMessages(roomId)
    }

    LaunchedEffect(otherUserId) {
        otherUser = homeViewModel.getUserById(otherUserId)
    }

    val otherUserName = otherUser?.name ?: "Đang tải..."
    val otherUserAvatar = otherUser?.avtUrl

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
                                painter = rememberAsyncImagePainter(otherUserAvatar),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(otherUserName)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm"
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
                val reversed = filteredMessages.reversed()
                items(reversed) { msg ->
                    val isMe = msg.senderId == currentUserId
                    MessageBubblePrivate(msg, isMe, otherUserAvatar)
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
fun MessageBubblePrivate(msg: ChatMessage, isMe: Boolean, otherAvatar: String?) {
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
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            Image(
                painter = rememberAsyncImagePainter(otherAvatar),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(6.dp))
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
