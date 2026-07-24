package com.okbatech.smartevents.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Wraps the app's single DataStore<Preferences> with typed session/onboarding flags. */
class EvenroPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        val SESSION_TOKEN = stringPreferencesKey("session_token")
        val HAS_ONBOARDED = booleanPreferencesKey("has_onboarded")
        val HAS_SEEDED_DATABASE = booleanPreferencesKey("has_seeded_database")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
        val HAS_SEEN_ADD_EVENT_TOOLTIP = booleanPreferencesKey("has_seen_add_event_tooltip")
    }

    val currentUserId: Flow<String?> = dataStore.data.map { it[Keys.CURRENT_USER_ID] }

    /** The backend JWT for the signed-in user, read synchronously by [com.okbatech.smartevents.core.network.AuthTokenInterceptor]. */
    val sessionToken: Flow<String?> = dataStore.data.map { it[Keys.SESSION_TOKEN] }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { !it[Keys.CURRENT_USER_ID].isNullOrEmpty() }

    val hasOnboarded: Flow<Boolean> = dataStore.data.map { it[Keys.HAS_ONBOARDED] ?: false }

    /** Persists the signed-in user id + JWT together; pass a null [token] only for local-only flows. */
    suspend fun setSession(userId: String?, token: String?, rememberMe: Boolean = true) {
        dataStore.edit { prefs ->
            if (userId == null) {
                prefs.remove(Keys.CURRENT_USER_ID)
            } else {
                prefs[Keys.CURRENT_USER_ID] = userId
            }
            if (token == null) {
                prefs.remove(Keys.SESSION_TOKEN)
            } else {
                prefs[Keys.SESSION_TOKEN] = token
            }
            prefs[Keys.REMEMBER_ME] = rememberMe
        }
    }

    suspend fun setOnboardingComplete() {
        dataStore.edit { it[Keys.HAS_ONBOARDED] = true }
    }

    suspend fun hasSeededDatabase(): Boolean =
        dataStore.data.map { it[Keys.HAS_SEEDED_DATABASE] ?: false }.first()

    suspend fun markDatabaseSeeded() {
        dataStore.edit { it[Keys.HAS_SEEDED_DATABASE] = true }
    }

    val hasSeenAddEventTooltip: Flow<Boolean> = dataStore.data.map { it[Keys.HAS_SEEN_ADD_EVENT_TOOLTIP] ?: false }

    suspend fun markAddEventTooltipSeen() {
        dataStore.edit { it[Keys.HAS_SEEN_ADD_EVENT_TOOLTIP] = true }
    }
}
