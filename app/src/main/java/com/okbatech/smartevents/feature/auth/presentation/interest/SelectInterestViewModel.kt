package com.okbatech.smartevents.feature.auth.presentation.interest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.SaveInterestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val MaxInterests = 3

val AvailableInterests = listOf("Design", "Music", "Art", "Sports", "Food", "Others")

data class SelectInterestUiState(
    val selected: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val saved: Boolean = false,
) {
    val canSubmit: Boolean get() = selected.isNotEmpty()
}

sealed interface SelectInterestEvent {
    data class ToggleInterest(val interest: String) : SelectInterestEvent
    data object Submit : SelectInterestEvent
}

@HiltViewModel
class SelectInterestViewModel @Inject constructor(
    private val saveInterests: SaveInterestsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectInterestUiState())
    val uiState: StateFlow<SelectInterestUiState> = _uiState.asStateFlow()

    fun onEvent(event: SelectInterestEvent) {
        when (event) {
            is SelectInterestEvent.ToggleInterest -> toggle(event.interest)
            SelectInterestEvent.Submit -> submit()
        }
    }

    private fun toggle(interest: String) {
        _uiState.update { state ->
            val newSelection = when {
                interest in state.selected -> state.selected - interest
                state.selected.size >= MaxInterests -> state.selected
                else -> state.selected + interest
            }
            state.copy(selected = newSelection)
        }
    }

    private fun submit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            saveInterests(_uiState.value.selected.toList())
            _uiState.update { it.copy(isLoading = false, saved = true) }
        }
    }
}
