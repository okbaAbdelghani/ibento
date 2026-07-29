package com.okbatech.smartevents.feature.social.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.feature.auth.domain.model.User
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveUserByIdUseCase
import com.okbatech.smartevents.feature.booking.domain.usecase.ObserveBookingByIdUseCase
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveEventDetailUseCase
import com.okbatech.smartevents.feature.social.domain.model.ChatThreads
import com.okbatech.smartevents.feature.social.domain.usecase.MarkThreadSeenUseCase
import com.okbatech.smartevents.feature.social.domain.usecase.ObserveMessagesUseCase
import com.okbatech.smartevents.feature.social.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatInBookedDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    observeBookingById: ObserveBookingByIdUseCase,
    observeEventDetail: ObserveEventDetailUseCase,
    observeUserById: ObserveUserByIdUseCase,
    observeMessages: ObserveMessagesUseCase,
    private val sendMessage: SendMessageUseCase,
    private val markThreadSeen: MarkThreadSeenUseCase,
) : ViewModel() {

    private val bookingId = savedStateHandle.toRoute<EvenroRoute.ChatInBookedDetails>().bookingId

    private var threadId: String? = null

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        val organizer: kotlinx.coroutines.flow.Flow<User> = observeBookingById(bookingId).filterNotNull()
            .flatMapLatest { booking -> observeEventDetail(booking.event.id) }
            .filterNotNull()
            .flatMapLatest { event -> observeUserById(event.organizerId) }
            .filterNotNull()

        combine(observeCurrentUser().filterNotNull(), organizer) { me, org -> me to org }
            .onEach { (me, org) -> _uiState.update { it.copy(currentUserId = me.id, title = org.name) } }
            .flatMapLatest { (me, org) ->
                val thread = ChatThreads.direct(me.id, org.id)
                threadId = thread
                observeMessages(thread)
            }
            .onEach { messages ->
                _uiState.update { it.copy(messages = messages) }
                val myId = _uiState.value.currentUserId
                val thread = threadId
                if (myId != null && thread != null && messages.any { it.senderId != myId }) {
                    viewModelScope.launch { markThreadSeen(thread, myId) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onDraftChanged(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun send() {
        val body = _uiState.value.draft.trim()
        val thread = threadId
        if (body.isEmpty() || thread == null) return
        viewModelScope.launch {
            val userId = _uiState.value.currentUserId ?: return@launch
            sendMessage(thread, userId, body)
            _uiState.update { it.copy(draft = "") }
        }
    }
}
