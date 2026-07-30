package com.okbatech.smartevents.core.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.okbatech.smartevents.core.datastore.EvenroPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around FirebaseCrashlytics — single import point for the rest of the app, and a
 * seam other managers can log breadcrumbs/non-fatals through instead of just Log.e (which is
 * lost once logcat rotates, unlike Crashlytics' breadcrumb trail attached to the next crash).
 */
@Singleton
class CrashReporter @Inject constructor(
    private val preferences: EvenroPreferences,
) {
    private val crashlytics get() = FirebaseCrashlytics.getInstance()

    /** Call once from Application.onCreate, before other managers start, so early failures are
     * still attributed to the right user. */
    fun start(appScope: CoroutineScope) {
        preferences.currentUserId
            .onEach { crashlytics.setUserId(it ?: "") }
            .launchIn(appScope)
    }

    /** Breadcrumb shown in the trail leading up to the next crash report — cheap, use freely at
     * key state transitions (call started, offer sent, ICE state changed, etc.). */
    fun log(message: String) {
        crashlytics.log(message)
    }

    /** Reports a non-fatal exception immediately, without waiting for a crash. */
    fun recordException(throwable: Throwable, message: String? = null) {
        message?.let { crashlytics.log(it) }
        crashlytics.recordException(throwable)
    }
}
