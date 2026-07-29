package com.okbatech.smartevents.feature.social.domain.repository

import com.okbatech.smartevents.feature.social.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(threadId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(threadId: String, senderId: String, body: String)
    fun observeLastMessage(threadId: String): Flow<ChatMessage?>
    fun observeThreadUnreadCount(threadId: String, myUserId: String): Flow<Int>
    fun observeTotalUnreadCount(myUserId: String): Flow<Int>
    suspend fun markThreadSeen(threadId: String, myUserId: String)
}
