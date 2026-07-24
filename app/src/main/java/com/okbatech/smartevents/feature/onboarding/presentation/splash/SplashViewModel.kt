package com.okbatech.smartevents.feature.onboarding.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveOnboardingStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashDestination {
    data object Onboarding : SplashDestination
    data object SignIn : SplashDestination
    data object Home : SplashDestination
}

data class SplashUiState(
    val destination: SplashDestination? = null,
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val observeOnboardingState: ObserveOnboardingStateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        viewModelScope.launch {
            val hasOnboarded = observeOnboardingState.hasOnboarded.first()
            val isLoggedIn = observeOnboardingState.isLoggedIn.first()
            _uiState.value = SplashUiState(
                destination = when {
                    !hasOnboarded -> SplashDestination.Onboarding
                    !isLoggedIn -> SplashDestination.SignIn
                    else -> SplashDestination.Home
                },
            )
        }
    }
}
