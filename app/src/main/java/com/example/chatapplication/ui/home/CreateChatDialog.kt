package com.example.chatapplication.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateChatDialog(
    isGroup: Boolean,
    onDismiss: () -> Unit,
    onCreate: (List<String>) -> Unit
) {
    var email1 by remember { mutableStateOf("") }
    var email2 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (isGroup) {
                    val emails = listOf(email1, email2).filter { it.isNotBlank() }
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
                }
            }
        }
    )
}