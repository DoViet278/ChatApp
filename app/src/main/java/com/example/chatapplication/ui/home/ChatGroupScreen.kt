package com.example.chatapplication.ui.home

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.chatapplication.CallStateHolder
import com.example.chatapplication.MainActivity
import com.example.chatapplication.R
import com.example.chatapplication.Screen
import com.example.chatapplication.data.model.ChatMessage
import com.example.chatapplication.data.model.User
import com.example.chatapplication.ui.component.AttachmentItem
import com.example.chatapplication.ui.component.CallButton
import com.example.chatapplication.ui.viewmodel.ChatViewModel
import io.ktor.client.plugins.Sender
import java.text.SimpleDateFormat
import java.util.*
import com.example.chatapplication.ui.component.ChatInputBar
import com.zegocloud.uikit.service.defines.ZegoUIKitUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    navController: NavController,
    currentUserId: String,
    roomId: String,
    onBack: ()->Unit,
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val usersInRoom by viewModel.usersInRoom.collectAsState()
    var input by remember { mutableStateOf("") }
    val chatRoom by viewModel.chatRoom.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSheet by remember { mutableStateOf(false) }
    //image
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    var previewVideoUrl by remember { mutableStateOf<String?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(roomId) {
        viewModel.listenMessages(roomId)
        viewModel.listenChatRoomInfo(roomId)
        viewModel.markAsRead(roomId, currentUserId)
    }

    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) messages
        else messages.filter {
            it.message.contains(searchQuery, ignoreCase = true)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val type = context.contentResolver.getType(uri) ?: ""
            val input = context.contentResolver.openInputStream(uri) ?: return@let

            when {
                type.startsWith("image") -> viewModel.sendImage(roomId, currentUserId, input, "jpg")
                type.startsWith("video") -> viewModel.sendVideo(roomId, currentUserId, input, "mp4")
            }
        }
    }


    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            val input = context.contentResolver.openInputStream(tempImageUri!!)
            if (input != null) {
                viewModel.sendImage(
                    roomId = roomId,
                    userId = currentUserId,
                    input = input,
                    ext = "jpg"
                )
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { onBack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
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
                            Text(
                                text = chatRoom?.groupName ?: "Group Chat",
                                maxLines = 1,
                                modifier = Modifier
                                    .clickable(onClick = {
                                            navController.navigate(Screen.GroupInfo.createRoute(roomId))
                                    }
                            ))
                        }
                    },
                    actions = {
                        CallButton(isVideoCall = false, onClick = {viewModel.sendCallMessage(roomId, currentUserId)}) { btn ->
                            val invitees = usersInRoom
                                .filter { it.uid != currentUserId }
                                .map { user -> ZegoUIKitUser(user.uid, user.name ?: user.uid) }
                                .toMutableList()

                            val targetIds = usersInRoom
                                .filter { it.uid != currentUserId }
                                .map { it.uid }

                            viewModel.createCallLog(
                                roomId = roomId,
                                myId = currentUserId,
                                targetIds = targetIds,
                                type = "voice"
                            ) { callId ->

                                CallStateHolder.currentRoomId = roomId
                                CallStateHolder.currentCallId = callId

                                if (invitees.isNotEmpty()) btn.setInvitees(invitees)
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        CallButton(isVideoCall = true, onClick = {viewModel.sendCallMessage(roomId, currentUserId)}) { btn ->
                            val invitees = usersInRoom
                                .filter { it.uid != currentUserId }
                                .map { user -> ZegoUIKitUser(user.uid, user.name ?: user.uid) }
                                .toMutableList()

                            val targetIds = usersInRoom
                                .filter { it.uid != currentUserId }
                                .map { it.uid }

                            viewModel.createCallLog(
                                roomId = roomId,
                                myId = currentUserId,
                                targetIds = targetIds,
                                type = "video"
                            ) { callId ->

                                CallStateHolder.currentRoomId = roomId
                                CallStateHolder.currentCallId = callId

                                if (invitees.isNotEmpty()) btn.setInvitees(invitees)
                            }


                        }
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
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F2F5))
                .navigationBarsPadding()
                .imePadding()
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
                    val sender = usersInRoom.find { it.uid == msg.senderId }
                    val isMe = msg.senderId == currentUserId
                    MessageBubbleGroup(
                        msg,
                        sender,
                        isMe,
                        onImageClick = {url ->
                            previewImageUrl = url
                        },
                        onVideoClick = {url ->
                            previewVideoUrl = url
                        }
                    )
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
                },
                onAttachmentClick = { showSheet = true }
            )
        }
    }
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttachmentItem(
                        icon = R.drawable.ic_cam,
                        label = "Máy ảnh",
                        onClick = {
                            showSheet = false
                            val uri = createImageUri(context)
                            tempImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                    )
                    AttachmentItem(
                        icon = R.drawable.ic_gallery,
                        label = "Thư viện",
                        onClick = {
                            showSheet = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        }
                    )
                }
            }
        }
    }
    if (previewImageUrl != null) {
        Dialog(
            onDismissRequest = { previewImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { previewImageUrl = null },
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = rememberAsyncImagePainter(previewImageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
    if (previewVideoUrl != null) {
        Dialog(
            onDismissRequest = { previewVideoUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { previewVideoUrl = null },
                contentAlignment = Alignment.Center
            ) {
                VideoPlayer(url = previewVideoUrl!!)
            }
        }
    }

}

@Composable
fun MessageBubbleGroup(
    msg: ChatMessage,
    sender: User?,
    isMe: Boolean,
    onImageClick: (String) -> Unit,
    onVideoClick: (String) -> Unit
) {
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
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
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
                when (msg.type) {
                    "text" -> Text(
                        text = msg.message,
                        color = if (isMe) Color.White else Color.Black
                    )

                    "image" -> Image(
                        painter = rememberAsyncImagePainter(msg.fileUrl),
                        contentDescription = "image_message",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 250.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = {onImageClick(msg.fileUrl ?: "")}),
                        contentScale = ContentScale.Crop
                    )

                    "video" -> Column {
                        Box(
                            modifier = Modifier
                                .height(180.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onVideoClick(msg.fileUrl ?: "")
                                }
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(msg.fileUrl)
                                        .build()
                                ),
                                contentDescription = "video_thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                    "call" -> {
                        val icon = if (msg.callType == "video")
                            painterResource(R.drawable.ic_video_call)
                        else
                            painterResource(R.drawable.ic_phone_call)

                        val statusText = when (msg.callStatus) {
                            "missed" -> "Cuộc gọi nhỡ"
                            "ended"  -> if (msg.callType == "video") "Cuộc gọi video" else "Cuộc gọi thoại"
                            else     -> "Cuộc gọi"
                        }

                        val label = if (isMe) {
                            statusText
                        } else {
                            "${sender?.name} • $statusText"
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(6.dp)
                                .wrapContentWidth()
                        ) {
                            Icon(
                                painter = icon,
                                contentDescription = null,
                                tint = if (isMe) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))

                            Text(
                                label,
                                color = if (isMe) Color.White else Color.Black,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = msgTime,
                    color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = if (isMe) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.wrapContentWidth()
                )
            }
        }
    }
}

private fun createImageUri(context: Context): Uri {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )!!
}