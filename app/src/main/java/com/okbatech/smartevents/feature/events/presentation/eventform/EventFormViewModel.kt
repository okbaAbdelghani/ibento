package com.okbatech.smartevents.feature.events.presentation.eventform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.events.domain.usecase.CreateEventUseCase
import com.okbatech.smartevents.feature.events.domain.usecase.EventFormInput
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveEventDetailUseCase
import com.okbatech.smartevents.feature.events.domain.usecase.UpdateEventUseCase
import com.okbatech.smartevents.feature.events.presentation.search.SearchCategories
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val SampleImageUrls = listOf(
    "https://picsum.photos/seed/evenro-new-1/800/600",
    "https://picsum.photos/seed/evenro-new-2/800/600",
    "https://picsum.photos/seed/evenro-new-3/800/600",
    "https://picsum.photos/seed/evenro-new-4/800/600",
)

data class EventFormUiState(
    val isEditMode: Boolean = false,
    val eventId: String? = null,
    val title: String = "",
    val description: String = "",
    val category: String = SearchCategories.first(),
    val imageUrl: String = SampleImageUrls.first(),
    val venueName: String = "",
    val city: String = "",
    val priceAmount: String = "",
    val capacity: String = "100",
    val startDateTime: Long = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7),
    val isSaving: Boolean = false,
    val savedEventId: String? = null,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = title.isNotBlank() && venueName.isNotBlank() && city.isNotBlank() &&
            priceAmount.toDoubleOrNull() != null && capacity.toIntOrNull() != null
}

sealed interface EventFormEvent {
    data class TitleChanged(val value: String) : EventFormEvent
    data class DescriptionChanged(val value: String) : EventFormEvent
    data class CategoryChanged(val value: String) : EventFormEvent
    data class ImageSelected(val value: String) : EventFormEvent
    data class VenueChanged(val value: String) : EventFormEvent
    data class CityChanged(val value: String) : EventFormEvent
    data class PriceChanged(val value: String) : EventFormEvent
    data class CapacityChanged(val value: String) : EventFormEvent
    data class DateChanged(val value: Long) : EventFormEvent
    data object Submit : EventFormEvent
}

val EventFormSampleImageUrls = SampleImageUrls

@HiltViewModel
class EventFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    observeEventDetail: ObserveEventDetailUseCase,
    private val createEvent: CreateEventUseCase,
    private val updateEvent: UpdateEventUseCase,
) : ViewModel() {

    private val editingEventId: String? = savedStateHandle.get<String>("eventId")

    private val _uiState = MutableStateFlow(
        EventFormUiState(isEditMode = editingEventId != null, eventId = editingEventId),
    )
    val uiState: StateFlow<EventFormUiState> = _uiState.asStateFlow()

    init {
        if (editingEventId != null) {
            observeEventDetail(editingEventId).filterNotNull().onEach { detail ->
                _uiState.update {
                    it.copy(
                        title = detail.title,
                        description = detail.description,
                        category = detail.category,
                        imageUrl = detail.imageUrl,
                        venueName = detail.venueName,
                        city = detail.city,
                        priceAmount = detail.priceAmount.toString(),
                        capacity = detail.capacity.toString(),
                        startDateTime = detail.startDateTime,
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onEvent(event: EventFormEvent) {
        when (event) {
            is EventFormEvent.TitleChanged -> _uiState.update { it.copy(title = event.value) }
            is EventFormEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value) }
            is EventFormEvent.CategoryChanged -> _uiState.update { it.copy(category = event.value) }
            is EventFormEvent.ImageSelected -> _uiState.update { it.copy(imageUrl = event.value) }
            is EventFormEvent.VenueChanged -> _uiState.update { it.copy(venueName = event.value) }
            is EventFormEvent.CityChanged -> _uiState.update { it.copy(city = event.value) }
            is EventFormEvent.PriceChanged -> _uiState.update { it.copy(priceAmount = event.value) }
            is EventFormEvent.CapacityChanged -> _uiState.update { it.copy(capacity = event.value) }
            is EventFormEvent.DateChanged -> _uiState.update { it.copy(startDateTime = event.value) }
            EventFormEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        val input = EventFormInput(
            title = state.title,
            description = state.description,
            category = state.category,
            imageUrl = state.imageUrl,
            startDateTime = state.startDateTime,
            endDateTime = state.startDateTime + TimeUnit.HOURS.toMillis(3),
            venueName = state.venueName,
            city = state.city,
            priceAmount = state.priceAmount.toDouble(),
            capacity = state.capacity.toInt(),
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = if (state.isEditMode && state.eventId != null) {
                updateEvent(state.eventId, input).map { state.eventId }
            } else {
                val userId = observeCurrentUser().filterNotNull().first().id
                createEvent(input, userId)
            }
            result
                .onSuccess { id -> _uiState.update { it.copy(isSaving = false, savedEventId = id) } }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.message) } }
        }
    }
}
