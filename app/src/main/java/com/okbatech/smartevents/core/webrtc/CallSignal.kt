package com.okbatech.smartevents.core.webrtc

/** WebRTC call signaling received over XMPP (see XmppManager's "call"/"evenro:call" stanza
 * extension) — offer/answer/ICE-candidate/end, mirroring the SDP/ICE exchange WebRTC needs. */
sealed class CallSignal {
    abstract val callId: String
    abstract val fromUserId: String

    data class Offer(
        override val callId: String,
        override val fromUserId: String,
        val sdp: String,
        val video: Boolean,
    ) : CallSignal()

    data class Answer(
        override val callId: String,
        override val fromUserId: String,
        val sdp: String,
    ) : CallSignal()

    data class IceCandidate(
        override val callId: String,
        override val fromUserId: String,
        val sdpMid: String,
        val sdpMLineIndex: Int,
        val candidate: String,
    ) : CallSignal()

    data class End(
        override val callId: String,
        override val fromUserId: String,
        val reason: CallEndReason,
    ) : CallSignal()
}

enum class CallEndReason { BUSY, DECLINED, CANCELLED, TIMEOUT, HANGUP, ERROR }
