package com.okbatech.smartevents.feature.booking.presentation.myevents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.booking.domain.model.BookingSummary
import com.okbatech.smartevents.feature.booking.domain.usecase.ObserveMyBookingsUseCase
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

enum class MyEventsTab { Upcoming, Past }

data class MyEventsUiState(
    val selectedTab: MyEventsTab = MyEventsTab.Upcoming,
    val bookings: List<BookingSummary> = emptyList(),
) {
    val visibleBookings: List<BookingSummary>
        get() = bookings
            .filter { if (selectedTab == MyEventsTab.Upcoming) it.isUpcoming else !it.isUpcoming }
            .sortedBy { it.event.startDateTime }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MyEventsViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeMyBookings: ObserveMyBookingsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyEventsUiState())
    val uiState: StateFlow<MyEventsUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().filterNotNull()
            .flatMapLatest { user -> observeMyBookings(user.id) }
            .onEach { bookings -> _uiState.update { it.copy(bookings = bookings) } }
            .launchIn(viewModelScope)
    }

    fun selectTab(tab: MyEventsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
