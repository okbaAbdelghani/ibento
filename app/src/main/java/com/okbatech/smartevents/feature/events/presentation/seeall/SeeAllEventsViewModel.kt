package com.okbatech.smartevents.feature.events.presentation.seeall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.booking.domain.usecase.JoinEventUseCase
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveWishlistedEventIdsUseCase
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

data class SeeAllEventsUiState(
    val userId: String? = null,
    val events: List<EventSummary> = emptyList(),
    val favoriteEventIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SeeAllEventsEvent {
    data class ToggleFavorite(val eventId: String) : SeeAllEventsEvent
    data class Join(val eventId: String) : SeeAllEventsEvent
    data object Retry : SeeAllEventsEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SeeAllEventsViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val eventRepository: EventRepository,
    private val toggleWishlist: ToggleWishlistUseCase,
    observeWishlistedEventIds: ObserveWishlistedEventIdsUseCase,
    private val joinEvent: JoinEventUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeeAllEventsUiState())
    val uiState: StateFlow<SeeAllEventsUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().onEach { user -> _uiState.update { it.copy(userId = user?.id) } }
            .launchIn(viewModelScope)

        observeCurrentUser().filterNotNull()
            .flatMapLatest { user -> observeWishlistedEventIds(user.id) }
            .onEach { ids -> _uiState.update { it.copy(favoriteEventIds = ids) } }
            .launchIn(viewModelScope)

        eventRepository.observeAllEvents()
            .onEach { events -> _uiState.update { it.copy(events = events) } }
            .launchIn(viewModelScope)

        eventRepository.observeEventsLoadState()
            .onEach { load -> _uiState.update { it.copy(isLoading = load.isLoading, errorMessage = load.errorMessage) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SeeAllEventsEvent) {
        when (event) {
            is SeeAllEventsEvent.ToggleFavorite -> {
                val userId = _uiState.value.userId ?: return
                viewModelScope.launch { toggleWishlist(userId, event.eventId) }
            }
            is SeeAllEventsEvent.Join -> {
                val userId = _uiState.value.userId ?: return
                viewModelScope.launch { joinEvent(userId, event.eventId) }
            }
            SeeAllEventsEvent.Retry -> viewModelScope.launch { eventRepository.refreshEvents() }
        }
    }
}
