package com.okbatech.smartevents.feature.social.presentation.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.model.User
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveOtherUsersUseCase
import com.okbatech.smartevents.feature.booking.domain.model.BookingSummary
import com.okbatech.smartevents.feature.booking.domain.usecase.ObserveMyBookingsUseCase
import com.okbatech.smartevents.feature.social.domain.model.ChatThreads
import com.okbatech.smartevents.feature.social.domain.usecase.ObserveLastMessageUseCase
import com.okbatech.smartevents.feature.social.domain.usecase.ObserveThreadUnreadCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class MessagesTab { Direct, Groups }

data class DirectConversation(
    val person: User,
    val lastMessageAt: Long? = null,
    val unreadCount: Int = 0,
)

data class MessagesUiState(
    val currentUserId: String? = null,
    val selectedTab: MessagesTab = MessagesTab.Direct,
    val conversations: List<DirectConversation> = emptyList(),
    val groupBookings: List<BookingSummary> = emptyList(),
) {
    val groups: List<BookingSummary> get() = groupBookings.distinctBy { it.event.id }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MessagesViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeOtherUsers: ObserveOtherUsersUseCase,
    observeMyBookings: ObserveMyBookingsUseCase,
    private val observeLastMessage: ObserveLastMessageUseCase,
    private val observeThreadUnreadCount: ObserveThreadUnreadCountUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().onEach { user -> _uiState.update { it.copy(currentUserId = user?.id) } }
            .launchIn(viewModelScope)

        observeCurrentUser().filterNotNull()
            .flatMapLatest { user ->
                observeOtherUsers(user.id).flatMapLatest { people -> observeConversations(user.id, people) }
            }
            .onEach { conversations -> _uiState.update { it.copy(conversations = conversations) } }
            .launchIn(viewModelScope)

        observeCurrentUser().filterNotNull()
            .flatMapLatest { user -> observeMyBookings(user.id) }
            .onEach { bookings -> _uiState.update { it.copy(groupBookings = bookings) } }
            .launchIn(viewModelScope)
    }

    // Combines each person's last-message-time + unread-count into one row, sorted most-recent
    // conversation first (people with no messages yet sink to the bottom) — mirrors WhatsApp.
    private fun observeConversations(myId: String, people: List<User>) =
        if (people.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(
                people.map { person ->
                    val threadId = ChatThreads.direct(myId, person.id)
                    combine(
                        observeLastMessage(threadId),
                        observeThreadUnreadCount(threadId, myId),
                    ) { last, unread -> DirectConversation(person, last?.sentAt, unread) }
                },
            ) { rows -> rows.sortedByDescending { it.lastMessageAt ?: Long.MIN_VALUE } }
        }

    fun selectTab(tab: MessagesTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
