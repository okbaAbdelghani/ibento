package com.okbatech.smartevents.feature.social.presentation.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.model.User
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveOtherUsersUseCase
import com.okbatech.smartevents.feature.booking.domain.model.BookingSummary
import com.okbatech.smartevents.feature.booking.domain.usecase.ObserveMyBookingsUseCase
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
import javax.inject.Inject

enum class MessagesTab { Direct, Groups }

data class MessagesUiState(
    val currentUserId: String? = null,
    val selectedTab: MessagesTab = MessagesTab.Direct,
    val people: List<User> = emptyList(),
    val groupBookings: List<BookingSummary> = emptyList(),
) {
    val groups: List<BookingSummary> get() = groupBookings.distinctBy { it.event.id }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MessagesViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeOtherUsers: ObserveOtherUsersUseCase,
    observeMyBookings: ObserveMyBookingsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().onEach { user -> _uiState.update { it.copy(currentUserId = user?.id) } }
            .launchIn(viewModelScope)

        observeCurrentUser().filterNotNull()
            .flatMapLatest { user -> observeOtherUsers(user.id) }
            .onEach { people -> _uiState.update { it.copy(people = people) } }
            .launchIn(viewModelScope)

        observeCurrentUser().filterNotNull()
            .flatMapLatest { user -> observeMyBookings(user.id) }
            .onEach { bookings -> _uiState.update { it.copy(groupBookings = bookings) } }
            .launchIn(viewModelScope)
    }

    fun selectTab(tab: MessagesTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
