package com.okbatech.smartevents.feature.booking.presentation.ticket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.feature.booking.domain.model.BookingSummary
import com.okbatech.smartevents.feature.booking.domain.usecase.ObserveBookingByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class TicketUiState(val booking: BookingSummary? = null)

@HiltViewModel
class TicketViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeBookingById: ObserveBookingByIdUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketUiState())
    val uiState: StateFlow<TicketUiState> = _uiState.asStateFlow()

    init {
        val bookingId = savedStateHandle.toRoute<EvenroRoute.Ticket>().bookingId
        observeBookingById(bookingId).onEach { booking -> _uiState.value = TicketUiState(booking) }
            .launchIn(viewModelScope)
    }
}
