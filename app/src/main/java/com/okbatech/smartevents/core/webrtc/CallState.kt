package com.okbatech.smartevents.core.webrtc

sealed class CallState {
    data object Idle : CallState()

    data class Outgoing(
        val callId: String,
        val calleeId: String,
        val video: Boolean,
        val startedAt: Long,
    ) : CallState()

    /** [offerSdp] is null when this state was raised by the FCM wake push (metadata only) and
     * the XMPP offer stanza hasn't landed yet — filled in-place once it arrives. Accepting
     * while null just marks intent; CallManager finishes the accept once the SDP shows up. */
    data class Incoming(
        val callId: String,
        val callerId: String,
        val video: Boolean,
        val offerSdp: String?,
        val receivedAt: Long,
    ) : CallState()

    data class Connecting(val callId: String, val otherUserId: String, val video: Boolean) : CallState()

    data class Active(
        val callId: String,
        val otherUserId: String,
        val video: Boolean,
        val isMuted: Boolean,
        val isCameraOn: Boolean,
        val isSpeakerOn: Boolean,
        val connectedAt: Long,
    ) : CallState()

    data class Ended(val callId: String, val reason: CallEndReason) : CallState()
}
