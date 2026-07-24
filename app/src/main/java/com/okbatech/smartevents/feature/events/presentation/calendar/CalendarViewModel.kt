package com.okbatech.smartevents.feature.events.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val eventDaysInMonth: Set<Int> = emptySet(),
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        eventRepository.observeAllEvents()
            .map { events ->
                events
                    .map { Instant.ofEpochMilli(it.startDateTime).atZone(ZoneId.systemDefault()).toLocalDate() }
                    .filter { YearMonth.from(it) == _uiState.value.month }
                    .map { it.dayOfMonth }
                    .toSet()
            }
            .onEach { days -> _uiState.value = _uiState.value.copy(eventDaysInMonth = days) }
            .launchIn(viewModelScope)
    }
}
