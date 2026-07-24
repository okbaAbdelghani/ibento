package com.okbatech.smartevents.feature.auth.presentation.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = true,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val signedIn: Boolean = false,
)

sealed interface SignInEvent {
    data class EmailChanged(val value: String) : SignInEvent
    data class PasswordChanged(val value: String) : SignInEvent
    data object ToggleRememberMe : SignInEvent
    data object TogglePasswordVisibility : SignInEvent
    data object Submit : SignInEvent
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signIn: SignInUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.EmailChanged -> _uiState.update { it.copy(email = event.value, errorMessage = null) }
            is SignInEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value, errorMessage = null) }
            SignInEvent.ToggleRememberMe -> _uiState.update { it.copy(rememberMe = !it.rememberMe) }
            SignInEvent.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            SignInEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            signIn(state.email.trim(), state.password, state.rememberMe)
                .onSuccess { _uiState.update { it.copy(isLoading = false, signedIn = true) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Sign in failed") }
                }
        }
    }
}
