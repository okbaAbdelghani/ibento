package com.okbatech.smartevents.feature.social.presentation.groupdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.feature.booking.domain.usecase.ObserveAttendeeUserIdsUseCase
import com.okbatech.smartevents.feature.events.domain.model.EventDetail
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveEventDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class GroupDetailsUiState(
    val event: EventDetail? = null,
    val memberCount: Int = 0,
)

@HiltViewModel
class GroupDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeEventDetail: ObserveEventDetailUseCase,
    observeAttendeeUserIds: ObserveAttendeeUserIdsUseCase,
) : ViewModel() {

    val eventId: String = savedStateHandle.toRoute<EvenroRoute.GroupDetails>().groupId

    private val _uiState = MutableStateFlow(GroupDetailsUiState())
    val uiState: StateFlow<GroupDetailsUiState> = _uiState.asStateFlow()

    init {
        observeEventDetail(eventId).onEach { event -> _uiState.update { it.copy(event = event) } }
            .launchIn(viewModelScope)

        observeAttendeeUserIds(eventId).map { it.size }
            .onEach { count -> _uiState.update { it.copy(memberCount = count) } }
            .launchIn(viewModelScope)
    }
}
