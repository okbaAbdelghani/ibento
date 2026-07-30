package com.okbatech.smartevents.core.xmpp

import android.util.Log
import com.okbatech.smartevents.core.crash.CrashReporter
import com.okbatech.smartevents.core.database.dao.MessageDao
import com.okbatech.smartevents.core.database.entity.MessageEntity
import com.okbatech.smartevents.core.datastore.EvenroPreferences
import com.okbatech.smartevents.core.di.IoDispatcher
import com.okbatech.smartevents.core.di.XMPP_DOMAIN
import com.okbatech.smartevents.core.di.XMPP_MUC_DOMAIN
import com.okbatech.smartevents.core.webrtc.CallEndReason
import com.okbatech.smartevents.core.webrtc.CallSignal
import com.okbatech.smartevents.feature.social.domain.model.ChatThreads
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.ReconnectionManager
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.StandardExtensionElement
import org.jivesoftware.smackx.chatstates.ChatState
import org.jivesoftware.smackx.chatstates.ChatStateListener
import org.jivesoftware.smackx.chatstates.ChatStateManager
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jivesoftware.smackx.ping.PingManager
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "XmppManager"
private const val READ_MARKER_ELEMENT = "read"
private const val READ_MARKER_NAMESPACE = "evenro:read"
private const val TYPING_TIMEOUT_MS = 5_000L

private const val CALL_ELEMENT = "call"
private const val CALL_NAMESPACE = "evenro:call"

// ReconnectionManager only reacts to a connection that was already established later dropping —
// a failure on the very first connect()/login() call (e.g. a TLS handshake timeout right after
// app launch, confirmed live) isn't its concern at all, and nothing else here would ever retry it
// since preferences.currentUserId/sessionToken aren't changing. Without this, that one bad first
// attempt left every XMPP-dependent feature (typing, receipts, calls) silently dead until the
// user force-closed and reopened the app.
private const val CONNECT_RETRY_INITIAL_DELAY_MS = 2_000L
private const val CONNECT_RETRY_MAX_DELAY_MS = 30_000L

/** Smack's ChatManager silently drops a body-less incoming message before it ever reaches
 * addIncomingListener (see sendCallElement/sendReadMarker) — every extension-only stanza needs
 * a placeholder body to actually arrive. Never surfaced to the user: handled and returned from
 * before persistIncoming would treat it as a real chat message body. */
private const val EXTENSION_ONLY_BODY = "[evenro-signal]"

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
 * Also owns three lighter-weight chat features, all client-side/message-scoped (no ejabberd
 * config needed — see the "online" presence, which is deliberately backend-tracked instead):
 * delivered receipts (XEP-0184, via Smack's built-in [DeliveryReceiptManager]), typing states
 * (XEP-0085, via [ChatStateManager]), and read receipts (a small custom "read up to timestamp"
 * stanza extension, since full XEP-0333 chat-marker support isn't guaranteed in this Smack
 * version) — all 1:1 DM only, not group chat, for this pass.
 *
 * Enables Smack's [ReconnectionManager] with a short ping interval (see init) — confirmed via
 * live device logs that without this, a silently dropped TCP connection (mobile network
 * handoff/idle timeout/Doze) never recovers on its own, causing exactly the "message doesn't
 * send/receive sometimes" symptom this was built to fix.
 */
@Singleton
class XmppManager @Inject constructor(
    private val connection: XMPPTCPConnection,
    private val messageDao: MessageDao,
    private val preferences: EvenroPreferences,
    private val crashReporter: CrashReporter,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val chatManager: ChatManager = ChatManager.getInstanceFor(connection)
    private val mucManager: MultiUserChatManager = MultiUserChatManager.getInstanceFor(connection)
    private val chatStateManager: ChatStateManager = ChatStateManager.getInstance(connection)
    private val deliveryReceiptManager: DeliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(connection)
    private val joinedRooms = mutableMapOf<String, MultiUserChat>()
    private val dbScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // All outgoing stanza sends funnel through this single thread — confirmed live that letting
    // many coroutines call into Smack concurrently (e.g. CallManager firing one coroutine per
    // ICE candidate) can wedge Smack's own NIO reactor thread (caught via an ANR: main thread
    // blocked 48s on a monitor held by "Smack DefaultReactor Thread #1" inside
    // SelectorImpl.lockAndDoSelect). Smack's connection isn't verified safe for concurrent
    // sendStanza from multiple threads under load, so don't rely on it being so.
    private val xmppDispatcher = ioDispatcher.limitedParallelism(1)
    private val typingTimeoutJobs = ConcurrentHashMap<String, Job>()
    private var connectRetryJob: Job? = null

    @Volatile private var currentUserId: String? = null

    private val _typingByUser = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    /** Keyed by the *other* user's id — true while they're composing a reply to me. DM only. */
    val typingByUser: StateFlow<Map<String, Boolean>> = _typingByUser.asStateFlow()

    private val _incomingCallSignals = MutableSharedFlow<CallSignal>(extraBufferCapacity = 8)
    /** Call offer/answer/ICE-candidate/end stanzas — collected by CallManager. */
    val incomingCallSignals: SharedFlow<CallSignal> = _incomingCallSignals.asSharedFlow()

    init {
        chatManager.addIncomingListener { from, message, _ ->
            val callElement = message.getExtensionElement(CALL_ELEMENT, CALL_NAMESPACE)
            if (callElement is StandardExtensionElement) {
                handleCallSignal(from.localpart.toString(), callElement)
                return@addIncomingListener
            }

            val readMarker = message.getExtensionElement(READ_MARKER_ELEMENT, READ_MARKER_NAMESPACE)
            if (readMarker is StandardExtensionElement) {
                handleReadMarker(readMarker)
                return@addIncomingListener
            }

            val body = message.body ?: return@addIncomingListener
            val senderId = from.localpart.toString()
            val myId = currentUserId ?: return@addIncomingListener
            persistIncoming(ChatThreads.direct(myId, senderId), senderId, body, message.stanzaId)
        }

        // Auto-request a delivery receipt on every message we send, and auto-respond to
        // receipt requests on messages we receive (Smack's default AutoReceiptMode already
        // covers "chat"/"groupchat" stanza types, which is exactly what ChatManager/MUC use).
        deliveryReceiptManager.autoAddDeliveryReceiptRequests()
        deliveryReceiptManager.addReceiptReceivedListener { _, _, receiptId, _ ->
            markDelivered(receiptId)
        }

        chatStateManager.addChatStateListener(
            ChatStateListener { chat, state, _ ->
                val otherUserId = chat.xmppAddressOfChatPartner.localpart.toString()
                onTypingStateReceived(otherUserId, state == ChatState.composing)
            },
        )

        // Without this, a dropped connection (mobile network handoff, carrier/NAT idle
        // timeout, Doze) silently stays dead forever — every send/receive fails from then on
        // with no visible symptom until the app process is killed and restarted, since nothing
        // else here re-triggers connectAndLogin (it only runs again when currentUserId/
        // sessionToken actually change). ReconnectionManager detects the drop and reconnects
        // + re-logs-in automatically using the same credentials passed to the first login().
        ReconnectionManager.getInstanceFor(connection).enableAutomaticReconnection()
        // Smack's default ping interval (30 min) is far longer than most mobile NATs/carriers
        // will hold an idle TCP connection open — shorten it so a drop is detected (and
        // reconnection kicked off) within a minute or two instead of only surfacing the next
        // time something tries to actually send a stanza.
        PingManager.getInstanceFor(connection).pingInterval = 60

        connection.addConnectionListener(
            object : ConnectionListener {
                override fun connected(connection: XMPPConnection) {
                    Log.d(TAG, "connection established")
                }

                override fun authenticated(connection: XMPPConnection, resumed: Boolean) {
                    Log.d(TAG, "authenticated (resumed=$resumed)")
                }

                override fun connectionClosed() {
                    Log.d(TAG, "connection closed")
                }

                override fun connectionClosedOnError(e: Exception) {
                    Log.e(TAG, "connection closed on error — ReconnectionManager will retry", e)
                    crashReporter.recordException(e, "XMPP connectionClosedOnError")
                }
            },
        )
    }

    /** Call once (e.g. from Application.onCreate) to start watching the session and auto (re)connect. */
    fun start(scope: CoroutineScope) {
        scope.launch(xmppDispatcher) {
            preferences.currentUserId
                .combine(preferences.sessionToken) { userId, token -> userId to token }
                .distinctUntilChanged()
                .collect { (userId, token) ->
                    // A new emission means the session itself changed (login/logout/token
                    // refresh) — any retry loop still spinning for the *previous* one is stale
                    // and must stop instead of racing this one (or, on logout, continuing to
                    // hammer connect() for a user who's no longer signed in).
                    connectRetryJob?.cancel()
                    if (userId != null && token != null) {
                        connectRetryJob = scope.launch(xmppDispatcher) { connectWithRetry(userId, token) }
                    } else {
                        disconnect()
                    }
                }
        }
    }

    // Runs as its own job (see start()) rather than inline in the collector above so a still-
    // retrying attempt can be cancelled independently of — and without blocking — the collector
    // noticing the *next* preferences change.
    private suspend fun connectWithRetry(userId: String, jwt: String) {
        var delayMs = CONNECT_RETRY_INITIAL_DELAY_MS
        while (true) {
            if (connectAndLogin(userId, jwt)) return
            delay(delayMs)
            delayMs = (delayMs * 2).coerceAtMost(CONNECT_RETRY_MAX_DELAY_MS)
        }
    }

    private suspend fun connectAndLogin(userId: String, jwt: String): Boolean = withContext(xmppDispatcher) {
        runCatching {
            currentUserId = userId
            if (!connection.isConnected) connection.connect()
            if (!connection.isAuthenticated) connection.login(userId, jwt)
            Log.d(TAG, "connected+authenticated as $userId")
        }.onFailure {
            Log.e(TAG, "connectAndLogin failed for $userId", it)
            crashReporter.recordException(it, "connectAndLogin failed for $userId")
        }.isSuccess
    }

    fun disconnect() {
        Log.d(TAG, "disconnecting (was $currentUserId)")
        currentUserId = null
        joinedRooms.clear()
        typingTimeoutJobs.values.forEach { it.cancel() }
        typingTimeoutJobs.clear()
        _typingByUser.value = emptyMap()
        if (connection.isConnected) connection.disconnect()
    }

    /** [messageId] must match the local Room row's id — delivery receipts key off the stanza id. */
    suspend fun sendDirect(toUserId: String, body: String, messageId: String) = withContext(xmppDispatcher) {
        runCatching {
            val jid = JidCreate.entityBareFrom("$toUserId@$XMPP_DOMAIN")
            val message = Message().apply {
                stanzaId = messageId
                setBody(body)
            }
            chatManager.chatWith(jid).send(message)
        }.onFailure { Log.e(TAG, "sendDirect to $toUserId failed", it) }
    }

    suspend fun sendGroup(eventId: String, body: String, messageId: String) = withContext(xmppDispatcher) {
        runCatching {
            val message = Message().apply {
                stanzaId = messageId
                setBody(body)
            }
            @Suppress("DEPRECATION") // sendMessage(Message) still present in Smack 4.4.x
            getOrJoinRoom(eventId).sendMessage(message)
        }.onFailure { Log.e(TAG, "sendGroup for event $eventId failed", it) }
    }

    /** DM typing indicator (XEP-0085). Debounce/throttle on the caller side (ChatViewModel). */
    suspend fun sendTypingState(otherUserId: String, isTyping: Boolean) = withContext(xmppDispatcher) {
        runCatching {
            val jid = JidCreate.entityBareFrom("$otherUserId@$XMPP_DOMAIN")
            chatStateManager.setCurrentState(
                if (isTyping) ChatState.composing else ChatState.paused,
                chatManager.chatWith(jid),
            )
        }.onFailure { Log.e(TAG, "sendTypingState to $otherUserId failed", it) }
    }

    /** Tells [otherUserId] "I've read everything you sent me up to [upToSentAt]" (a read receipt). */
    suspend fun sendReadMarker(otherUserId: String, threadId: String, upToSentAt: Long) = withContext(xmppDispatcher) {
        runCatching {
            val jid = JidCreate.entityBareFrom("$otherUserId@$XMPP_DOMAIN")
            val marker = StandardExtensionElement.builder(READ_MARKER_ELEMENT, READ_MARKER_NAMESPACE)
                .addAttribute("threadId", threadId)
                .addAttribute("upToMillis", upToSentAt.toString())
                .build()
            // Smack's ChatManager only dispatches an incoming stanza to addIncomingListener when
            // it has a body (or the built-in XHTML-IM extension) — MessageWithBodiesFilter drops
            // anything else before it ever reaches our listener (confirmed by decompiling
            // ChatManager's MESSAGE_FILTER; a body-less extension-only message never arrives on
            // the receiving end at all). The placeholder body is harmless: handleReadMarker's
            // caller returns before this message would ever be treated as a chat body.
            chatManager.chatWith(jid).send(Message().apply { setBody(EXTENSION_ONLY_BODY); addExtension(marker) })
        }.onFailure { Log.e(TAG, "sendReadMarker to $otherUserId failed", it) }
    }

    suspend fun sendCallOffer(toUserId: String, callId: String, video: Boolean, sdp: String) =
        withContext(xmppDispatcher) {
            runCatching {
                val element = StandardExtensionElement.builder(CALL_ELEMENT, CALL_NAMESPACE)
                    .addAttribute("type", "offer")
                    .addAttribute("callId", callId)
                    .addAttribute("video", video.toString())
                    .setText(sdp)
                    .build()
                sendCallElement(toUserId, element)
            }.onFailure {
                Log.e(TAG, "sendCallOffer to $toUserId failed", it)
                crashReporter.recordException(it, "sendCallOffer to $toUserId failed")
            }
        }

    suspend fun sendCallAnswer(toUserId: String, callId: String, sdp: String) = withContext(xmppDispatcher) {
        runCatching {
            val element = StandardExtensionElement.builder(CALL_ELEMENT, CALL_NAMESPACE)
                .addAttribute("type", "answer")
                .addAttribute("callId", callId)
                .setText(sdp)
                .build()
            sendCallElement(toUserId, element)
        }.onFailure {
            Log.e(TAG, "sendCallAnswer to $toUserId failed", it)
            crashReporter.recordException(it, "sendCallAnswer to $toUserId failed")
        }
    }

    suspend fun sendCallIceCandidate(
        toUserId: String,
        callId: String,
        sdpMid: String,
        sdpMLineIndex: Int,
        candidate: String,
    ) = withContext(xmppDispatcher) {
        runCatching {
            val element = StandardExtensionElement.builder(CALL_ELEMENT, CALL_NAMESPACE)
                .addAttribute("type", "candidate")
                .addAttribute("callId", callId)
                .addAttribute("sdpMid", sdpMid)
                .addAttribute("sdpMLineIndex", sdpMLineIndex.toString())
                .setText(candidate)
                .build()
            sendCallElement(toUserId, element)
        }.onFailure {
            Log.e(TAG, "sendCallIceCandidate to $toUserId failed", it)
            crashReporter.recordException(it, "sendCallIceCandidate to $toUserId failed")
        }
    }

    suspend fun sendCallEnd(toUserId: String, callId: String, reason: CallEndReason) = withContext(xmppDispatcher) {
        runCatching {
            val element = StandardExtensionElement.builder(CALL_ELEMENT, CALL_NAMESPACE)
                .addAttribute("type", "end")
                .addAttribute("callId", callId)
                .addAttribute("reason", reason.name.lowercase())
                .build()
            sendCallElement(toUserId, element)
        }.onFailure {
            Log.e(TAG, "sendCallEnd to $toUserId failed", it)
            crashReporter.recordException(it, "sendCallEnd to $toUserId failed")
        }
    }

    private fun sendCallElement(toUserId: String, element: StandardExtensionElement) {
        val jid = JidCreate.entityBareFrom("$toUserId@$XMPP_DOMAIN")
        // See the comment on sendReadMarker — same body-less-stanza-gets-dropped issue applies
        // here, and is far more consequential for calling: a dropped offer just means the phone
        // never rings, no retry-driven "eventually shows up" fallback like a chat message has.
        chatManager.chatWith(jid).send(Message().apply { setBody(EXTENSION_ONLY_BODY); addExtension(element) })
    }

    private fun handleCallSignal(fromUserId: String, element: StandardExtensionElement) {
        val callId = element.getAttributeValue("callId") ?: return
        val signal = when (element.getAttributeValue("type")) {
            "offer" -> {
                val sdp = element.text ?: return
                val video = element.getAttributeValue("video").toBoolean()
                CallSignal.Offer(callId, fromUserId, sdp, video)
            }
            "answer" -> {
                val sdp = element.text ?: return
                CallSignal.Answer(callId, fromUserId, sdp)
            }
            "candidate" -> {
                val sdpMid = element.getAttributeValue("sdpMid") ?: return
                val sdpMLineIndex = element.getAttributeValue("sdpMLineIndex")?.toIntOrNull() ?: return
                val candidate = element.text ?: return
                CallSignal.IceCandidate(callId, fromUserId, sdpMid, sdpMLineIndex, candidate)
            }
            "end" -> {
                val reason = element.getAttributeValue("reason")?.uppercase()?.let {
                    runCatching { CallEndReason.valueOf(it) }.getOrNull()
                } ?: CallEndReason.HANGUP
                CallSignal.End(callId, fromUserId, reason)
            }
            else -> null
        } ?: return
        Log.d(TAG, "incoming call signal: $signal")
        _incomingCallSignals.tryEmit(signal)
    }

    private fun onTypingStateReceived(otherUserId: String, isComposing: Boolean) {
        typingTimeoutJobs.remove(otherUserId)?.cancel()
        _typingByUser.update { it + (otherUserId to isComposing) }

        if (isComposing) {
            // Safety net: some clients don't reliably send a "paused" state, so locally clear
            // the flag if no refresh arrives within the timeout.
            typingTimeoutJobs[otherUserId] = dbScope.launch {
                delay(TYPING_TIMEOUT_MS)
                _typingByUser.update { it + (otherUserId to false) }
            }
        }
    }

    private fun handleReadMarker(element: StandardExtensionElement) {
        val threadId = element.getAttributeValue("threadId") ?: return
        val upToMillis = element.getAttributeValue("upToMillis")?.toLongOrNull() ?: return
        val myId = currentUserId ?: return
        dbScope.launch {
            runCatching { messageDao.markReadUpTo(threadId, myId, upToMillis, System.currentTimeMillis()) }
                .onFailure { Log.e(TAG, "markReadUpTo failed for $threadId", it) }
        }
    }

    private fun markDelivered(stanzaId: String) {
        dbScope.launch {
            runCatching { messageDao.markDelivered(stanzaId, System.currentTimeMillis()) }
                .onFailure { Log.e(TAG, "markDelivered failed for $stanzaId", it) }
        }
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
                        // stanzaId is already the sender's own Room row id verbatim (see
                        // ChatRepositoryImpl.sendMessage / XmppManager.sendDirect/sendGroup,
                        // which set message.stanzaId = the local "m-<uuid>" id directly) — don't
                        // re-prefix it, or this row's id won't match what a push-notification-
                        // triggered insert for the same message uses, breaking dedup between
                        // the two delivery paths.
                        id = stanzaId ?: "m-${UUID.randomUUID()}",
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
