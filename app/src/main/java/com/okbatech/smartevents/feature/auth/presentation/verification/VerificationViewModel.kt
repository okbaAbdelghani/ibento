package com.okbatech.smartevents.feature.auth.presentation.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.VerifyOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerificationUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val verified: Boolean = false,
) {
    val codeDigits: List<String> get() = (0 until 4).map { code.getOrNull(it)?.toString() ?: "" }
}

sealed interface VerificationEvent {
    data class CodeChanged(val value: String) : VerificationEvent
    data object Submit : VerificationEvent
}

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verifyOtp: VerifyOtpUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    fun onEvent(event: VerificationEvent) {
        when (event) {
            is VerificationEvent.CodeChanged ->
                _uiState.update { it.copy(code = event.value.take(4), errorMessage = null) }

            VerificationEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val code = _uiState.value.code
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            verifyOtp(code)
                .onSuccess { _uiState.update { it.copy(isLoading = false, verified = true) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }
}
