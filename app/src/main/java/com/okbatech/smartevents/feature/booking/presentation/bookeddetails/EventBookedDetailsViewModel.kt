package com.okbatech.smartevents.feature.booking.presentation.bookeddetails

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

data class EventBookedDetailsUiState(
    val booking: BookingSummary? = null,
)

@HiltViewModel
class EventBookedDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeBookingById: ObserveBookingByIdUseCase,
) : ViewModel() {

    val bookingId: String = savedStateHandle.toRoute<EvenroRoute.EventBookedDetails>().bookingId

    private val _uiState = MutableStateFlow(EventBookedDetailsUiState())
    val uiState: StateFlow<EventBookedDetailsUiState> = _uiState.asStateFlow()

    init {
        observeBookingById(bookingId).onEach { booking -> _uiState.value = EventBookedDetailsUiState(booking) }
            .launchIn(viewModelScope)
    }
}
