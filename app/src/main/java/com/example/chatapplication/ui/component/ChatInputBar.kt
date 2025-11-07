package com.example.chatapplication.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChatInputBar(
    input: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachmentClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAttachmentClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Thêm",
                tint = Color(0xFF0084FF)
            )
        }

        TextField(
            value = input,
            onValueChange = onValueChange,
            placeholder = { Text("Aa") },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFF0F2F5)),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            maxLines = 4
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onSend,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0084FF)
            ),
            contentPadding = PaddingValues(12.dp)
        ) {
            Text("➤", color = Color.White)
        }
    }
}