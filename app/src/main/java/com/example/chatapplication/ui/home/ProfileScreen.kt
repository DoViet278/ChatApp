package com.example.chatapplication.ui.home

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import com.example.chatapplication.Screen
import com.example.chatapplication.ui.viewmodel.AuthViewModel
import com.example.chatapplication.ui.viewmodel.ProfileViewModel
import java.io.InputStream

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    userViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val user by userViewModel.currentUser.collectAsState()
    val profile by viewModel.user.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        user?.let {
            viewModel.loadUser(it.uid)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ){uri: Uri? ->
        uri?.let {
            val inputSteam: InputStream? = context.contentResolver.openInputStream(it)
            inputSteam?.let { steam ->
                viewModel.uploadAvatar(steam,"jpg")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            Toast.makeText(context, "Thành công", Toast.LENGTH_SHORT)
                .show()
            val uri = saveBitmapToUri(context, bitmap)
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                inputStream?.let { stream ->
                    viewModel.uploadAvatar(stream, "jpg")
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

    profile?.let { user ->
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var sdt by remember { mutableStateOf("") }
        var birthday by remember { mutableStateOf("") }

        LaunchedEffect(profile?.uid) {
            profile?.let { user ->
                name = user.name
                email = user.email
                sdt = user.sdt
                birthday = user.birthday
            }
        }

        var editName by remember { mutableStateOf(false) }
        var editEmail by remember { mutableStateOf(false) }
        var editSdt by remember { mutableStateOf(false) }
        var editBirthday by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = rememberAsyncImagePainter(
                        user.avtUrl.ifEmpty { "https://picsum.photos/id/1/200/300" }
                    ),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { showDialog = true },
                    contentScale = ContentScale.Crop
                )

                if (isUploading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            FieldRow(
                label = "Họ tên",
                value = name,
                editable = editName,
                onEditClick = { editName = true },
                onValueChange = { name = it },
                onSaveClick = { editName = false }
            )

            FieldRow(
                label = "Email",
                value = email,
                editable = editEmail,
                onEditClick = { editEmail = true },
                onValueChange = { email = it },
                onSaveClick = { editEmail = false }
            )

            FieldRow(
                label = "Số điện thoại",
                value = sdt,
                editable = editSdt,
                onEditClick = { editSdt = true },
                onValueChange = { sdt = it },
                onSaveClick = { editSdt = false }
            )

            FieldRow(
                label = "Ngày sinh",
                value = birthday,
                editable = editBirthday,
                onEditClick = { editBirthday = true },
                onValueChange = { birthday = it },
                onSaveClick = { editBirthday = false }
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val updatedUser = user.copy(
                        name = name,
                        email = email,
                        sdt = sdt,
                        birthday = birthday
                    )
                    viewModel.updateUser(updatedUser)
                    Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Cập nhật")
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    userViewModel.logout()
                }
            ) {
                Text("Đăng xuất")
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Thay đổi ảnh đại diện") },
                    text = {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDialog = false
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_cam),
                                    contentDescription = "Camera",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Chụp ảnh", style = MaterialTheme.typography.bodyLarge)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDialog = false
                                        galleryLauncher.launch("image/*")
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_gallery),
                                    contentDescription = "Gallery",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Chọn từ thư viện", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Đóng")
                        }
                    }
                )

            }
        }
    }
}

@Composable
fun FieldRow(
    label: String,
    value: String,
    editable: Boolean,
    onEditClick: () -> Unit,
    onValueChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(4.dp))

        if (!editable) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    value.ifBlank { "Chưa cập nhật" },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {

                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Spacer(Modifier.width(8.dp))

                TextButton(onClick = onSaveClick) {
                    Text("Lưu")
                }
            }
        }
    }
}


fun saveBitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    val filename = "avatar_${System.currentTimeMillis()}.jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ChatApp")
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
    }

    return uri
}
