package com.okbatech.smartevents.core.push

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.okbatech.smartevents.MainActivity
import com.okbatech.smartevents.R
import com.okbatech.smartevents.core.database.dao.MessageDao
import com.okbatech.smartevents.core.database.entity.MessageEntity
import com.okbatech.smartevents.core.webrtc.CallManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private const val TAG = "EvenroFcmService"
const val NOTIFICATION_CHANNEL_MESSAGES = "messages"
const val EXTRA_THREAD_ID = "extra_thread_id"
const val EXTRA_THREAD_TITLE = "extra_thread_title"

/**
 * Data-only push messages only (see backend `src/lib/push.ts`) — never a top-level FCM
 * `notification` payload, since that bypasses onMessageReceived() (and therefore the
 * "already viewing this thread" dedupe + deep-link-on-tap below) whenever the app isn't in
 * the foreground.
 */
@AndroidEntryPoint
class EvenroFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var pushTokenManager: PushTokenManager
    @Inject lateinit var messageDao: MessageDao
    @Inject lateinit var callManager: CallManager

    override fun onNewToken(token: String) {
        pushTokenManager.onTokenRefreshed(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data

        when (data["type"]) {
            "call_offer" -> {
                handleCallOfferPush(message, data)
                return
            }
            "call_cancel" -> {
                data["callId"]?.let { callManager.onIncomingCallCancelled(it) }
                return
            }
        }

        val threadId = data["threadId"] ?: return
        val title = data["title"] ?: return
        val body = data["body"] ?: return

        // Write the message straight into Room here, rather than relying on it eventually
        // arriving over XMPP (mod_offline flush after reconnect) or the app being foregrounded
        // — when the app was fully killed, tapping the notification launches a fresh process
        // whose XMPP connection may not catch up before the ChatScreen queries Room, which is
        // exactly why the message used to show as "missing" right after tapping the notification.
        // Blocking here (not fire-and-forget) matters too: this call must finish before
        // onMessageReceived returns, since Android may reclaim the process shortly after a
        // bare FCM wake-up if nothing else is keeping it alive.
        val senderId = data["senderId"]
        val messageId = data["messageId"]
        val sentAt = data["sentAt"]?.toLongOrNull()
        if (senderId != null && messageId != null && sentAt != null) {
            runBlocking {
                runCatching {
                    messageDao.insert(
                        MessageEntity(
                            id = messageId,
                            threadId = threadId,
                            senderId = senderId,
                            body = body,
                            sentAt = sentAt,
                        ),
                    )
                }.onFailure { Log.e(TAG, "Failed to persist push-received message", it) }
            }
        }

        // Already looking at this exact conversation — the message is already visible live,
        // showing a tray notification on top of it would just be noise.
        if (threadId == ActiveChatTracker.openThreadId) return

        // Only threadId/title are passed — MainActivity resolves the DM's "other user id" (or
        // group event id) itself once it knows the signed-in user, rather than this Service
        // (which has no easy synchronous access to the current session) guessing at it.
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_THREAD_ID, threadId)
            putExtra(EXTRA_THREAD_TITLE, title)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            threadId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this).notify(threadId.hashCode(), notification)
    }

    /** Backend sends this with androidPriority "high" (see calls.ts) specifically so this check
     * passes — a high-priority FCM message is one of the few exemptions Android 12+ grants for
     * starting a foreground service from the background, which CallManager needs to do here to
     * raise the incoming-call UI. The OS can itself downgrade a claimed-high message it doesn't
     * consider time-sensitive, so this is checked defensively rather than trusted from the payload. */
    private fun handleCallOfferPush(message: RemoteMessage, data: Map<String, String>) {
        if (message.priority != RemoteMessage.PRIORITY_HIGH) {
            Log.w(TAG, "call_offer push arrived at non-high priority (${message.priority}) — cannot raise call UI from background")
            return
        }
        val callId = data["callId"] ?: return
        val callerId = data["callerId"] ?: return
        val callerName = data["callerName"] ?: callerId
        val video = data["video"].toBoolean()
        callManager.onIncomingCallPush(callId, callerId, callerName, video)
    }
}
