package com.okbatech.smartevents.feature.map.presentation.locationpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.SaveLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationPickerUiState(
    val query: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
)

sealed interface LocationPickerEvent {
    data class QueryChanged(val value: String) : LocationPickerEvent
    data object Submit : LocationPickerEvent
}

@HiltViewModel
class LocationPickerViewModel @Inject constructor(
    private val saveLocation: SaveLocationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationPickerUiState())
    val uiState: StateFlow<LocationPickerUiState> = _uiState.asStateFlow()

    fun onEvent(event: LocationPickerEvent) {
        when (event) {
            is LocationPickerEvent.QueryChanged -> _uiState.update { it.copy(query = event.value) }
            LocationPickerEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val city = query.split(",").last().trim()
            saveLocation(city = city, country = "")
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }
    }
}
