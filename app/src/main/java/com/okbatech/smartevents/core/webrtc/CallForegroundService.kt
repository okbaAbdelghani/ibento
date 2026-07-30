package com.okbatech.smartevents.core.webrtc

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.okbatech.smartevents.feature.call.presentation.CallActivity

private const val NOTIFICATION_ID = 42001

const val NOTIFICATION_CHANNEL_CALLS = "calls"

private const val ACTION_RINGING = "ringing"
private const val ACTION_ACTIVE = "active"
private const val ACTION_STOP = "stop"

const val EXTRA_CALL_ID = "callId"
const val EXTRA_OTHER_USER_ID = "otherUserId"
const val EXTRA_OTHER_NAME = "otherName"
const val EXTRA_VIDEO = "video"
const val EXTRA_IS_INCOMING = "isIncoming"

/** Owns the incoming-call / ongoing-call notification lifecycle. Runs as a foreground service
 * (type microphone|camera, see AndroidManifest) so the audio pipeline survives the app being
 * backgrounded mid-call — a plain background coroutine would risk getting killed by Android's
 * background execution limits. */
class CallForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            FirebaseCrashlytics.getInstance().log("CallForegroundService action=${intent?.action}")
            when (intent?.action) {
                ACTION_RINGING -> startRinging(intent)
                ACTION_ACTIVE -> startActive(intent)
                ACTION_STOP -> {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        } catch (t: Throwable) {
            // Previously crashed the whole process here (missing-permission SecurityException on
            // startForeground) — that path is fixed, but keep this net + report so any future
            // regression surfaces as a Crashlytics non-fatal instead of a silent process death.
            Log.e("CallForegroundService", "onStartCommand threw", t)
            FirebaseCrashlytics.getInstance().recordException(t)
        }
        return START_NOT_STICKY
    }

    private fun startRinging(intent: Intent) {
        val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: return
        val otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: return
        val otherName = intent.getStringExtra(EXTRA_OTHER_NAME) ?: otherUserId
        val video = intent.getBooleanExtra(EXTRA_VIDEO, false)

        val fullScreenIntent = callActivityIntent(this, callId, otherUserId, otherName, video, isIncoming = true)
        val pendingIntent = PendingIntent.getActivity(
            this,
            callId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_CALLS)
            .setSmallIcon(com.okbatech.smartevents.R.drawable.ic_stat_notification)
            .setContentTitle(otherName)
            .setContentText(if (video) "Incoming video call" else "Incoming voice call")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)

        // USE_FULL_SCREEN_INTENT isn't auto-granted by default on API 34+ for apps the OS
        // doesn't consider a "calling" app — fall back to a normal heads-up notification
        // (still has the tap-to-open contentIntent above) rather than assuming it always fires.
        if (NotificationManagerCompat.from(this).canUseFullScreenIntent()) {
            builder.setFullScreenIntent(pendingIntent, true)
        }

        startForegroundCompat(builder.build())
    }

    private fun startActive(intent: Intent) {
        val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: return
        val otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: return
        val otherName = intent.getStringExtra(EXTRA_OTHER_NAME) ?: otherUserId
        val video = intent.getBooleanExtra(EXTRA_VIDEO, false)

        val contentIntent = callActivityIntent(this, callId, otherUserId, otherName, video, isIncoming = false)
        val pendingIntent = PendingIntent.getActivity(
            this,
            callId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_CALLS)
            .setSmallIcon(com.okbatech.smartevents.R.drawable.ic_stat_notification)
            .setContentTitle(otherName)
            .setContentText("In call")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setUsesChronometer(true)
            .setContentIntent(pendingIntent)
            .build()

        startForegroundCompat(notification)
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Requesting a type this device hasn't actually granted the underlying runtime
            // permission for throws a SecurityException that kills the whole process (hit this
            // live: an incoming call's ringing notification crashed because RECORD_AUDIO isn't
            // requested until CallActivity opens — too late, since this service has to start
            // first to raise that UI). Build the type mask from what's actually granted instead
            // of assuming both are — a call still needs to ring/notify even if the user hasn't
            // (yet, or ever) granted mic/camera; CallManager surfaces the real failure once it
            // tries to open the mic and hits the same missing permission.
            var type = 0
            if (hasPermission(android.Manifest.permission.RECORD_AUDIO)) {
                type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            if (hasPermission(android.Manifest.permission.CAMERA)) {
                type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            }
            startForeground(NOTIFICATION_ID, notification, type)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    companion object {
        fun ring(context: Context, callId: String, otherUserId: String, otherName: String, video: Boolean) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                action = ACTION_RINGING
                putExtra(EXTRA_CALL_ID, callId)
                putExtra(EXTRA_OTHER_USER_ID, otherUserId)
                putExtra(EXTRA_OTHER_NAME, otherName)
                putExtra(EXTRA_VIDEO, video)
            }
            context.startForegroundService(intent)
        }

        fun active(context: Context, callId: String, otherUserId: String, otherName: String, video: Boolean) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                action = ACTION_ACTIVE
                putExtra(EXTRA_CALL_ID, callId)
                putExtra(EXTRA_OTHER_USER_ID, otherUserId)
                putExtra(EXTRA_OTHER_NAME, otherName)
                putExtra(EXTRA_VIDEO, video)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, CallForegroundService::class.java).apply { action = ACTION_STOP })
        }

        fun callActivityIntent(
            context: Context,
            callId: String,
            otherUserId: String,
            otherName: String,
            video: Boolean,
            isIncoming: Boolean,
        ): Intent = Intent(context, CallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_OTHER_USER_ID, otherUserId)
            putExtra(EXTRA_OTHER_NAME, otherName)
            putExtra(EXTRA_VIDEO, video)
            putExtra(EXTRA_IS_INCOMING, isIncoming)
        }
    }
}
