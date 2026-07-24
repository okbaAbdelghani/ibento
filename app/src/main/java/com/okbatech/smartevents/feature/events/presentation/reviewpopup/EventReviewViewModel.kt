package com.okbatech.smartevents.feature.events.presentation.reviewpopup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveEventDetailUseCase
import com.okbatech.smartevents.feature.events.domain.usecase.SubmitReviewUseCase
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

data class EventReviewUiState(
    val eventTitle: String = "",
    val organizerId: String? = null,
    val rating: Float = 0f,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
)

sealed interface EventReviewEvent {
    data class RatingChanged(val value: Float) : EventReviewEvent
    data class CommentChanged(val value: String) : EventReviewEvent
    data object Submit : EventReviewEvent
}

@HiltViewModel
class EventReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    observeEventDetail: ObserveEventDetailUseCase,
    private val submitReview: SubmitReviewUseCase,
) : ViewModel() {

    private val eventId = savedStateHandle.toRoute<EvenroRoute.EventReviewPopup>().eventId

    private val _uiState = MutableStateFlow(EventReviewUiState())
    val uiState: StateFlow<EventReviewUiState> = _uiState.asStateFlow()

    init {
        observeEventDetail(eventId).filterNotNull().onEach { detail ->
            _uiState.update { it.copy(eventTitle = detail.title, organizerId = detail.organizerId) }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: EventReviewEvent) {
        when (event) {
            is EventReviewEvent.RatingChanged -> _uiState.update { it.copy(rating = event.value) }
            is EventReviewEvent.CommentChanged -> _uiState.update { it.copy(comment = event.value) }
            EventReviewEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val organizerId = _uiState.value.organizerId ?: return
        if (_uiState.value.rating <= 0f) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val userId = observeCurrentUser().filterNotNull().first().id
            submitReview(organizerId, userId, _uiState.value.rating, _uiState.value.comment)
            _uiState.update { it.copy(isSubmitting = false, submitted = true) }
        }
    }
}
