package com.okbatech.smartevents.feature.social.presentation.groupmembers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.feature.auth.domain.model.User
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveUsersByIdsUseCase
import com.okbatech.smartevents.feature.booking.domain.usecase.ObserveAttendeeUserIdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class GroupMembersUiState(val members: List<User> = emptyList())

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GroupMembersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeAttendeeUserIds: ObserveAttendeeUserIdsUseCase,
    observeUsersByIds: ObserveUsersByIdsUseCase,
) : ViewModel() {

    private val eventId = savedStateHandle.toRoute<EvenroRoute.GroupMembers>().groupId

    private val _uiState = MutableStateFlow(GroupMembersUiState())
    val uiState: StateFlow<GroupMembersUiState> = _uiState.asStateFlow()

    init {
        observeAttendeeUserIds(eventId)
            .flatMapLatest { ids -> if (ids.isEmpty()) kotlinx.coroutines.flow.flowOf(emptyList()) else observeUsersByIds(ids) }
            .onEach { members -> _uiState.update { it.copy(members = members) } }
            .launchIn(viewModelScope)
    }
}
