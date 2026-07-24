package com.okbatech.smartevents.feature.events.presentation.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import androidx.navigation.toRoute
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class CalendarOpenUiState(
    val date: LocalDate = LocalDate.now(),
    val events: List<EventSummary> = emptyList(),
)

@HiltViewModel
class CalendarOpenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    eventRepository: EventRepository,
) : ViewModel() {

    private val date: LocalDate = LocalDate.ofEpochDay(savedStateHandle.toRoute<EvenroRoute.CalendarOpenView>().epochDay)

    private val _uiState = MutableStateFlow(CalendarOpenUiState(date = date))
    val uiState: StateFlow<CalendarOpenUiState> = _uiState.asStateFlow()

    init {
        eventRepository.observeAllEvents()
            .map { events ->
                events.filter {
                    Instant.ofEpochMilli(it.startDateTime).atZone(ZoneId.systemDefault()).toLocalDate() == date
                }
            }
            .onEach { events -> _uiState.value = _uiState.value.copy(events = events) }
            .launchIn(viewModelScope)
    }
}
