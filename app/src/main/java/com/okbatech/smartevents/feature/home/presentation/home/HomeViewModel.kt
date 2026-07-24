package com.okbatech.smartevents.feature.home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.core.datastore.EvenroPreferences
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val HomeCategories = listOf("Design", "Art", "Sports", "Music")

data class HomeUiState(
    val userId: String? = null,
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val currentCity: String = "",
    val searchQuery: String = "",
    val selectedCategory: String = HomeCategories.first(),
    val featuredEvents: List<EventSummary> = emptyList(),
    val categoryEvents: List<EventSummary> = emptyList(),
    val favoriteEventIds: Set<String> = emptySet(),
    val justJoinedEventId: String? = null,
    val showAddEventTooltip: Boolean = false,
)

sealed interface HomeEvent {
    data class SearchQueryChanged(val value: String) : HomeEvent
    data class CategorySelected(val value: String) : HomeEvent
    data class ToggleFavorite(val eventId: String) : HomeEvent
    data class JoinEvent(val eventId: String) : HomeEvent
    data object DismissAddEventTooltip : HomeEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val eventRepository: EventRepository,
    private val toggleWishlist: ToggleWishlistUseCase,
    observeWishlistedEventIds: ObserveWishlistedEventIdsUseCase,
    private val joinEvent: JoinEventUseCase,
    private val preferences: EvenroPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val currentUser = observeCurrentUser()

    init {
        currentUser.onEach { user ->
            _uiState.update {
                it.copy(
                    userId = user?.id,
                    userName = user?.name.orEmpty(),
                    userAvatarUrl = user?.avatarUrl,
                    currentCity = user?.city?.takeIf { city -> city.isNotBlank() } ?: "Dhaka, 1202",
                )
            }
        }.launchIn(viewModelScope)

        currentUser.filterNotNull()
            .flatMapLatest { user -> observeWishlistedEventIds(user.id) }
            .onEach { ids -> _uiState.update { it.copy(favoriteEventIds = ids) } }
            .launchIn(viewModelScope)

        eventRepository.observeFeaturedEvents().onEach { events ->
            _uiState.update { it.copy(featuredEvents = events) }
        }.launchIn(viewModelScope)

        combine(eventRepository.observeAllEvents(), _uiState) { events, state -> events to state.selectedCategory }
            .onEach { (events, category) ->
                _uiState.update { it.copy(categoryEvents = events.filter { e -> e.category == category }) }
            }
            .launchIn(viewModelScope)

        preferences.hasSeenAddEventTooltip.onEach { seen ->
            _uiState.update { it.copy(showAddEventTooltip = !seen) }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SearchQueryChanged -> _uiState.update { it.copy(searchQuery = event.value) }
            is HomeEvent.CategorySelected -> _uiState.update { it.copy(selectedCategory = event.value) }
            is HomeEvent.ToggleFavorite -> toggleFavorite(event.eventId)
            is HomeEvent.JoinEvent -> join(event.eventId)
            HomeEvent.DismissAddEventTooltip -> viewModelScope.launch { preferences.markAddEventTooltipSeen() }
        }
    }

    private fun toggleFavorite(eventId: String) {
        val userId = _uiState.value.userId ?: return
        viewModelScope.launch { toggleWishlist(userId, eventId) }
    }

    private fun join(eventId: String) {
        val userId = _uiState.value.userId ?: return
        viewModelScope.launch {
            joinEvent(userId, eventId).onSuccess {
                _uiState.update { it.copy(justJoinedEventId = eventId) }
            }
        }
    }
}
