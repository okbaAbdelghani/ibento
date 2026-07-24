package com.okbatech.smartevents.feature.profile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.SignOutUseCase
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userId: String? = null,
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val hostedEventCount: Int = 0,
    val isOrganizer: Boolean = false,
    val isSignedOut: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    eventRepository: EventRepository,
    private val signOut: SignOutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val currentUser = observeCurrentUser()

        currentUser.onEach { user ->
            _uiState.update {
                it.copy(
                    userId = user?.id,
                    name = user?.name.orEmpty(),
                    email = user?.email.orEmpty(),
                    avatarUrl = user?.avatarUrl,
                    bio = user?.bio,
                    followerCount = user?.followerCount ?: 0,
                    followingCount = user?.followingCount ?: 0,
                )
            }
        }.launchIn(viewModelScope)

        currentUser.filterNotNull()
            .flatMapLatest { user -> eventRepository.observeEventsByOrganizer(user.id) }
            .onEach { events -> _uiState.update { it.copy(hostedEventCount = events.size, isOrganizer = events.isNotEmpty()) } }
            .launchIn(viewModelScope)
    }

    fun onSignOut() {
        viewModelScope.launch {
            signOut()
            _uiState.update { it.copy(isSignedOut = true) }
        }
    }
}
