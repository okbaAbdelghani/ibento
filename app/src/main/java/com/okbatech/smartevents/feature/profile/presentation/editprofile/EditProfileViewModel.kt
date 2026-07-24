package com.okbatech.smartevents.feature.profile.presentation.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface EditProfileEvent {
    data class NameChanged(val value: String) : EditProfileEvent
    data class PhoneChanged(val value: String) : EditProfileEvent
    data class BioChanged(val value: String) : EditProfileEvent
    data class AvatarUrlChanged(val value: String) : EditProfileEvent
    data object Save : EditProfileEvent
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val updateProfile: UpdateProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().onEach { user ->
            if (user != null) {
                _uiState.update {
                    it.copy(
                        name = user.name,
                        phone = user.phone.orEmpty(),
                        bio = user.bio.orEmpty(),
                        avatarUrl = user.avatarUrl.orEmpty(),
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.NameChanged -> _uiState.update { it.copy(name = event.value) }
            is EditProfileEvent.PhoneChanged -> _uiState.update { it.copy(phone = event.value) }
            is EditProfileEvent.BioChanged -> _uiState.update { it.copy(bio = event.value) }
            is EditProfileEvent.AvatarUrlChanged -> _uiState.update { it.copy(avatarUrl = event.value) }
            EditProfileEvent.Save -> save()
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name can't be empty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            updateProfile(
                name = state.name.trim(),
                phone = state.phone.trim().ifBlank { null },
                bio = state.bio.trim().ifBlank { null },
                avatarUrl = state.avatarUrl.trim().ifBlank { null },
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }
}
