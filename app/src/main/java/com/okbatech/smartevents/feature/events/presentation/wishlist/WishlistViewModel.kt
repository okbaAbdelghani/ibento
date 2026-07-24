package com.okbatech.smartevents.feature.events.presentation.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.booking.domain.usecase.JoinEventUseCase
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveWishlistedEventsUseCase
import com.okbatech.smartevents.feature.events.domain.usecase.ToggleWishlistUseCase
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

data class WishlistUiState(
    val userId: String? = null,
    val events: List<EventSummary> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WishlistViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeWishlistedEvents: ObserveWishlistedEventsUseCase,
    private val toggleWishlist: ToggleWishlistUseCase,
    private val joinEvent: JoinEventUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().onEach { user -> _uiState.update { it.copy(userId = user?.id) } }
            .launchIn(viewModelScope)

        observeCurrentUser().filterNotNull()
            .flatMapLatest { user -> observeWishlistedEvents(user.id) }
            .onEach { events -> _uiState.update { it.copy(events = events) } }
            .launchIn(viewModelScope)
    }

    fun removeFromWishlist(eventId: String) {
        val userId = _uiState.value.userId ?: return
        viewModelScope.launch { toggleWishlist(userId, eventId) }
    }

    fun join(eventId: String) {
        val userId = _uiState.value.userId ?: return
        viewModelScope.launch { joinEvent(userId, eventId) }
    }
}
