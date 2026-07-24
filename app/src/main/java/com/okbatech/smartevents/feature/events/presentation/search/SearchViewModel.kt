package com.okbatech.smartevents.feature.events.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.booking.domain.usecase.JoinEventUseCase
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveWishlistedEventIdsUseCase
import com.okbatech.smartevents.feature.events.domain.usecase.ToggleWishlistUseCase
import com.okbatech.smartevents.feature.events.presentation.filter.FilterCriteria
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

val SearchCategories = listOf("Design", "Art", "Sports", "Music")

data class SearchUiState(
    val userId: String? = null,
    val query: String = "",
    val filter: FilterCriteria = FilterCriteria(),
    val results: List<EventSummary> = emptyList(),
    val favoriteEventIds: Set<String> = emptySet(),
)

sealed interface SearchEvent {
    data class QueryChanged(val value: String) : SearchEvent
    data class CategoryToggled(val value: String) : SearchEvent
    data class FilterApplied(val criteria: FilterCriteria) : SearchEvent
    data class ToggleFavorite(val eventId: String) : SearchEvent
    data class Join(val eventId: String) : SearchEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val eventRepository: EventRepository,
    private val toggleWishlist: ToggleWishlistUseCase,
    observeWishlistedEventIds: ObserveWishlistedEventIdsUseCase,
    private val joinEvent: JoinEventUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().onEach { user -> _uiState.update { it.copy(userId = user?.id) } }
            .launchIn(viewModelScope)

        observeCurrentUser().filterNotNull()
            .flatMapLatest { user -> observeWishlistedEventIds(user.id) }
            .onEach { ids -> _uiState.update { it.copy(favoriteEventIds = ids) } }
            .launchIn(viewModelScope)

        combine(eventRepository.observeAllEvents(), _uiState) { events, state ->
            events.filter { event ->
                (state.filter.categories.isEmpty() || event.category in state.filter.categories) &&
                    (state.query.isBlank() || event.title.contains(state.query, ignoreCase = true)) &&
                    event.priceAmount >= state.filter.minPrice &&
                    event.priceAmount <= state.filter.maxPrice
            }
        }.onEach { filtered -> _uiState.update { it.copy(results = filtered) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.QueryChanged -> _uiState.update { it.copy(query = event.value) }
            is SearchEvent.CategoryToggled -> _uiState.update {
                val categories = it.filter.categories
                val newCategories = if (event.value in categories) categories - event.value else categories + event.value
                it.copy(filter = it.filter.copy(categories = newCategories))
            }
            is SearchEvent.FilterApplied -> _uiState.update { it.copy(filter = event.criteria) }
            is SearchEvent.ToggleFavorite -> {
                val userId = _uiState.value.userId ?: return
                viewModelScope.launch { toggleWishlist(userId, event.eventId) }
            }
            is SearchEvent.Join -> {
                val userId = _uiState.value.userId ?: return
                viewModelScope.launch { joinEvent(userId, event.eventId) }
            }
        }
    }
}
