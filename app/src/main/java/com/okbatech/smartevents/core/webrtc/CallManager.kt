package com.okbatech.smartevents.core.webrtc

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.okbatech.smartevents.core.crash.CrashReporter
import com.okbatech.smartevents.core.di.IoDispatcher
import com.okbatech.smartevents.core.network.ApiService
import com.okbatech.smartevents.core.network.CallNotifyRequest
import com.okbatech.smartevents.core.xmpp.XmppManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoTrack
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "CallManager"
private const val RING_TIMEOUT_MS = 45_000L
private const val OFFER_RETRY_INTERVAL_MS = 3_000L
private const val STREAM_ID = "evenro-call"

/**
 * Owns the WebRTC peer connection lifecycle for the app's single active call. Reactive-singleton
 * shape mirrors [XmppManager]/[com.okbatech.smartevents.core.push.PushTokenManager] — started
 * once from Application.onCreate, lives for the process lifetime.
 *
 * Signaling rides the existing XMPP connection (see [XmppManager.incomingCallSignals]/
 * sendCallOffer/Answer/IceCandidate/End). Because ejabberd's mod_offline only queues stanzas
 * with a body (a call stanza has none), an offer sent while the callee's socket is down is
 * dropped, not queued — [startCall] retries it every [OFFER_RETRY_INTERVAL_MS] for
 * [RING_TIMEOUT_MS], long enough to cover a cold FCM-triggered process start reconnecting XMPP.
 *
 * Raising incoming-call UI from the background is legally gated on Android 12+ — starting a
 * foreground service from a background process is only allowed for a fixed exemption list, and
 * a live-XMPP-only signal (app backgrounded, no push involved) does not qualify. So this manager
 * only drives the foreground-service/notification directly when the process is already
 * foregrounded ([isAppForegrounded]); otherwise it just updates [callState] and leaves raising
 * the UI to [onIncomingCallPush], called from the FCM service after that high-priority push
 * arrives (which *is* an exemption).
 */
@Singleton
class CallManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val xmppManager: XmppManager,
    private val apiService: ApiService,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val eglBase: EglBase,
    private val crashReporter: CrashReporter,
    private val callRinger: CallRinger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrackRef: VideoTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    // Guards cleanupConnection() against running its teardown twice concurrently — confirmed
    // live that a user-initiated hangUp and an ICE-state-driven endCall (onIceConnectionChange)
    // can race for the same call.
    private val cleanupLock = Any()
    // Keyed by callId: WebRTC's trickle ICE means candidates can legitimately arrive before the
    // offer/answer that establishes call state for them (confirmed live) — buffering only once
    // a call is already known would silently drop those. Bounded implicitly by cleanupConnection
    // clearing it on every call end.
    private val pendingRemoteCandidates = mutableListOf<Pair<String, IceCandidate>>()

    private var ringTimeoutJob: Job? = null
    private var offerRetryJob: Job? = null

    /** Set by [acceptCall] when the user accepts before the XMPP offer's SDP has arrived (push
     * woke us first) — the accept is finished once the matching Offer signal lands. */
    private var pendingAcceptCallId: String? = null

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun start(appScope: CoroutineScope) {
        xmppManager.incomingCallSignals
            .onEach { signal ->
                runCatching { handleSignal(signal) }.onFailure {
                    Log.e(TAG, "handleSignal threw", it)
                    crashReporter.recordException(it, "handleSignal threw for $signal")
                }
            }
            .launchIn(appScope)

        // Ringing is purely a function of callState, not of which entry point raised the call
        // (direct startCall, an XMPP offer, or an FCM push) — driving it from here keeps every
        // path in sync for free instead of needing a startRinging()/stop() call threaded through
        // each one individually.
        _callState.onEach { state ->
            when (state) {
                is CallState.Outgoing -> callRinger.startRingback()
                is CallState.Incoming -> callRinger.startRinging()
                else -> callRinger.stop()
            }
        }.launchIn(appScope)
    }

    // ---- Outgoing ----

    fun startCall(otherUserId: String, video: Boolean) {
        if (_callState.value !is CallState.Idle) {
            Log.w(TAG, "startCall ignored — already in a call")
            return
        }
        val callId = "call-${UUID.randomUUID()}"
        crashReporter.log("startCall $callId to $otherUserId video=$video")
        _callState.value = CallState.Outgoing(callId, otherUserId, video, System.currentTimeMillis())

        scope.launch {
            runCatching {
                val pc = createPeerConnection(otherUserId, callId)
                createLocalTracks(video)
                addLocalTracksToConnection(pc)

                val offer = pc.createOfferSuspend(mediaConstraints(video))
                pc.setLocalDescriptionSuspend(offer)

                runCatching { apiService.notifyCall(CallNotifyRequest(otherUserId, callId, video, "offer")) }
                    .onFailure { Log.w(TAG, "call push notify failed (best-effort)", it) }

                offerRetryJob = scope.launch {
                    while (isActive) {
                        xmppManager.sendCallOffer(otherUserId, callId, video, offer.description)
                        delay(OFFER_RETRY_INTERVAL_MS)
                    }
                }
                ringTimeoutJob = scope.launch {
                    delay(RING_TIMEOUT_MS)
                    if (currentCallId() == callId) {
                        Log.w(TAG, "call $callId timed out waiting for answer")
                        endCall(otherUserId, callId, CallEndReason.TIMEOUT, notifyPeer = true)
                    }
                }
            }.onFailure {
                Log.e(TAG, "startCall failed", it)
                crashReporter.recordException(it, "startCall $callId to $otherUserId failed")
                cleanupConnection()
                _callState.value = CallState.Ended(callId, CallEndReason.ERROR)
            }
        }
    }

    // ---- Incoming ----

    /** Called by EvenroFirebaseMessagingService after verifying the FCM message arrived with
     * high priority — the one legal trigger for raising UI from the background. Also the entry
     * point when this same event races with (or precedes) the XMPP offer stanza. */
    fun onIncomingCallPush(callId: String, callerId: String, callerName: String, video: Boolean) {
        val current = _callState.value
        if (current is CallState.Incoming && current.callId == callId) {
            // Already tracking it (XMPP offer arrived first) — just make sure the ring UI/FGS
            // is up, in case this device only just came out of Doze to receive the push.
            CallForegroundService.ring(context, callId, callerId, callerName, video)
            return
        }
        if (current !is CallState.Idle) {
            scope.launch { xmppManager.sendCallEnd(callerId, callId, CallEndReason.BUSY) }
            return
        }
        _callState.value = CallState.Incoming(callId, callerId, video, offerSdp = null, receivedAt = System.currentTimeMillis())
        CallForegroundService.ring(context, callId, callerId, callerName, video)
    }

    fun acceptCall() {
        val incoming = _callState.value as? CallState.Incoming ?: return
        val sdp = incoming.offerSdp
        if (sdp == null) {
            // Offer hasn't arrived over XMPP yet (push-first race) — proceed automatically once
            // handleSignal(Offer) fills it in.
            pendingAcceptCallId = incoming.callId
            return
        }
        proceedAccept(incoming.callId, incoming.callerId, incoming.video, sdp)
    }

    private fun proceedAccept(callId: String, callerId: String, video: Boolean, offerSdp: String) {
        crashReporter.log("proceedAccept $callId from $callerId video=$video")
        _callState.value = CallState.Connecting(callId, callerId, video)
        scope.launch {
            runCatching {
                val pc = createPeerConnection(callerId, callId)
                createLocalTracks(video)
                addLocalTracksToConnection(pc)

                pc.setRemoteDescriptionSuspend(SessionDescription(SessionDescription.Type.OFFER, offerSdp))
                flushPendingCandidates(pc, callId)

                val answer = pc.createAnswerSuspend(mediaConstraints(video))
                pc.setLocalDescriptionSuspend(answer)
                xmppManager.sendCallAnswer(callerId, callId, answer.description)
            }.onFailure {
                Log.e(TAG, "acceptCall failed", it)
                crashReporter.recordException(it, "acceptCall $callId from $callerId failed")
                cleanupConnection()
                _callState.value = CallState.Ended(callId, CallEndReason.ERROR)
            }
        }
    }

    fun declineCall() {
        val incoming = _callState.value as? CallState.Incoming ?: return
        endCall(incoming.callerId, incoming.callId, CallEndReason.DECLINED, notifyPeer = true)
    }

    /** Called by EvenroFirebaseMessagingService on a "call_cancel" push — the caller hung up
     * before we answered. Only acts if we're still ringing for that exact call. */
    fun onIncomingCallCancelled(callId: String) {
        val incoming = _callState.value as? CallState.Incoming ?: return
        if (incoming.callId != callId) return
        cleanupConnection()
        _callState.value = CallState.Ended(callId, CallEndReason.CANCELLED)
        CallForegroundService.stop(context)
    }

    // ---- In-call controls ----

    fun hangUp() {
        val (otherUserId, callId) = currentPeerAndCallId() ?: return
        endCall(otherUserId, callId, CallEndReason.HANGUP, notifyPeer = true)
    }

    fun toggleMute() {
        val active = _callState.value as? CallState.Active ?: return
        val newMuted = !active.isMuted
        localAudioTrack?.setEnabled(!newMuted)
        _callState.value = active.copy(isMuted = newMuted)
    }

    fun toggleCamera() {
        val active = _callState.value as? CallState.Active ?: return
        val newCameraOn = !active.isCameraOn
        localVideoTrackRef?.setEnabled(newCameraOn)
        _callState.value = active.copy(isCameraOn = newCameraOn)
    }

    fun toggleSpeaker() {
        val active = _callState.value as? CallState.Active ?: return
        val newSpeakerOn = !active.isSpeakerOn
        audioManager.isSpeakerphoneOn = newSpeakerOn
        _callState.value = active.copy(isSpeakerOn = newSpeakerOn)
    }

    // ---- Signal handling ----

    private fun handleSignal(signal: CallSignal) {
        when (signal) {
            is CallSignal.Offer -> handleOffer(signal)
            is CallSignal.Answer -> handleAnswer(signal)
            is CallSignal.IceCandidate -> handleIceCandidate(signal)
            is CallSignal.End -> handleEnd(signal)
        }
    }

    private fun handleOffer(signal: CallSignal.Offer) {
        val current = _callState.value
        when {
            current is CallState.Incoming && current.callId == signal.callId -> {
                if (current.offerSdp == null) {
                    _callState.value = current.copy(offerSdp = signal.sdp)
                    if (pendingAcceptCallId == signal.callId) {
                        pendingAcceptCallId = null
                        proceedAccept(signal.callId, signal.fromUserId, signal.video, signal.sdp)
                    }
                }
                // else: a retried offer for a call we're already tracking — no-op.
            }
            currentCallId() == signal.callId -> {
                // A retried offer for a call we've already progressed past Incoming (e.g.
                // accept was still setting up — creating the peer connection, opening the
                // camera — when another 3s retry landed) — confirmed live this was being
                // misclassified as "busy with a different call" below, which wrongly aborted
                // an in-progress accept. Same call, nothing to do.
            }
            current is CallState.Idle -> {
                _callState.value = CallState.Incoming(signal.callId, signal.fromUserId, signal.video, signal.sdp, System.currentTimeMillis())
                if (isAppForegrounded()) {
                    // A full-screen-intent notification is deliberately suppressed by the OS in
                    // favor of a plain heads-up banner whenever the app is already foregrounded/
                    // the screen is unlocked (confirmed live: CallForegroundService.ring() posted
                    // fine, no crash, but never surfaced anything — that's Android's documented
                    // behavior, not a bug). So when foregrounded, launch CallActivity directly
                    // instead of depending on the notification to do it.
                    launchCallActivity(signal.callId, signal.fromUserId, signal.fromUserId, signal.video)
                    CallForegroundService.ring(context, signal.callId, signal.fromUserId, signal.fromUserId, signal.video)
                }
                // else: leave raising the UI to onIncomingCallPush (the FCM push sent alongside
                // this same offer) — see class doc.
            }
            else -> {
                // Already busy with a different call.
                scope.launch { xmppManager.sendCallEnd(signal.fromUserId, signal.callId, CallEndReason.BUSY) }
            }
        }
    }

    private fun handleAnswer(signal: CallSignal.Answer) {
        val outgoing = _callState.value as? CallState.Outgoing ?: return
        if (outgoing.callId != signal.callId) return
        offerRetryJob?.cancel()
        ringTimeoutJob?.cancel()
        _callState.value = CallState.Connecting(outgoing.callId, outgoing.calleeId, outgoing.video)
        scope.launch {
            runCatching {
                val pc = peerConnection ?: return@launch
                pc.setRemoteDescriptionSuspend(SessionDescription(SessionDescription.Type.ANSWER, signal.sdp))
                flushPendingCandidates(pc, outgoing.callId)
            }.onFailure {
                Log.e(TAG, "applying answer failed", it)
                crashReporter.recordException(it, "applying answer failed for ${outgoing.callId}")
                endCall(outgoing.calleeId, outgoing.callId, CallEndReason.ERROR, notifyPeer = true)
            }
        }
    }

    private fun handleIceCandidate(signal: CallSignal.IceCandidate) {
        val activeCallId = currentCallId()
        // A candidate can legitimately arrive before we've processed the offer that would set
        // activeCallId (trickle ICE races with signaling) — only discard it outright if it's for
        // some OTHER call we're not tracking at all, not just because we're still Idle.
        if (activeCallId != null && activeCallId != signal.callId) return
        val candidate = IceCandidate(signal.sdpMid, signal.sdpMLineIndex, signal.candidate)
        val pc = peerConnection
        if (pc == null || activeCallId == null) {
            pendingRemoteCandidates += signal.callId to candidate
        } else {
            pc.addIceCandidate(candidate)
        }
    }

    private fun handleEnd(signal: CallSignal.End) {
        val (_, activeCallId) = currentPeerAndCallId() ?: return
        if (activeCallId != signal.callId) return
        cleanupConnection()
        _callState.value = CallState.Ended(signal.callId, signal.reason)
        CallForegroundService.stop(context)
    }

    // ---- Peer connection setup ----

    private suspend fun createPeerConnection(otherUserId: String, callId: String): PeerConnection {
        val iceServers = fetchIceServers()
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        val pc = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                scope.launch {
                    xmppManager.sendCallIceCandidate(otherUserId, callId, candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp)
                }
            }

            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
                Log.d(TAG, "ICE connection state: $newState")
                crashReporter.log("ICE connection state ($callId): $newState")
                when (newState) {
                    PeerConnection.IceConnectionState.CONNECTED -> onConnected(otherUserId, callId)
                    PeerConnection.IceConnectionState.FAILED, PeerConnection.IceConnectionState.DISCONNECTED -> {
                        if (currentCallId() == callId) endCall(otherUserId, callId, CallEndReason.ERROR, notifyPeer = true)
                    }
                    else -> Unit
                }
            }

            override fun onTrack(transceiver: RtpTransceiver) {
                val track = transceiver.receiver.track()
                if (track is VideoTrack) _remoteVideoTrack.value = track
            }

            override fun onSignalingChange(newState: PeerConnection.SignalingState) = Unit
            override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) = Unit
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) = Unit
            override fun onAddStream(stream: MediaStream) = Unit
            override fun onRemoveStream(stream: MediaStream) = Unit
            override fun onDataChannel(channel: DataChannel) = Unit
            override fun onRenegotiationNeeded() = Unit
            override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) = Unit
        }) ?: error("createPeerConnection returned null")
        peerConnection = pc
        return pc
    }

    private fun onConnected(otherUserId: String, callId: String) {
        val current = _callState.value
        if (current is CallState.Active || currentCallId() != callId) return
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        val video = localVideoTrackRef != null
        _callState.value = CallState.Active(
            callId = callId,
            otherUserId = otherUserId,
            video = video,
            isMuted = false,
            isCameraOn = video,
            isSpeakerOn = video,
            connectedAt = System.currentTimeMillis(),
        )
        audioManager.isSpeakerphoneOn = video
        CallForegroundService.active(context, callId, otherUserId, otherUserId, video)
    }

    private suspend fun fetchIceServers(): List<PeerConnection.IceServer> {
        val stun = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        val turn = runCatching {
            val creds = apiService.getTurnCredentials()
            creds.urls.map { url ->
                PeerConnection.IceServer.builder(url)
                    .setUsername(creds.username)
                    .setPassword(creds.credential)
                    .createIceServer()
            }
        }.getOrElse {
            Log.e(TAG, "fetchIceServers: TURN credentials failed, falling back to STUN only", it)
            crashReporter.recordException(it, "fetchIceServers: TURN credentials failed, falling back to STUN only")
            emptyList()
        }
        return listOf(stun) + turn
    }

    private fun createLocalTracks(video: Boolean) {
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("evenro-audio-${UUID.randomUUID()}", audioSource)

        if (video) {
            val capturer = createCameraCapturer() ?: return
            val videoSource = peerConnectionFactory.createVideoSource(capturer.isScreencast)
            val helper = SurfaceTextureHelper.create("CallCaptureThread", eglBase.eglBaseContext)
            surfaceTextureHelper = helper
            capturer.initialize(helper, context, videoSource.capturerObserver)
            capturer.startCapture(1280, 720, 30)
            videoCapturer = capturer

            val track = peerConnectionFactory.createVideoTrack("evenro-video-${UUID.randomUUID()}", videoSource)
            localVideoTrackRef = track
            _localVideoTrack.value = track
        }
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        val name = deviceNames.firstOrNull { enumerator.isFrontFacing(it) } ?: deviceNames.firstOrNull()
        return name?.let { enumerator.createCapturer(it, null) }
    }

    private fun addLocalTracksToConnection(pc: PeerConnection) {
        localAudioTrack?.let { pc.addTrack(it, listOf(STREAM_ID)) }
        localVideoTrackRef?.let { pc.addTrack(it, listOf(STREAM_ID)) }
    }

    private fun flushPendingCandidates(pc: PeerConnection, callId: String) {
        val (matching, rest) = pendingRemoteCandidates.partition { it.first == callId }
        matching.forEach { pc.addIceCandidate(it.second) }
        pendingRemoteCandidates.clear()
        pendingRemoteCandidates += rest
    }

    private fun endCall(otherUserId: String, callId: String, reason: CallEndReason, notifyPeer: Boolean) {
        // State flips synchronously (cheap) so CallScreen reacts immediately; the actual teardown
        // below is deferred onto scope's dispatcher — cleanupConnection() ends up blocking on
        // native WebRTC calls (peerConnection.close(), surfaceTextureHelper.dispose()) that wait
        // on WebRTC's own signaling/capture threads, and running that synchronously on whichever
        // thread called endCall() is what deadlocked live: hangUp() (main thread) blocked inside
        // close(), while the FAILED/DISCONNECTED transition close() itself triggers re-entered
        // endCall() from onIceConnectionChange on WebRTC's signaling thread — main thread and
        // signaling thread each waiting on the other. Deferring here, plus the idempotency guard
        // in cleanupConnection() below, closes both ends of that.
        _callState.value = CallState.Ended(callId, reason)
        CallForegroundService.stop(context)
        scope.launch {
            if (notifyPeer) xmppManager.sendCallEnd(otherUserId, callId, reason)
            cleanupConnection()
        }
    }

    private fun cleanupConnection() {
        var pc: PeerConnection? = null
        var capturer: CameraVideoCapturer? = null
        var helper: SurfaceTextureHelper? = null
        var localVideo: VideoTrack? = null
        var localAudio: AudioTrack? = null

        val hasWork = synchronized(cleanupLock) {
            // Already torn down by a concurrent caller (e.g. a hangUp racing an ICE-state-driven
            // endCall for the same call) — nothing left to do.
            if (peerConnection == null && videoCapturer == null && surfaceTextureHelper == null) {
                return@synchronized false
            }
            offerRetryJob?.cancel()
            ringTimeoutJob?.cancel()
            offerRetryJob = null
            ringTimeoutJob = null
            pendingAcceptCallId = null
            pendingRemoteCandidates.clear()

            pc = peerConnection
            peerConnection = null
            capturer = videoCapturer
            videoCapturer = null
            helper = surfaceTextureHelper
            surfaceTextureHelper = null
            localVideo = localVideoTrackRef
            localVideoTrackRef = null
            localAudio = localAudioTrack
            localAudioTrack = null
            _localVideoTrack.value = null
            _remoteVideoTrack.value = null

            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
            true
        }
        if (!hasWork) return

        // Deliberately outside the lock: these are the slow/blocking native calls, and holding
        // the lock across them would just relocate the deadlock instead of fixing it.
        runCatching { capturer?.stopCapture() }
        capturer?.dispose()
        helper?.dispose()
        localVideo?.dispose()
        localAudio?.dispose()
        pc?.close()
        pc?.dispose()
    }

    /** Call once the UI has consumed an [CallState.Ended] (e.g. CallActivity finishing) so a
     * later call starts clean. */
    fun resetToIdle() {
        if (_callState.value is CallState.Ended) _callState.value = CallState.Idle
    }

    private fun currentCallId(): String? = when (val s = _callState.value) {
        is CallState.Outgoing -> s.callId
        is CallState.Incoming -> s.callId
        is CallState.Connecting -> s.callId
        is CallState.Active -> s.callId
        else -> null
    }

    private fun currentPeerAndCallId(): Pair<String, String>? = when (val s = _callState.value) {
        is CallState.Outgoing -> s.calleeId to s.callId
        is CallState.Incoming -> s.callerId to s.callId
        is CallState.Connecting -> s.otherUserId to s.callId
        is CallState.Active -> s.otherUserId to s.callId
        else -> null
    }

    private fun isAppForegrounded(): Boolean =
        ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

    /** Only safe to call when the app is already foregrounded (verified by the caller) — a
     * background-started Service calling this without going through a full-screen-intent
     * notification would be blocked by Android's background-activity-launch restrictions. */
    private fun launchCallActivity(callId: String, otherUserId: String, otherName: String, video: Boolean) {
        val intent = CallForegroundService.callActivityIntent(context, callId, otherUserId, otherName, video, isIncoming = true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun mediaConstraints(video: Boolean): MediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", video.toString()))
    }
}

// ---- Coroutine adapters over WebRTC's callback-based SDP APIs ----

private abstract class SdpObserverAdapter : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription?) = Unit
    override fun onCreateFailure(error: String?) = Unit
    override fun onSetSuccess() = Unit
    override fun onSetFailure(error: String?) = Unit
}

private suspend fun PeerConnection.createOfferSuspend(constraints: MediaConstraints): SessionDescription =
    suspendCancellableCoroutine { cont ->
        createOffer(
            object : SdpObserverAdapter() {
                override fun onCreateSuccess(sdp: SessionDescription?) {
                    if (sdp != null) cont.resume(sdp) else cont.resumeWithException(IllegalStateException("null offer"))
                }
                override fun onCreateFailure(error: String?) {
                    cont.resumeWithException(IllegalStateException("createOffer failed: $error"))
                }
            },
            constraints,
        )
    }

private suspend fun PeerConnection.createAnswerSuspend(constraints: MediaConstraints): SessionDescription =
    suspendCancellableCoroutine { cont ->
        createAnswer(
            object : SdpObserverAdapter() {
                override fun onCreateSuccess(sdp: SessionDescription?) {
                    if (sdp != null) cont.resume(sdp) else cont.resumeWithException(IllegalStateException("null answer"))
                }
                override fun onCreateFailure(error: String?) {
                    cont.resumeWithException(IllegalStateException("createAnswer failed: $error"))
                }
            },
            constraints,
        )
    }

private suspend fun PeerConnection.setLocalDescriptionSuspend(sdp: SessionDescription): Unit =
    suspendCancellableCoroutine { cont ->
        setLocalDescription(
            object : SdpObserverAdapter() {
                override fun onSetSuccess() { cont.resume(Unit) }
                override fun onSetFailure(error: String?) {
                    cont.resumeWithException(IllegalStateException("setLocalDescription failed: $error"))
                }
            },
            sdp,
        )
    }

private suspend fun PeerConnection.setRemoteDescriptionSuspend(sdp: SessionDescription): Unit =
    suspendCancellableCoroutine { cont ->
        setRemoteDescription(
            object : SdpObserverAdapter() {
                override fun onSetSuccess() { cont.resume(Unit) }
                override fun onSetFailure(error: String?) {
                    cont.resumeWithException(IllegalStateException("setRemoteDescription failed: $error"))
                }
            },
            sdp,
        )
    }
