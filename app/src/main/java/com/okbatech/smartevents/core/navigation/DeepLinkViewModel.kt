package com.okbatech.smartevents.core.navigation

import androidx.lifecycle.ViewModel
import com.okbatech.smartevents.core.datastore.EvenroPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Thin Hilt entry point so a notification-tap deep link can resolve the signed-in user id. */
@HiltViewModel
class DeepLinkViewModel @Inject constructor(
    private val preferences: EvenroPreferences,
) : ViewModel() {
    suspend fun currentUserId(): String? = preferences.currentUserId.first()
}
