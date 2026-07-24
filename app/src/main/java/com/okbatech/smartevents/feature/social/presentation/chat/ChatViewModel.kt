package com.okbatech.smartevents.feature.social.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.social.domain.model.ChatMessage
import com.okbatech.smartevents.feature.social.domain.usecase.ObserveMessagesUseCase
import com.okbatech.smartevents.feature.social.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val title: String = "",
    val currentUserId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    observeMessages: ObserveMessagesUseCase,
    private val sendMessage: SendMessageUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<EvenroRoute.Chat>()

    private val _uiState = MutableStateFlow(ChatUiState(title = route.title))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser().onEach { user -> _uiState.update { it.copy(currentUserId = user?.id) } }
            .launchIn(viewModelScope)

        observeMessages(route.threadId).onEach { messages -> _uiState.update { it.copy(messages = messages) } }
            .launchIn(viewModelScope)
    }

    fun onDraftChanged(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun send() {
        val body = _uiState.value.draft.trim()
        if (body.isEmpty()) return
        viewModelScope.launch {
            val userId = _uiState.value.currentUserId ?: return@launch
            sendMessage(route.threadId, userId, body)
            _uiState.update { it.copy(draft = "") }
        }
    }
}
