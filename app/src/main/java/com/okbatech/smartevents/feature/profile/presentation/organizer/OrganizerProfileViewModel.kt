package com.okbatech.smartevents.feature.profile.presentation.organizer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.core.navigation.OrganizerTab
import com.okbatech.smartevents.feature.auth.domain.model.User
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveUserByIdUseCase
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.model.Review
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveEventsByOrganizerUseCase
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveReviewsForOrganizerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OrganizerProfileUiState(
    val viewerUserId: String? = null,
    val organizer: User? = null,
    val hostedEvents: List<EventSummary> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val selectedTab: OrganizerTab = OrganizerTab.About,
    val isFollowing: Boolean = false,
) {
    val isOwnProfile: Boolean get() = viewerUserId != null && viewerUserId == organizer?.id
    val averageRating: Float get() = if (reviews.isEmpty()) 0f else reviews.map { it.rating }.average().toFloat()
}

@HiltViewModel
class OrganizerProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeUserById: ObserveUserByIdUseCase,
    observeEventsByOrganizer: ObserveEventsByOrganizerUseCase,
    observeReviewsForOrganizer: ObserveReviewsForOrganizerUseCase,
) : ViewModel() {

    private val route: EvenroRoute.OrganizerProfile = savedStateHandle.toRoute()
    val organizerId: String = route.userId

    private val _uiState = MutableStateFlow(OrganizerProfileUiState(selectedTab = route.tab))
    val uiState: StateFlow<OrganizerProfileUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().onEach { user -> _uiState.update { it.copy(viewerUserId = user?.id) } }
            .launchIn(viewModelScope)

        observeUserById(organizerId).onEach { organizer -> _uiState.update { it.copy(organizer = organizer) } }
            .launchIn(viewModelScope)

        observeEventsByOrganizer(organizerId).onEach { events -> _uiState.update { it.copy(hostedEvents = events) } }
            .launchIn(viewModelScope)

        observeReviewsForOrganizer(organizerId).onEach { reviews -> _uiState.update { it.copy(reviews = reviews) } }
            .launchIn(viewModelScope)
    }

    fun selectTab(tab: OrganizerTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleFollow() {
        _uiState.update { it.copy(isFollowing = !it.isFollowing) }
    }
}
