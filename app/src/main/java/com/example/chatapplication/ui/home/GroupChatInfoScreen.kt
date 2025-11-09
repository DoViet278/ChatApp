package com.example.chatapplication.ui.home

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chatapplication.R
import com.example.chatapplication.data.model.ChatRoom
import com.example.chatapplication.data.model.User
import com.example.chatapplication.ui.viewmodel.AuthViewModel
import com.example.chatapplication.ui.viewmodel.GroupChatInfoViewModel
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatInfoScreen(
    roomId: String,
    navController: NavController,
    viewModel: GroupChatInfoViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val room by viewModel.chatRoom.collectAsState()
    val members by viewModel.members.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin = remember(room) {
        room?.adminIds?.contains(currentUser?.uid) == true
    }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditAvtDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var userToRemove by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(roomId) {
        viewModel.loadGroup(roomId)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ){uri: Uri? ->
        uri?.let {
            val inputSteam: InputStream? = context.contentResolver.openInputStream(it)
            inputSteam?.let { steam ->
                viewModel.uploadAvatar(room!!.chatroomId,steam,"jpg")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        Log.d("ảnh","$bitmap")
        bitmap?.let {
            Toast.makeText(context, "Thành công", Toast.LENGTH_SHORT)
                .show()
            val uri = saveBitmapToUri(context, bitmap)
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                inputStream?.let { stream ->
                    viewModel.uploadAvatar(room!!.chatroomId,stream, "jpg")
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Cần quyền Camera để chụp ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin nhóm") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = rememberAsyncImagePainter(room?.groupAvt),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable {
                            showEditAvtDialog = true
                        },
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = room?.groupName ?: "Không có tên",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.width(4.dp))

                    IconButton(onClick = { showEditNameDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh tên nhóm",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Thành viên",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyColumn {
                items(members) { user ->
                    MemberItem(
                        user = user,
                        room = room,
                        isAdmin = isAdmin,
                        viewModel = viewModel
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isAdmin) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { showAddMemberDialog = true },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Thêm thành viên")
                }
            }
        }
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onAdd = { email ->
                viewModel.addMember(roomId, email) { ok ->
                    showAddMemberDialog = false
                }
            }
        )
    }
    if (showEditNameDialog) {
        EditGroupNameDialog(
            currentName = room?.groupName ?: "",
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                showEditNameDialog = false
                if (newName.isNotBlank()) {
                    viewModel.updateName(roomId, newName)
                }
            }
        )
    }
    if (showEditAvtDialog) {
        AlertDialog(
            onDismissRequest = { showEditAvtDialog = false },
            title = {
                Text(
                    "Thay đổi ảnh đại diện",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(Modifier.fillMaxWidth()) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showEditAvtDialog = false
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cam),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Chụp ảnh",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showEditAvtDialog = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_gallery),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Chọn từ thư viện",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEditAvtDialog = false }) {
                    Text("Đóng")
                }
            }
        )

    }
    if (showRemoveDialog && userToRemove != null) {
        ConfirmRemoveMemberDialog(
            userName = userToRemove!!.name ?: "Thành viên",
            onDismiss = { showRemoveDialog = false },
            onConfirm = {
                showRemoveDialog = false
                viewModel.removeMember(roomId, userToRemove!!.uid)
            }
        )
    }
}

@Composable
fun AddMemberDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm thành viên") },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email người dùng") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onAdd(email) }) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun EditGroupNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi tên nhóm") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên nhóm mới") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name.trim()) }) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ConfirmRemoveMemberDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Xóa thành viên")
        },
        text = {
            Text("Bạn có chắc muốn xóa \"$userName\" khỏi nhóm?")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Xóa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun MemberItem(
    user: User,
    room: ChatRoom?,
    isAdmin: Boolean,
    viewModel: GroupChatInfoViewModel
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(user.avtUrl),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row {
                Text(user.name ?: "")
                if (room?.adminIds?.contains(user.uid) == true) {
                    Spacer(Modifier.width(6.dp))
                    Text("(Admin)", color = Color(0xFF0084FF))
                }
            }
            Text(user.email ?: "", color = Color.Gray)
        }

        if (isAdmin) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null)
                }

                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {

                    if (room?.adminIds?.contains(user.uid) == false) {
                        DropdownMenuItem(
                            text = { Text("Cấp quyền Admin") },
                            onClick = {
                                showMenu = false
                                viewModel.addAdmin(room!!.chatroomId, user.uid)
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Gỡ quyền Admin") },
                            onClick = {
                                showMenu = false
                                viewModel.removeAdmin(room!!.chatroomId, user.uid)
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Xóa khỏi nhóm") },
                        onClick = {
                            showMenu = false
                            viewModel.removeMember(room!!.chatroomId, user.uid)
                        }
                    )
                }
            }
        }
    }
}