package com.okbatech.smartevents.feature.auth.presentation.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.SignInUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.SignInWithFacebookUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.SignInWithGoogleUseCase
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
    data class GoogleIdTokenReceived(val idToken: String) : SignInEvent
    data class GoogleSignInFailed(val message: String) : SignInEvent
    data class FacebookAccessTokenReceived(val accessToken: String) : SignInEvent
    data class FacebookSignInFailed(val message: String) : SignInEvent
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signIn: SignInUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signInWithFacebook: SignInWithFacebookUseCase,
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
            is SignInEvent.GoogleIdTokenReceived -> submitGoogle(event.idToken)
            is SignInEvent.GoogleSignInFailed -> _uiState.update { it.copy(errorMessage = event.message) }
            is SignInEvent.FacebookAccessTokenReceived -> submitFacebook(event.accessToken)
            is SignInEvent.FacebookSignInFailed -> _uiState.update { it.copy(errorMessage = event.message) }
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

    private fun submitGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            signInWithGoogle(idToken)
                .onSuccess { _uiState.update { it.copy(isLoading = false, signedIn = true) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Google sign-in failed") }
                }
        }
    }

    private fun submitFacebook(accessToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            signInWithFacebook(accessToken)
                .onSuccess { _uiState.update { it.copy(isLoading = false, signedIn = true) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Facebook sign-in failed") }
                }
        }
    }
}
