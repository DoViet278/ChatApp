package com.example.chatapplication.ui.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatapplication.Screen
import com.example.chatapplication.data.model.ChatRoom
import com.example.chatapplication.data.model.User
import com.example.chatapplication.ui.viewmodel.ChatViewModel
import com.example.chatapplication.ui.viewmodel.HomeViewModel
import im.zego.zim.entity.ZIMAudioMessage
import im.zego.zim.entity.ZIMConversation
import im.zego.zim.entity.ZIMFileMessage
import im.zego.zim.entity.ZIMImageMessage
import im.zego.zim.entity.ZIMTextMessage
import im.zego.zim.entity.ZIMVideoMessage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    currentUserId: String,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val chatRooms by viewModel.chatRooms.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var isGroup by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        viewModel.listenChatRooms(currentUserId)
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
            Box {
                FloatingActionButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New Chat")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tạo chat 1-1") },
                        onClick = {
                            menuExpanded = false
                            isGroup = false
                            showDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tạo nhóm chat") },
                        onClick = {
                            menuExpanded = false
                            isGroup = true
                            showDialog = true
                        }
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            items(chatRooms) { room ->
                ChatRoomItem(
                    room = room,
                    currentUserId = currentUserId,
                    getUserById = { id -> viewModel.getUserById(id) }
                ) {
                    val id = room.chatroomId
                    Log.d("room","$isGroup")
                    Log.d("roomname","${room.groupName}")
                    if(id.isNotBlank()){
                        if(room.group){
                            navController.navigate(Screen.ChatGroup.createRoute(id))
                        }else{
                            val otherId = room.userIds.firstOrNull{it != currentUserId}
                            if(otherId != null){
                                navController.navigate(Screen.ChatOneToOne.createRoute(id,otherId))
                            }else{
                                Toast.makeText(navController.context,"Lỗi màn",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else{
                        Toast.makeText(navController.context,"Lỗi id room",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        if (showDialog) {
            CreateChatDialog(
                isGroup = isGroup,
                onDismiss = { showDialog = false },
                onCreate = { emails ->
                    showDialog = false
                    if (isGroup) {
                        viewModel.createGroupChat(
                            currentUserId = currentUserId,
                            memberEmails = emails,
                            groupName = "Nhóm mới"
                        ) { chatId ->
                            if (chatId.isNotEmpty()) {
                                navController.navigate(Screen.ChatGroup.createRoute(chatId))
                            }
                        }
                    } else {
                        viewModel.createOneToOneChat(
                            currentUserId = currentUserId,
                            email = emails.firstOrNull() ?: ""
                        ) { chatId, otherUserId ->
                            if (chatId.isNotEmpty()) {
                                navController.navigate(Screen.ChatOneToOne.createRoute(chatId, otherUserId))
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ChatRoomItem(
    room: ChatRoom,
    currentUserId: String,
    getUserById: suspend (String) -> User?,
    onClick: () -> Unit
) {
    var otherUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(room.chatroomId) {
        if (!room.group) {
            val otherId = room.userIds.firstOrNull { it != currentUserId }
            if (otherId != null) {
                otherUser = getUserById(otherId)
            }
        }
    }

    val displayName = when {
        room.group -> room.groupName ?: "Nhóm không tên"
        else -> otherUser?.name ?: "Đang tải..."
    }

    val displayAvatar = when {
        room.group -> room.groupAvt ?: "https://picsum.photos/id/1/200/300"
        else -> otherUser?.avtUrl
    }

    val lastTime = android.text.format.DateUtils.getRelativeTimeSpanString(room.lastMessageTimestamp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (displayAvatar != null) {
                Image(
                    painter = rememberAsyncImagePainter(displayAvatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    room.lastMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }

            Text(
                lastTime.toString(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}





