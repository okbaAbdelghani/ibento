package com.okbatech.smartevents.feature.auth.presentation.resetpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.SendPasswordResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val requestSent: Boolean = false,
)

sealed interface ResetPasswordEvent {
    data class EmailChanged(val value: String) : ResetPasswordEvent
    data object Submit : ResetPasswordEvent
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val sendPasswordReset: SendPasswordResetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onEvent(event: ResetPasswordEvent) {
        when (event) {
            is ResetPasswordEvent.EmailChanged -> _uiState.update { it.copy(email = event.value, errorMessage = null) }
            ResetPasswordEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val email = _uiState.value.email.trim()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            sendPasswordReset(email)
                .onSuccess { _uiState.update { it.copy(isLoading = false, requestSent = true) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }
}
