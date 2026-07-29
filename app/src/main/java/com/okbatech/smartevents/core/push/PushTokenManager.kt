package com.okbatech.smartevents.core.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.okbatech.smartevents.core.datastore.EvenroPreferences
import com.okbatech.smartevents.core.di.IoDispatcher
import com.okbatech.smartevents.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PushTokenManager"

/**
 * Registers this device's FCM token with the backend whenever the user is signed in, and
 * again whenever FCM rotates the token (see [EvenroFirebaseMessagingService.onNewToken]).
 * Mirrors [com.okbatech.smartevents.core.xmpp.XmppManager]'s reactive-singleton shape.
 */
@Singleton
class PushTokenManager @Inject constructor(
    private val preferences: EvenroPreferences,
    private val authRepository: AuthRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    /** Call once (e.g. from Application.onCreate) alongside XmppManager.start(). */
    fun start(appScope: CoroutineScope) {
        preferences.currentUserId
            .filterNotNull()
            .onEach { registerCurrentToken() }
            .launchIn(appScope)
    }

    /** Called from [EvenroFirebaseMessagingService.onNewToken]. */
    fun onTokenRefreshed(token: String) {
        scope.launch { register(token) }
    }

    private suspend fun registerCurrentToken() {
        runCatching { FirebaseMessaging.getInstance().token.await() }
            .onSuccess { register(it) }
            .onFailure { Log.e(TAG, "Failed to fetch FCM token", it) }
    }

    private suspend fun register(token: String) {
        authRepository.registerDeviceToken(token)
            .onFailure { Log.e(TAG, "Failed to register device token", it) }
    }
}
