package com.example.chatapplication.data.repository

import im.zego.zim.ZIM
import im.zego.zim.entity.ZIMConversation
import im.zego.zim.entity.ZIMConversationQueryConfig
import im.zego.zim.enums.ZIMErrorCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class ChatRepository @Inject constructor(
    private val zim: ZIM
) {
    suspend fun getConversations(): List<ZIMConversation> =
        suspendCancellableCoroutine { cont ->
            val config = ZIMConversationQueryConfig().apply {
                count = 50
            }

            zim.queryConversationList(config) { conversationList, errorInfo ->
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    cont.resume(conversationList)
                } else {
                    cont.resume(emptyList())
                }
            }
        }
}