package com.okbatech.smartevents.feature.social.data

import com.okbatech.smartevents.core.database.dao.MessageDao
import com.okbatech.smartevents.core.database.entity.MessageEntity
import com.okbatech.smartevents.core.xmpp.XmppManager
import com.okbatech.smartevents.feature.social.domain.model.ChatMessage
import com.okbatech.smartevents.feature.social.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val xmppManager: XmppManager,
) : ChatRepository {

    override fun observeMessages(threadId: String): Flow<List<ChatMessage>> =
        messageDao.observeByThread(threadId).map { list -> list.map { it.toDomain() } }

    override suspend fun sendMessage(threadId: String, senderId: String, body: String) {
        // Optimistic local echo — kept even if the XMPP send below fails, so the UI never
        // loses a message the user typed just because the connection was momentarily down.
        // Offline-queue retry on reconnect is future work, not handled here yet.
        messageDao.insert(
            MessageEntity(
                id = "m-${UUID.randomUUID()}",
                threadId = threadId,
                senderId = senderId,
                body = body,
                sentAt = System.currentTimeMillis(),
            ),
        )

        when {
            threadId.startsWith("dm_") -> {
                // threadId is "dm_<sortedIdA>_<sortedIdB>" (see ChatThreads.direct) — strip the
                // known senderId + its separator rather than a naive split, since ids may
                // themselves contain underscores.
                val otherUserId = threadId.removePrefix("dm_").replace(senderId, "").trim('_')
                if (otherUserId.isNotEmpty()) xmppManager.sendDirect(otherUserId, body)
            }
            threadId.startsWith("event_") -> {
                val eventId = threadId.removePrefix("event_")
                xmppManager.sendGroup(eventId, body)
            }
        }
    }
}

private fun MessageEntity.toDomain() = ChatMessage(
    id = id,
    threadId = threadId,
    senderId = senderId,
    body = body,
    sentAt = sentAt,
)
