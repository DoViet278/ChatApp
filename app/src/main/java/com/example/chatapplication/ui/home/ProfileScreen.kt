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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
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

    Log.d("ProfileScreen", "user: $user")

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
        Log.d("ảnh","$bitmap")
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
        var name by remember { mutableStateOf(user.name) }
        var email by remember { mutableStateOf(user.email) }
        var sdt by remember { mutableStateOf(user.sdt) }
        var birthday by remember { mutableStateOf(user.birthday) }

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

            FieldWithEditButton(
                label = "Họ tên",
                value = name,
                editable = editName,
                onEditClick = { editName = !editName },
                onValueChange = { name = it }
            )

            Spacer(Modifier.height(8.dp))

            FieldWithEditButton(
                label = "Email",
                value = email,
                editable = editEmail,
                onEditClick = { editEmail = !editEmail },
                onValueChange = { email = it }
            )

            Spacer(Modifier.height(8.dp))

            FieldWithEditButton(
                label = "Số điện thoại",
                value = sdt,
                editable = editSdt,
                onEditClick = { editSdt = !editSdt },
                onValueChange = { sdt = it }
            )

            Spacer(Modifier.height(8.dp))

            FieldWithEditButton(
                label = "Ngày sinh",
                value = birthday,
                editable = editBirthday,
                onEditClick = { editBirthday = !editBirthday },
                onValueChange = { birthday = it }
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
                    navController.navigate(Screen.Login.route)
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
                            TextButton(onClick = {
                                showDialog = false
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }) { Text("📷 Chụp ảnh") }
                            TextButton(onClick = {
                                showDialog = false
                                galleryLauncher.launch("image/*")
                            }) { Text("🖼️ Chọn từ thư viện") }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Đóng") }
                    }
                )
            }
        }
    }
}

@Composable
fun FieldWithEditButton(
    label: String,
    value: String,
    editable: Boolean,
    onEditClick: () -> Unit,
    onValueChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = value,
            onValueChange = { if (editable) onValueChange(it) },
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            enabled = editable,
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = onEditClick) {
            Text(if (editable) "Lưu" else "Sửa")
        }
    }
}

fun saveBitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    val filename = "avatar_${System.currentTimeMillis()}.jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ChatApp") // thư mục tạm
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
