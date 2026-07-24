package com.okbatech.smartevents.feature.social.domain.repository

import com.okbatech.smartevents.feature.social.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(threadId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(threadId: String, senderId: String, body: String)
}
