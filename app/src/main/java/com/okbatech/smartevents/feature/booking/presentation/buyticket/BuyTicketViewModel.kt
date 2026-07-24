package com.okbatech.smartevents.feature.booking.presentation.buyticket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.feature.events.domain.model.EventDetail
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveEventDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class BuyTicketUiState(
    val event: EventDetail? = null,
    val ticketCount: Int = 1,
) {
    val totalPrice: Double get() = (event?.priceAmount ?: 0.0) * ticketCount
}

@HiltViewModel
class BuyTicketViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeEventDetail: ObserveEventDetailUseCase,
) : ViewModel() {

    val eventId: String = savedStateHandle.toRoute<EvenroRoute.BuyTicket>().eventId

    private val _uiState = MutableStateFlow(BuyTicketUiState())
    val uiState: StateFlow<BuyTicketUiState> = _uiState.asStateFlow()

    init {
        observeEventDetail(eventId).onEach { detail -> _uiState.update { it.copy(event = detail) } }
            .launchIn(viewModelScope)
    }

    fun increment() {
        _uiState.update { it.copy(ticketCount = (it.ticketCount + 1).coerceAtMost(it.event?.capacity ?: 10)) }
    }

    fun decrement() {
        _uiState.update { it.copy(ticketCount = (it.ticketCount - 1).coerceAtLeast(1)) }
    }
}
