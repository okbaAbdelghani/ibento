package com.okbatech.smartevents.feature.booking.presentation.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.booking.domain.usecase.PurchaseTicketUseCase
import com.okbatech.smartevents.feature.events.domain.model.EventDetail
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveEventDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val AddCardResultKey = "add_card_result"

data class MockCard(val id: String, val last4: String, val brand: String) : java.io.Serializable

data class PaymentUiState(
    val event: EventDetail? = null,
    val ticketCount: Int = 1,
    val cards: List<MockCard> = listOf(MockCard("default", "4242", "Visa")),
    val selectedCardId: String? = "default",
    val isProcessing: Boolean = false,
    val bookingId: String? = null,
    val errorMessage: String? = null,
) {
    val totalPrice: Double get() = (event?.priceAmount ?: 0.0) * ticketCount
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeEventDetail: ObserveEventDetailUseCase,
    private val purchaseTicket: PurchaseTicketUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<EvenroRoute.Payment>()
    val eventId: String = route.eventId

    private val currentUser = observeCurrentUser()

    private val _uiState = MutableStateFlow(PaymentUiState(ticketCount = route.ticketCount))
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        observeEventDetail(eventId).onEach { detail -> _uiState.update { it.copy(event = detail) } }
            .launchIn(viewModelScope)
    }

    fun selectCard(cardId: String) {
        _uiState.update { it.copy(selectedCardId = cardId) }
    }

    fun addCard(card: MockCard) {
        _uiState.update { it.copy(cards = it.cards + card, selectedCardId = card.id) }
    }

    fun pay() {
        viewModelScope.launch {
            val userId = currentUser.filterNotNull().first().id
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            purchaseTicket(userId, eventId, _uiState.value.ticketCount)
                .onSuccess { bookingId -> _uiState.update { it.copy(isProcessing = false, bookingId = bookingId) } }
                .onFailure { error -> _uiState.update { it.copy(isProcessing = false, errorMessage = error.message) } }
        }
    }
}
