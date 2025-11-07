package com.example.chatapplication.ui.home

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.chatapplication.MainActivity
import com.example.chatapplication.data.model.ChatMessage
import com.example.chatapplication.ui.viewmodel.ChatViewModel
import com.example.chatapplication.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.chatapplication.data.model.User
import com.example.chatapplication.R
import com.example.chatapplication.Screen
import com.example.chatapplication.ZegoConfig
import com.example.chatapplication.ui.component.AttachmentItem
import com.example.chatapplication.ui.component.CallButton
import com.example.chatapplication.ui.component.ChatInputBar
import com.google.firebase.auth.FirebaseAuth
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatOneScreen(
    currentUserId: String,
    otherUserId: String,
    roomId: String,
    onBack: ()->Unit,
    viewModel: ChatViewModel,
    homeViewModel: HomeViewModel
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    var input by remember { mutableStateOf("") }
    var otherUser by remember { mutableStateOf<User?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSheet by remember { mutableStateOf(false) }
    //image
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    var previewVideoUrl by remember { mutableStateOf<String?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(roomId) {
        viewModel.listenMessages(roomId)
        viewModel.markAsRead(roomId, currentUserId)
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
                        CallButton(isVideoCall = false) { btn ->
                            if (otherUserId.isNotEmpty()) {
                                btn.setInvitees(
                                    mutableListOf(
                                        ZegoUIKitUser(
                                            otherUserId,
                                            otherUserName
                                        )
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        CallButton(isVideoCall = true) { btn ->
                            if (otherUserId.isNotEmpty()) {
                                btn.setInvitees(
                                    mutableListOf(
                                        ZegoUIKitUser(
                                            otherUserId,
                                            otherUserName
                                        )
                                    )
                                )
                            }
                        }


                        Spacer(Modifier.width(4.dp))

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
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
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
                    val isMe = msg.senderId == currentUserId
                    MessageBubblePrivate(
                        msg,
                        isMe,
                        otherUserAvatar,
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
fun MessageBubblePrivate(
    msg: ChatMessage,
    isMe: Boolean,
    otherAvatar: String?,
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
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isMe) 16.dp else 4.dp,
        bottomEnd = if (isMe) 4.dp else 16.dp
    )
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
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(6.dp))
        }

        Box(
            modifier = Modifier
                .wrapContentWidth()
                .background(
                    color = if (isMe) Color(0xFF0084FF) else Color(0xFFE4E6EB),
                    shape = bubbleShape
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .widthIn(max = 260.dp)
            ) {

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

                    "video" -> Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onVideoClick(msg.fileUrl ?: "") }
                    ) {
                        val painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(msg.fileUrl)
                                .videoFrameMillis(0L)
                                .build()
                        )

                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.height(180.dp),
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

                Spacer(Modifier.height(2.dp))

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

@Composable
fun VideoPlayer(url: String) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        factory = { context ->
            android.widget.VideoView(context).apply {
                setVideoURI(Uri.parse(url))
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    start()
                }
            }
        }
    )
}


