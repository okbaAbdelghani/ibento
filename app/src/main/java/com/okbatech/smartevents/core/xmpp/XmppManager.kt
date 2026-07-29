package com.okbatech.smartevents.core.xmpp

import android.util.Log
import com.okbatech.smartevents.core.database.dao.MessageDao
import com.okbatech.smartevents.core.database.entity.MessageEntity
import com.okbatech.smartevents.core.datastore.EvenroPreferences
import com.okbatech.smartevents.core.di.IoDispatcher
import com.okbatech.smartevents.core.di.XMPP_DOMAIN
import com.okbatech.smartevents.core.di.XMPP_MUC_DOMAIN
import com.okbatech.smartevents.feature.social.domain.model.ChatThreads
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "XmppManager"

/**
 * Owns the app's single XMPP connection (Smack). Reactively watches [EvenroPreferences] for
 * the signed-in user/session JWT and connects/disconnects to ejabberd accordingly — this
 * covers fresh sign-in, sign-out, AND a returning user re-opening the app with an existing
 * session, all through the one existing choke point (`EvenroPreferences.setSession`), so no
 * auth call site needs to know about XMPP at all.
 *
 * Incoming messages are written directly into Room via [MessageDao] (not exposed as a Flow for
 * a repository to collect) so they're captured as long as this manager is alive — regardless
 * of whether any chat screen/ViewModel has been opened yet. Call [start] once from
 * Application.onCreate to begin the watcher.
 *
 * NOTE: the MUC create-vs-join flow and message/JID extraction below follow Smack's documented
 * patterns but haven't been exercised against a real ejabberd server yet — verify against the
 * pinned Smack version during the plan's local docker-compose verification pass before trusting
 * this in production.
 */
@Singleton
class XmppManager @Inject constructor(
    private val connection: XMPPTCPConnection,
    private val messageDao: MessageDao,
    private val preferences: EvenroPreferences,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val chatManager: ChatManager = ChatManager.getInstanceFor(connection)
    private val mucManager: MultiUserChatManager = MultiUserChatManager.getInstanceFor(connection)
    private val joinedRooms = mutableMapOf<String, MultiUserChat>()
    private val dbScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    @Volatile private var currentUserId: String? = null

    init {
        chatManager.addIncomingListener { from, message, _ ->
            val body = message.body ?: return@addIncomingListener
            val senderId = from.localpart.toString()
            val myId = currentUserId ?: return@addIncomingListener
            persistIncoming(ChatThreads.direct(myId, senderId), senderId, body, message.stanzaId)
        }
    }

    /** Call once (e.g. from Application.onCreate) to start watching the session and auto (re)connect. */
    fun start(scope: CoroutineScope) {
        scope.launch(ioDispatcher) {
            preferences.currentUserId
                .combine(preferences.sessionToken) { userId, token -> userId to token }
                .distinctUntilChanged()
                .collect { (userId, token) ->
                    if (userId != null && token != null) {
                        connectAndLogin(userId, token)
                    } else {
                        disconnect()
                    }
                }
        }
    }

    private suspend fun connectAndLogin(userId: String, jwt: String) = withContext(ioDispatcher) {
        runCatching {
            currentUserId = userId
            if (!connection.isConnected) connection.connect()
            if (!connection.isAuthenticated) connection.login(userId, jwt)
            Log.d(TAG, "connected+authenticated as $userId")
        }.onFailure { Log.e(TAG, "connectAndLogin failed for $userId", it) }
        // Failures are swallowed here deliberately — a flaky handshake shouldn't crash the
        // caller (preferences watcher / login flow). Future work: surface connection state
        // via a Flow<XmppConnectionState> if the UI needs to show "connecting.../offline".
    }

    fun disconnect() {
        Log.d(TAG, "disconnecting (was $currentUserId)")
        currentUserId = null
        joinedRooms.clear()
        if (connection.isConnected) connection.disconnect()
    }

    suspend fun sendDirect(toUserId: String, body: String) = withContext(ioDispatcher) {
        runCatching {
            val jid = JidCreate.entityBareFrom("$toUserId@$XMPP_DOMAIN")
            chatManager.chatWith(jid).send(body)
        }.onFailure { Log.e(TAG, "sendDirect to $toUserId failed", it) }
    }

    suspend fun sendGroup(eventId: String, body: String) = withContext(ioDispatcher) {
        runCatching {
            getOrJoinRoom(eventId).sendMessage(body)
        }.onFailure { Log.e(TAG, "sendGroup for event $eventId failed", it) }
    }

    private fun getOrJoinRoom(eventId: String): MultiUserChat {
        val roomId = ChatThreads.group(eventId)
        joinedRooms[roomId]?.let { return it }

        val roomJid = JidCreate.entityBareFrom("$roomId@$XMPP_MUC_DOMAIN")
        val muc = mucManager.getMultiUserChat(roomJid)
        val nickname = Resourcepart.from(currentUserId ?: "guest")

        runCatching { muc.join(nickname) }
            .onFailure {
                // Room doesn't exist yet — create it as an instant room with default config.
                muc.create(nickname)
                runCatching { muc.sendConfigurationForm(muc.configurationForm.fillableForm) }
            }

        muc.addMessageListener { message ->
            val body = message.body ?: return@addMessageListener
            val senderId = message.from?.asEntityFullJidIfPossible()?.resourcepart?.toString()
                ?: return@addMessageListener
            if (senderId == currentUserId) return@addMessageListener
            persistIncoming(roomId, senderId, body, message.stanzaId)
        }

        joinedRooms[roomId] = muc
        return muc
    }

    private fun persistIncoming(threadId: String, senderId: String, body: String, stanzaId: String?) {
        Log.d(TAG, "incoming into $threadId from $senderId: $body")
        dbScope.launch {
            runCatching {
                messageDao.insert(
                    MessageEntity(
                        id = "m-${stanzaId ?: UUID.randomUUID()}",
                        threadId = threadId,
                        senderId = senderId,
                        body = body,
                        sentAt = System.currentTimeMillis(),
                    ),
                )
            }.onSuccess {
                Log.d(TAG, "persisted OK into $threadId")
            }.onFailure {
                Log.e(TAG, "persist FAILED into $threadId", it)
            }
        }
    }
}
