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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
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

    LaunchedEffect(Unit) { user?.let { viewModel.loadUser(it.uid) } }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { stream ->
                viewModel.uploadAvatar(stream, "jpg")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            Toast.makeText(context, "Chụp thành công", Toast.LENGTH_SHORT).show()
            saveBitmapToUri(context, it)?.let { uri ->
                context.contentResolver.openInputStream(uri)?.let { stream ->
                    viewModel.uploadAvatar(stream, "jpg")
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(null)
        else Toast.makeText(context, "Cần quyền camera", Toast.LENGTH_SHORT).show()
    }

    profile?.let { userProfile ->
        var name by remember { mutableStateOf(userProfile.name) }
        var email by remember { mutableStateOf(userProfile.email) }
        var sdt by remember { mutableStateOf(userProfile.sdt) }
        var birthday by remember { mutableStateOf(userProfile.birthday) }

        var editName by remember { mutableStateOf(false) }
        var editEmail by remember { mutableStateOf(false) }
        var editSdt by remember { mutableStateOf(false) }
        var editBirthday by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.White
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White, CircleShape)
                        .clickable { showDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(userProfile.avtUrl.ifEmpty { "https://picsum.photos/id/1/200/300" }),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                    )

                    if (isUploading) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(50.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    FieldCard("Họ tên", name, editName, onEdit = { editName = true }, onValueChange = { name = it }, onSave = { editName = false })
                    FieldCard("Email", email, editEmail, onEdit = { editEmail = true }, onValueChange = { email = it }, onSave = { editEmail = false })
                    FieldCard("Số điện thoại", sdt, editSdt, onEdit = { editSdt = true }, onValueChange = { sdt = it }, onSave = { editSdt = false })
                    FieldCard("Ngày sinh", birthday, editBirthday, onEdit = { editBirthday = true }, onValueChange = { birthday = it }, onSave = { editBirthday = false })
                }

                Spacer(Modifier.height(24.dp))

                // Buttons
                Button(
                    onClick = {
                        val updatedUser = userProfile.copy(name = name, email = email, sdt = sdt, birthday = birthday)
                        viewModel.updateUser(updatedUser)
                        Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) { Text("Cập nhật") }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { userViewModel.logout() },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) { Text("Đăng xuất") }
            }

            // Dialog chọn ảnh
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Thay đổi ảnh đại diện") },
                    text = {
                        Column {
                            OptionRow("Chụp ảnh", R.drawable.ic_cam) {
                                showDialog = false
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            Spacer(Modifier.height(8.dp))
                            OptionRow("Chọn từ thư viện", R.drawable.ic_gallery) {
                                showDialog = false
                                galleryLauncher.launch("image/*")
                            }
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
fun FieldCard(
    label: String,
    value: String,
    editable: Boolean,
    onEdit: () -> Unit,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            if (!editable) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(value.ifBlank { "Chưa cập nhật" }, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
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
                    TextButton(onClick = onSave) { Text("Lưu") }
                }
            }
        }
    }
}

@Composable
fun OptionRow(text: String, iconRes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(id = iconRes), contentDescription = text, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
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
