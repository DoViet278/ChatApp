package com.example.chatapplication.ui.home

import android.util.Log
import android.widget.Toast
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
import com.example.chatapplication.data.model.ChatRoom
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
                ChatRoomItem(room) {
                    val id = room.chatroomId
                    Log.d("room","$id")
                    if(id.isNotBlank()){
                        if(room.isGroup){
                            navController.navigate(Screen.ChatGroup.createRoute(id))
                        }else{
                            val otherId = room.userIds.firstOrNull{it != currentUserId}
                            if(otherId != null){
                                navController.navigate(Screen.ChatOneToOne.createRoute(id,otherId))
                            }else{
                                Toast.makeText(navController.context,"Lỗi chuyen màn",Toast.LENGTH_SHORT).show()
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
                                Log.d("chatid", "onCreate: $chatId")
                                navController.navigate(Screen.ChatGroup.createRoute(chatId))
                            }
                        }
                    } else {
                        viewModel.createOneToOneChat(
                            currentUserId = currentUserId,
                            email = emails.firstOrNull() ?: ""
                        ) { chatId, otherUserId ->
                            if (chatId.isNotEmpty()) {
                                Log.d("chatid", "onCreate: $chatId")
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
fun ChatRoomItem(room: ChatRoom, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(8.dp)) {
            Text("Room ID: ${room.chatroomId}")
            Text("Last Sender: ${room.lastMessageSenderId}")
            Text("Last Message Time: ${room.lastMessageTimestamp}")
        }
    }
}

@Composable
fun CreateChatDialog(
    isGroup: Boolean,
    onDismiss: () -> Unit,
    onCreate: (List<String>) -> Unit
) {
    var email1 by remember { mutableStateOf("") }
    var email2 by remember { mutableStateOf("") }
    var email3 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (isGroup) {
                    val emails = listOf(email1, email2, email3).filter { it.isNotBlank() }
                    if (emails.size >= 2) onCreate(emails)
                } else {
                    if (email1.isNotBlank()) onCreate(listOf(email1))
                }
            }) {
                Text("Tạo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        },
        title = {
            Text(if (isGroup) "Tạo nhóm chat" else "Tạo chat 1-1")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = email1,
                    onValueChange = { email1 = it },
                    label = { Text("Email 1") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (isGroup) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email2,
                        onValueChange = { email2 = it },
                        label = { Text("Email 2") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email3,
                        onValueChange = { email3 = it },
                        label = { Text("Email 3") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}


