package com.okbatech.smartevents.feature.social.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.navigation.EvenroRoute
import com.okbatech.smartevents.core.push.ActiveChatTracker
import com.okbatech.smartevents.core.xmpp.XmppManager
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveUserByIdUseCase
import com.okbatech.smartevents.feature.social.domain.model.ChatMessage
import com.okbatech.smartevents.feature.social.domain.usecase.MarkThreadSeenUseCase
import com.okbatech.smartevents.feature.social.domain.usecase.ObserveMessagesUseCase
import com.okbatech.smartevents.feature.social.domain.usecase.SendMessageUseCase
import com.okbatech.smartevents.util.formatRelativeTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PRESENCE_POLL_INTERVAL_MS = 15_000L
private const val ONLINE_THRESHOLD_MS = 30_000L
private const val TYPING_PAUSE_DELAY_MS = 3_000L

data class ChatUiState(
    val title: String = "",
    val otherUserId: String = "",
    val currentUserId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val presenceText: String? = null,
    val isPeerTyping: Boolean = false,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    observeMessages: ObserveMessagesUseCase,
    private val observeUserById: ObserveUserByIdUseCase,
    private val sendMessage: SendMessageUseCase,
    private val markThreadSeen: MarkThreadSeenUseCase,
    private val xmppManager: XmppManager,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<EvenroRoute.Chat>()

    private val _uiState = MutableStateFlow(ChatUiState(title = route.title, otherUserId = route.otherUserId))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var typingPauseJob: Job? = null
    private var lastReadMarkerSentAt = 0L

    init {
        // Lets an incoming push notification for this exact thread suppress itself — see
        // EvenroFirebaseMessagingService.
        ActiveChatTracker.openThreadId = route.threadId

        observeCurrentUser().onEach { user -> _uiState.update { it.copy(currentUserId = user?.id) } }
            .launchIn(viewModelScope)

        observeMessages(route.threadId).onEach { messages ->
            _uiState.update { it.copy(messages = messages) }
            maybeSendReadMarker(messages)
            maybeMarkThreadSeen(messages)
        }.launchIn(viewModelScope)

        xmppManager.typingByUser.onEach { typingByUser ->
            _uiState.update { it.copy(isPeerTyping = typingByUser[route.otherUserId] == true) }
        }.launchIn(viewModelScope)

        pollPeerPresence()
    }

    override fun onCleared() {
        super.onCleared()
        if (ActiveChatTracker.openThreadId == route.threadId) ActiveChatTracker.openThreadId = null
        typingPauseJob?.cancel()
        viewModelScope.launch { xmppManager.sendTypingState(route.otherUserId, isTyping = false) }
    }

    fun onDraftChanged(value: String) {
        _uiState.update { it.copy(draft = value) }
        sendTypingState(isTyping = value.isNotBlank())
    }

    fun send() {
        val body = _uiState.value.draft.trim()
        if (body.isEmpty()) return
        typingPauseJob?.cancel()
        sendTypingState(isTyping = false)
        viewModelScope.launch {
            val userId = _uiState.value.currentUserId ?: return@launch
            sendMessage(route.threadId, userId, body)
            _uiState.update { it.copy(draft = "") }
        }
    }

    private fun sendTypingState(isTyping: Boolean) {
        typingPauseJob?.cancel()
        viewModelScope.launch { xmppManager.sendTypingState(route.otherUserId, isTyping) }
        if (isTyping) {
            // No further keystrokes for a while implies the user stopped typing — tell the peer.
            typingPauseJob = viewModelScope.launch {
                delay(TYPING_PAUSE_DELAY_MS)
                xmppManager.sendTypingState(route.otherUserId, isTyping = false)
            }
        }
    }

    private fun maybeSendReadMarker(messages: List<ChatMessage>) {
        val latestIncoming = messages.lastOrNull { it.senderId == route.otherUserId } ?: return
        if (latestIncoming.sentAt <= lastReadMarkerSentAt) return
        lastReadMarkerSentAt = latestIncoming.sentAt
        viewModelScope.launch {
            xmppManager.sendReadMarker(route.otherUserId, route.threadId, latestIncoming.sentAt)
        }
    }

    // Opening the thread is what "reading" the message means locally — clears its unread
    // badge/count regardless of whether the peer ever confirms a read receipt back.
    private fun maybeMarkThreadSeen(messages: List<ChatMessage>) {
        val myId = _uiState.value.currentUserId ?: return
        if (messages.none { it.senderId != myId }) return
        viewModelScope.launch { markThreadSeen(route.threadId, myId) }
    }

    private fun pollPeerPresence() {
        viewModelScope.launch {
            while (isActive) {
                val lastSeenAt = observeUserById(route.otherUserId).firstOrNull()?.lastSeenAt
                _uiState.update { it.copy(presenceText = presenceTextFor(lastSeenAt)) }
                delay(PRESENCE_POLL_INTERVAL_MS)
            }
        }
    }

    private fun presenceTextFor(lastSeenAt: Long?): String? {
        if (lastSeenAt == null) return null
        val isOnline = System.currentTimeMillis() - lastSeenAt < ONLINE_THRESHOLD_MS
        return if (isOnline) "Online" else "Last seen ${formatRelativeTime(lastSeenAt)}"
    }
}
