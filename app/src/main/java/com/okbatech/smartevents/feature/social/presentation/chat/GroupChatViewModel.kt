package com.okbatech.smartevents.feature.social.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.events.domain.usecase.ObserveEventDetailUseCase
import com.okbatech.smartevents.feature.social.domain.model.ChatMessage
import com.okbatech.smartevents.feature.social.domain.model.ChatThreads
import com.okbatech.smartevents.feature.social.domain.usecase.MarkThreadSeenUseCase
import com.okbatech.smartevents.feature.social.domain.usecase.ObserveMessagesUseCase
import com.okbatech.smartevents.feature.social.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    observeEventDetail: ObserveEventDetailUseCase,
    observeMessages: ObserveMessagesUseCase,
    private val sendMessage: SendMessageUseCase,
    private val markThreadSeen: MarkThreadSeenUseCase,
) : ViewModel() {

    val eventId: String = savedStateHandle.toRoute<EvenroRoute.GroupChat>().groupId
    private val threadId = ChatThreads.group(eventId)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeEventDetail(eventId).filterNotNull().onEach { event ->
            _uiState.update { it.copy(title = "${event.title} · Group") }
        }.launchIn(viewModelScope)

        // Combined so currentUserId arriving (async, not guaranteed before the first messages
        // emission) re-runs the seen logic on its own instead of waiting on the next message —
        // see ChatViewModel's identical fix for why that made the unread bubble only clear once
        // the user sent a reply.
        combine(observeCurrentUser(), observeMessages(threadId)) { user, messages -> user to messages }
            .onEach { (user, messages) ->
                _uiState.update { it.copy(currentUserId = user?.id, messages = messages) }
                if (user != null) maybeMarkThreadSeen(messages, user.id)
            }
            .launchIn(viewModelScope)
    }

    private fun maybeMarkThreadSeen(messages: List<ChatMessage>, myId: String) {
        if (messages.none { it.senderId != myId }) return
        viewModelScope.launch { markThreadSeen(threadId, myId) }
    }

    fun onDraftChanged(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun send() {
        val body = _uiState.value.draft.trim()
        if (body.isEmpty()) return
        viewModelScope.launch {
            val userId = _uiState.value.currentUserId ?: return@launch
            sendMessage(threadId, userId, body)
            _uiState.update { it.copy(draft = "") }
        }
    }
}
