package com.okbatech.smartevents.feature.map.presentation.mapview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class MapViewUiState(
    val nearbyEvents: List<EventSummary> = emptyList(),
    val selectedEventId: String? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapViewUiState())
    val uiState: StateFlow<MapViewUiState> = _uiState.asStateFlow()

    init {
        eventRepository.observeAllEvents()
            .onEach { events ->
                _uiState.value = _uiState.value.copy(
                    nearbyEvents = events,
                    selectedEventId = _uiState.value.selectedEventId ?: events.firstOrNull()?.id,
                )
            }
            .launchIn(viewModelScope)
    }

    fun selectEvent(eventId: String) {
        _uiState.value = _uiState.value.copy(selectedEventId = eventId)
    }
}
