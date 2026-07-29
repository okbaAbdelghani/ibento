package com.okbatech.smartevents.core.presence

import android.util.Log
import com.okbatech.smartevents.core.datastore.EvenroPreferences
import com.okbatech.smartevents.core.di.IoDispatcher
import com.okbatech.smartevents.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PresenceHeartbeatManager"
private const val HEARTBEAT_INTERVAL_MS = 20_000L

/**
 * "Online" presence is tracked purely via the backend (see `POST users/me/heartbeat`) rather
 * than XMPP roster/presence — a deliberate choice to avoid adding new, unverified ejabberd
 * config (mod_shared_roster) on top of an already fragile-to-configure XMPP auth setup. While
 * signed in, pings the backend every ~20s; ChatViewModel derives "Online" vs "Last seen X ago"
 * for the peer by comparing against their polled `lastSeenAt`.
 */
@Singleton
class PresenceHeartbeatManager @Inject constructor(
    private val preferences: EvenroPreferences,
    private val authRepository: AuthRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun start(appScope: CoroutineScope) {
        appScope.launch(ioDispatcher) {
            preferences.currentUserId
                .distinctUntilChanged()
                // collectLatest cancels the still-running loop below the instant currentUserId
                // emits again (sign-out -> null cancels it; sign-in -> new id starts a fresh one).
                .collectLatest { userId -> if (userId != null) beatWhileLoggedIn() }
        }
    }

    private suspend fun beatWhileLoggedIn() {
        while (true) {
            authRepository.sendHeartbeat().onFailure { Log.e(TAG, "Heartbeat failed", it) }
            delay(HEARTBEAT_INTERVAL_MS)
        }
    }
}
