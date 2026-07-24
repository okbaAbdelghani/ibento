package com.okbatech.smartevents.feature.events.presentation.invitefriend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.model.User
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveOtherUsersUseCase
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
import javax.inject.Inject

data class InviteFriendUiState(
    val people: List<User> = emptyList(),
    val invitedUserIds: Set<String> = emptySet(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InviteFriendViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeOtherUsers: ObserveOtherUsersUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InviteFriendUiState())
    val uiState: StateFlow<InviteFriendUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().filterNotNull()
            .flatMapLatest { user -> observeOtherUsers(user.id) }
            .onEach { people -> _uiState.update { it.copy(people = people) } }
            .launchIn(viewModelScope)
    }

    fun invite(userId: String) {
        _uiState.update { it.copy(invitedUserIds = it.invitedUserIds + userId) }
    }
}
