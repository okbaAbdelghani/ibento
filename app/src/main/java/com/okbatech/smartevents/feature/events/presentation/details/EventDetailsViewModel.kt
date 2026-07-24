package com.okbatech.smartevents.feature.events.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.feature.auth.domain.model.User
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveUserByIdUseCase
import com.okbatech.smartevents.feature.events.domain.model.EventDetail
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveEventDetailUseCase
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

data class EventDetailsUiState(
    val userId: String? = null,
    val event: EventDetail? = null,
    val organizer: User? = null,
    val isFavorite: Boolean = false,
    val isDescriptionExpanded: Boolean = false,
)

sealed interface EventDetailsEvent {
    data object ToggleFavorite : EventDetailsEvent
    data object ToggleDescription : EventDetailsEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeEventDetail: ObserveEventDetailUseCase,
    observeUserById: ObserveUserByIdUseCase,
    observeWishlistedEventIds: ObserveWishlistedEventIdsUseCase,
    private val toggleWishlist: ToggleWishlistUseCase,
) : ViewModel() {

    val eventId: String = savedStateHandle.toRoute<EvenroRoute.EventDetails>().eventId

    private val _uiState = MutableStateFlow(EventDetailsUiState())
    val uiState: StateFlow<EventDetailsUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().onEach { user -> _uiState.update { it.copy(userId = user?.id) } }
            .launchIn(viewModelScope)

        observeEventDetail(eventId).onEach { detail -> _uiState.update { it.copy(event = detail) } }
            .launchIn(viewModelScope)

        observeEventDetail(eventId).filterNotNull()
            .flatMapLatest { detail -> observeUserById(detail.organizerId) }
            .onEach { organizer -> _uiState.update { it.copy(organizer = organizer) } }
            .launchIn(viewModelScope)

        observeCurrentUser().filterNotNull()
            .flatMapLatest { user -> observeWishlistedEventIds(user.id) }
            .onEach { ids -> _uiState.update { it.copy(isFavorite = eventId in ids) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: EventDetailsEvent) {
        when (event) {
            EventDetailsEvent.ToggleFavorite -> {
                val userId = _uiState.value.userId ?: return
                viewModelScope.launch { toggleWishlist(userId, eventId) }
            }
            EventDetailsEvent.ToggleDescription -> _uiState.update { it.copy(isDescriptionExpanded = !it.isDescriptionExpanded) }
        }
    }
}
