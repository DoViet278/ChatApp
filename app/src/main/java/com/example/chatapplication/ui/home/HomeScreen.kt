package com.example.chatapplication.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import com.example.chatapplication.ui.viewmodel.HomeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    currentUserId: String,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val chatRooms by viewModel.chatRooms.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }
    val onlineUsers by viewModel.onlineUsers.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var isGroup by remember { mutableStateOf(false) }
    val query by viewModel.searchQuery.collectAsState()

    val filteredRooms = remember(chatRooms, query) {
        chatRooms.filter { room ->
            val displayName =
                if (room.group) room.groupName ?: ""
                else {
                    val otherId = room.userIds.firstOrNull { it != currentUserId }
                    otherId?.let { id -> viewModel.getCachedUserName(id) } ?: ""
                }

            displayName.contains(query, ignoreCase = true)
        }
    }

    LaunchedEffect(currentUserId) {
        viewModel.listenChatRooms(currentUserId)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Chats") },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                    }
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Tìm kiếm...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = CircleShape,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0084FF),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                    )
                )

                if (onlineUsers.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 4.dp, bottom = 8.dp)
                    ) {
                        items(onlineUsers) { user ->
                            Column(
                                modifier = Modifier.padding(end = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(user.avtUrl ?: ""),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(Color.LightGray.copy(alpha = 0.3f)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00C853))
                                            .align(Alignment.BottomEnd)
                                            .offset(x = (-2).dp, y = (-2).dp)
                                    )
                                }
                                Text(
                                    text = user.name ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
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
            items(filteredRooms) { room ->
                ChatRoomBottomSheetItem(
                    room = room,
                    currentUserId = currentUserId,
                    getUserById = { id -> viewModel.getUserById(id) },
                    cacheUser = { viewModel.cacheUser(it) },
                    onClick = {
                        val id = room.chatroomId
                        if (room.group)
                            navController.navigate(Screen.ChatGroup.createRoute(id))
                        else {
                            val otherId = room.userIds.firstOrNull { it != currentUserId }
                            if (otherId != null)
                                navController.navigate(Screen.ChatOneToOne.createRoute(id, otherId))
                        }
                    },
                    onDelete = {
                        viewModel.deleteChatRoom(room.chatroomId)
                    }
                )
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomBottomSheetItem(
    room: ChatRoom,
    currentUserId: String,
    getUserById: suspend (String) -> User?,
    cacheUser: (User) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showSheet = true }
            )
    ) {
        ChatRoomItem(
            room = room,
            currentUserId = currentUserId,
            getUserById = getUserById,
            cacheUser = cacheUser,
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = Color.White,
            dragHandle = {
                Spacer(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.LightGray, CircleShape)
                )
            }
        ) {

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text(
                    text = "Xóa cuộc trò chuyện",
                    color = Color.Red,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showSheet = false
                            onDelete()
                        }
                        .padding(vertical = 16.dp)
                )

                Divider()

                Text(
                    text = "Hủy",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSheet = false }
                        .padding(vertical = 16.dp)
                )
            }
        }
    }
}



@Composable
fun ChatRoomItem(
    room: ChatRoom,
    currentUserId: String,
    getUserById: suspend (String) -> User?,
    cacheUser: (User) -> Unit,
) {
    var otherUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(room.chatroomId) {
        if (!room.group) {
            val otherId = room.userIds.firstOrNull { it != currentUserId }
            if (otherId != null) {
                otherUser = getUserById(otherId)
                cacheUser(otherUser!!)
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





