package com.okbatech.smartevents.feature.home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.core.datastore.EvenroPreferences
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.booking.domain.usecase.JoinEventUseCase
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveWishlistedEventIdsUseCase
import com.okbatech.smartevents.feature.events.presentation.filter.AllCategory
import com.okbatech.smartevents.feature.events.presentation.filter.FilterCriteria
import com.okbatech.smartevents.feature.events.presentation.search.SearchCategories
import com.okbatech.smartevents.feature.events.domain.usecase.ToggleWishlistUseCase
import com.okbatech.smartevents.feature.social.domain.usecase.ObserveTotalUnreadCountUseCase
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

val HomeCategories = listOf(AllCategory) + SearchCategories

data class HomeUiState(
    val userId: String? = null,
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val currentCity: String = "",
    val searchQuery: String = "",
    val filter: FilterCriteria = FilterCriteria(),
    val featuredEvents: List<EventSummary> = emptyList(),
    val categoryEvents: List<EventSummary> = emptyList(),
    val favoriteEventIds: Set<String> = emptySet(),
    val justJoinedEventId: String? = null,
    val showAddEventTooltip: Boolean = false,
    val isLoadingEvents: Boolean = false,
    val eventsErrorMessage: String? = null,
    val unreadMessagesCount: Int = 0,
)

sealed interface HomeEvent {
    data class SearchQueryChanged(val value: String) : HomeEvent
    data class CategorySelected(val value: String) : HomeEvent
    data class ToggleFavorite(val eventId: String) : HomeEvent
    data class JoinEvent(val eventId: String) : HomeEvent
    data class FilterApplied(val criteria: FilterCriteria) : HomeEvent
    data object DismissAddEventTooltip : HomeEvent
    data object RetryLoadEvents : HomeEvent
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
    private val observeTotalUnreadCount: ObserveTotalUnreadCountUseCase,
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

        combine(eventRepository.observeAllEvents(), _uiState) { events, state -> events to state }
            .onEach { (events, state) ->
                val filtered = events.filter { e ->
                    (state.filter.category == AllCategory || e.category == state.filter.category) &&
                        e.priceAmount >= state.filter.minPrice &&
                        e.priceAmount <= state.filter.maxPrice
                }
                _uiState.update { it.copy(categoryEvents = filtered) }
            }
            .launchIn(viewModelScope)

        preferences.hasSeenAddEventTooltip.onEach { seen ->
            _uiState.update { it.copy(showAddEventTooltip = !seen) }
        }.launchIn(viewModelScope)

        eventRepository.observeEventsLoadState().onEach { load ->
            _uiState.update { it.copy(isLoadingEvents = load.isLoading, eventsErrorMessage = load.errorMessage) }
        }.launchIn(viewModelScope)

        currentUser.filterNotNull()
            .flatMapLatest { user -> observeTotalUnreadCount(user.id) }
            .onEach { count -> _uiState.update { it.copy(unreadMessagesCount = count) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SearchQueryChanged -> _uiState.update { it.copy(searchQuery = event.value) }
            is HomeEvent.CategorySelected -> _uiState.update { it.copy(filter = it.filter.copy(category = event.value)) }
            is HomeEvent.ToggleFavorite -> toggleFavorite(event.eventId)
            is HomeEvent.JoinEvent -> join(event.eventId)
            is HomeEvent.FilterApplied -> _uiState.update { it.copy(filter = event.criteria) }
            HomeEvent.DismissAddEventTooltip -> viewModelScope.launch { preferences.markAddEventTooltipSeen() }
            HomeEvent.RetryLoadEvents -> viewModelScope.launch { eventRepository.refreshEvents() }
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
