package com.okbatech.smartevents.feature.auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val signedUpEmail: String? = null,
) {
    val canSubmit: Boolean
        get() = fullName.isNotBlank() && email.isNotBlank() &&
            password.length >= 6 && password == confirmPassword
}

sealed interface SignUpEvent {
    data class FullNameChanged(val value: String) : SignUpEvent
    data class EmailChanged(val value: String) : SignUpEvent
    data class PasswordChanged(val value: String) : SignUpEvent
    data class ConfirmPasswordChanged(val value: String) : SignUpEvent
    data object Submit : SignUpEvent
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUp: SignUpUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.FullNameChanged -> _uiState.update { it.copy(fullName = event.value, errorMessage = null) }
            is SignUpEvent.EmailChanged -> _uiState.update { it.copy(email = event.value, errorMessage = null) }
            is SignUpEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value, errorMessage = null) }
            is SignUpEvent.ConfirmPasswordChanged -> _uiState.update { it.copy(confirmPassword = event.value, errorMessage = null) }
            SignUpEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) {
            _uiState.update { it.copy(errorMessage = "Passwords must match and be at least 6 characters") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            signUp(state.fullName.trim(), state.email.trim(), state.password)
                .onSuccess { user -> _uiState.update { it.copy(isLoading = false, signedUpEmail = user.email) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Sign up failed") }
                }
        }
    }
}
