package com.okbatech.smartevents.feature.auth.presentation.location

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

data class SelectLocationUiState(
    val query: String = "Downey St, San Francisco",
    val isLoading: Boolean = false,
    val saved: Boolean = false,
)

sealed interface SelectLocationEvent {
    data class QueryChanged(val value: String) : SelectLocationEvent
    data object Submit : SelectLocationEvent
}

@HiltViewModel
class SelectLocationViewModel @Inject constructor(
    private val saveLocation: SaveLocationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectLocationUiState())
    val uiState: StateFlow<SelectLocationUiState> = _uiState.asStateFlow()

    fun onEvent(event: SelectLocationEvent) {
        when (event) {
            is SelectLocationEvent.QueryChanged -> _uiState.update { it.copy(query = event.value) }
            SelectLocationEvent.Submit -> submit()
        }
    }

    private fun submit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val parts = _uiState.value.query.split(",").map { it.trim() }
            val city = parts.lastOrNull().orEmpty()
            saveLocation(city = city, country = "")
            _uiState.update { it.copy(isLoading = false, saved = true) }
        }
    }
}
