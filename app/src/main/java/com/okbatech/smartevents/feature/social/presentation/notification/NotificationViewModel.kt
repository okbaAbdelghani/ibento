package com.okbatech.smartevents.feature.social.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.social.domain.model.AppNotification
import com.okbatech.smartevents.feature.social.domain.usecase.MarkNotificationReadUseCase
import com.okbatech.smartevents.feature.social.domain.usecase.ObserveNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(val notifications: List<AppNotification> = emptyList())

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeNotifications: ObserveNotificationsUseCase,
    private val markNotificationRead: MarkNotificationReadUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().filterNotNull()
            .flatMapLatest { user -> observeNotifications(user.id) }
            .onEach { notifications -> _uiState.update { it.copy(notifications = notifications) } }
            .launchIn(viewModelScope)
    }

    fun onNotificationClicked(id: String) {
        viewModelScope.launch { markNotificationRead(id) }
    }
}
