package com.okbatech.smartevents

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.okbatech.smartevents.core.database.DatabaseSeeder
import com.okbatech.smartevents.core.presence.PresenceHeartbeatManager
import com.okbatech.smartevents.core.push.NOTIFICATION_CHANNEL_MESSAGES
import com.okbatech.smartevents.core.push.PushTokenManager
import com.okbatech.smartevents.core.webrtc.CallManager
import com.okbatech.smartevents.core.webrtc.NOTIFICATION_CHANNEL_CALLS
import com.okbatech.smartevents.core.xmpp.XmppManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SmartEventsApp : Application() {

    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject lateinit var xmppManager: XmppManager
    @Inject lateinit var pushTokenManager: PushTokenManager
    @Inject lateinit var presenceHeartbeatManager: PresenceHeartbeatManager
    @Inject lateinit var callManager: CallManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        applicationScope.launch { databaseSeeder.seedIfNeeded() }
        xmppManager.start(applicationScope)
        pushTokenManager.start(applicationScope)
        presenceHeartbeatManager.start(applicationScope)
        callManager.start(applicationScope)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_MESSAGES,
                getString(R.string.notification_channel_messages),
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
        manager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_CALLS,
                getString(R.string.notification_channel_calls),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { setBypassDnd(false) },
        )
    }
}
