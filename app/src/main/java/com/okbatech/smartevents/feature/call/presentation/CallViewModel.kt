package com.okbatech.smartevents.feature.call.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.core.webrtc.CallManager
import com.okbatech.smartevents.core.webrtc.CallState
import com.okbatech.smartevents.feature.auth.domain.usecase.ObserveUserByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import org.webrtc.EglBase
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CallViewModel @Inject constructor(
    private val callManager: CallManager,
    val eglBase: EglBase,
    observeUserById: ObserveUserByIdUseCase,
) : ViewModel() {

    val callState: StateFlow<CallState> = callManager.callState
    val localVideoTrack = callManager.localVideoTrack
    val remoteVideoTrack = callManager.remoteVideoTrack

    val peerName: StateFlow<String> = callState
        .mapNotNull { it.peerId() }
        .distinctUntilChanged()
        .flatMapLatest { id -> observeUserById(id).map { user -> user?.name ?: id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private var hasStartedOutgoing = false

    /** Called once by CallActivity on first launch for an outgoing call — guarded so process
     * recreation (e.g. rotation) doesn't start a second call. */
    fun startOutgoingCallIfNeeded(otherUserId: String, video: Boolean) {
        if (hasStartedOutgoing) return
        if (callState.value !is CallState.Idle) return
        hasStartedOutgoing = true
        callManager.startCall(otherUserId, video)
    }

    fun acceptCall() = callManager.acceptCall()
    fun declineCall() = callManager.declineCall()
    fun hangUp() = callManager.hangUp()
    fun toggleMute() = callManager.toggleMute()
    fun toggleCamera() = callManager.toggleCamera()
    fun toggleSpeaker() = callManager.toggleSpeaker()

    fun onEndedAcknowledged() {
        callManager.resetToIdle()
    }
}

private fun CallState.peerId(): String? = when (this) {
    is CallState.Outgoing -> calleeId
    is CallState.Incoming -> callerId
    is CallState.Connecting -> otherUserId
    is CallState.Active -> otherUserId
    else -> null
}
