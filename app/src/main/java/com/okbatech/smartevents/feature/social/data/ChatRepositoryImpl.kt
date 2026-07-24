package com.okbatech.smartevents.feature.social.data

import com.okbatech.smartevents.core.database.dao.MessageDao
import com.okbatech.smartevents.core.database.entity.MessageEntity
import com.okbatech.smartevents.feature.social.domain.model.ChatMessage
import com.okbatech.smartevents.feature.social.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
) : ChatRepository {

    override fun observeMessages(threadId: String): Flow<List<ChatMessage>> =
        messageDao.observeByThread(threadId).map { list -> list.map { it.toDomain() } }

    override suspend fun sendMessage(threadId: String, senderId: String, body: String) {
        messageDao.insert(
            MessageEntity(
                id = "m-${UUID.randomUUID()}",
                threadId = threadId,
                senderId = senderId,
                body = body,
                sentAt = System.currentTimeMillis(),
            ),
        )
    }
}

private fun MessageEntity.toDomain() = ChatMessage(
    id = id,
    threadId = threadId,
    senderId = senderId,
    body = body,
    sentAt = sentAt,
)
