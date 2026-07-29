package com.okbatech.smartevents.feature.social.data

import com.okbatech.smartevents.core.database.dao.MessageDao
import com.okbatech.smartevents.core.database.entity.MessageEntity
import com.okbatech.smartevents.core.di.IoDispatcher
import com.okbatech.smartevents.core.network.ApiService
import com.okbatech.smartevents.core.network.PushNotifyRequest
import com.okbatech.smartevents.core.xmpp.XmppManager
import com.okbatech.smartevents.feature.social.domain.model.ChatMessage
import com.okbatech.smartevents.feature.social.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val xmppManager: XmppManager,
    private val api: ApiService,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) : ChatRepository {

    // Push notifications are best-effort — never let a slow/failed backend call delay the
    // suspend sendMessage() call (and therefore the UI) waiting for it.
    private val pushScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override fun observeMessages(threadId: String): Flow<List<ChatMessage>> =
        messageDao.observeByThread(threadId).map { list -> list.map { it.toDomain() } }

    override fun observeLastMessage(threadId: String): Flow<ChatMessage?> =
        messageDao.observeLastMessage(threadId).map { it?.toDomain() }

    override fun observeThreadUnreadCount(threadId: String, myUserId: String): Flow<Int> =
        messageDao.observeThreadUnreadCount(threadId, myUserId)

    override fun observeTotalUnreadCount(myUserId: String): Flow<Int> =
        messageDao.observeTotalUnreadCount(myUserId)

    override suspend fun markThreadSeen(threadId: String, myUserId: String) {
        messageDao.markThreadSeen(threadId, myUserId, System.currentTimeMillis())
    }

    override suspend fun sendMessage(threadId: String, senderId: String, body: String) {
        // The id is generated up front and reused as the actual XMPP stanza id (see
        // XmppManager.sendDirect/sendGroup) so a later delivery receipt — which references the
        // stanza id — can be correlated back to this exact Room row.
        val id = "m-${UUID.randomUUID()}"
        val sentAt = System.currentTimeMillis()

        // Optimistic local echo — kept even if the XMPP send below fails, so the UI never
        // loses a message the user typed just because the connection was momentarily down.
        // Offline-queue retry on reconnect is future work, not handled here yet.
        messageDao.insert(
            MessageEntity(
                id = id,
                threadId = threadId,
                senderId = senderId,
                body = body,
                sentAt = sentAt,
            ),
        )

        when {
            threadId.startsWith("dm_") -> {
                // threadId is "dm_<sortedIdA>_<sortedIdB>" (see ChatThreads.direct) — strip the
                // known senderId + its separator rather than a naive split, since ids may
                // themselves contain underscores.
                val otherUserId = threadId.removePrefix("dm_").replace(senderId, "").trim('_')
                if (otherUserId.isNotEmpty()) xmppManager.sendDirect(otherUserId, body, id)
            }
            threadId.startsWith("event_") -> {
                val eventId = threadId.removePrefix("event_")
                xmppManager.sendGroup(eventId, body, id)
            }
        }

        notifyPush(threadId, body, id, sentAt)
    }

    private fun notifyPush(threadId: String, body: String, messageId: String, sentAt: Long) {
        pushScope.launch {
            runCatching { api.notifyPush(PushNotifyRequest(threadId, body, messageId, sentAt)) }
        }
    }
}

private fun MessageEntity.toDomain() = ChatMessage(
    id = id,
    threadId = threadId,
    senderId = senderId,
    body = body,
    sentAt = sentAt,
    deliveredAt = deliveredAt,
    readAt = readAt,
)
