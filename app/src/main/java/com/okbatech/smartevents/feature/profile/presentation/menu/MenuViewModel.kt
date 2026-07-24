package com.okbatech.smartevents.feature.profile.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val isSignedOut: Boolean = false,
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val signOut: SignOutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().onEach { user ->
            _uiState.update { it.copy(userName = user?.name.orEmpty(), userAvatarUrl = user?.avatarUrl) }
        }.launchIn(viewModelScope)
    }

    fun onSignOut() {
        viewModelScope.launch {
            signOut()
            _uiState.update { it.copy(isSignedOut = true) }
        }
    }
}
