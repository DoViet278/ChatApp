package com.example.chatapplication.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chatapplication.R
import com.example.chatapplication.ui.viewmodel.ChatViewModel
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton

@Composable
fun CallButton(
    isVideoCall: Boolean,
    onClick: ()-> Unit,
    onInit: (ZegoSendCallInvitationButton) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            painter = painterResource(
                id = if (isVideoCall) R.drawable.ic_video_call else R.drawable.ic_phone_call
            ),
            contentDescription = null,
            tint = Color(0xFF2196F3),
            modifier = Modifier.size(22.dp).clickable(onClick = onClick)
        )

        AndroidView(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f),
            factory = { context ->
                ZegoSendCallInvitationButton(context).apply {
                    setIsVideoCall(isVideoCall)
                    resourceID = "zego_data"
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    background = null
                    setOnClickListener {
                        onInit(this)
                    }
                }
            }
        )
    }
}