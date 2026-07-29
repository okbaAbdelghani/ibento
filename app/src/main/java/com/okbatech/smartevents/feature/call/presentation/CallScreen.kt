package com.okbatech.smartevents.feature.call.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.core.webrtc.CallState
import kotlinx.coroutines.delay
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@Composable
fun CallRoute(
    otherUserId: String?,
    otherNameFallback: String?,
    video: Boolean,
    isIncoming: Boolean,
    onFinish: () -> Unit,
    viewModel: CallViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        if (!isIncoming && otherUserId != null) {
            viewModel.startOutgoingCallIfNeeded(otherUserId, video)
        }
    }

    val callState by viewModel.callState.collectAsStateWithLifecycle()
    val peerName by viewModel.peerName.collectAsStateWithLifecycle()
    val localVideoTrack by viewModel.localVideoTrack.collectAsStateWithLifecycle()
    val remoteVideoTrack by viewModel.remoteVideoTrack.collectAsStateWithLifecycle()

    LaunchedEffect(callState) {
        if (callState is CallState.Ended) {
            delay(1_200)
            viewModel.onEndedAcknowledged()
            onFinish()
        }
    }

    CallScreen(
        callState = callState,
        peerName = peerName.ifBlank { otherNameFallback ?: otherUserId.orEmpty() },
        eglBaseContext = viewModel.eglBase.eglBaseContext,
        localVideoTrack = localVideoTrack,
        remoteVideoTrack = remoteVideoTrack,
        onAccept = viewModel::acceptCall,
        onDecline = viewModel::declineCall,
        onHangUp = viewModel::hangUp,
        onToggleMute = viewModel::toggleMute,
        onToggleCamera = viewModel::toggleCamera,
        onToggleSpeaker = viewModel::toggleSpeaker,
    )
}

@Composable
private fun CallScreen(
    callState: CallState,
    peerName: String,
    eglBaseContext: EglBase.Context,
    localVideoTrack: VideoTrack?,
    remoteVideoTrack: VideoTrack?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onHangUp: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)),
    ) {
        when (callState) {
            is CallState.Active -> {
                if (callState.video && remoteVideoTrack != null) {
                    VideoRendererView(
                        track = remoteVideoTrack,
                        eglBaseContext = eglBaseContext,
                        mirror = false,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (localVideoTrack != null && callState.isCameraOn) {
                        VideoRendererView(
                            track = localVideoTrack,
                            eglBaseContext = eglBaseContext,
                            mirror = true,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(width = 110.dp, height = 150.dp),
                        )
                    }
                } else {
                    PeerHeader(peerName, subtitle = "In call")
                }
                CallControls(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
                    isMuted = callState.isMuted,
                    isCameraOn = callState.isCameraOn,
                    isSpeakerOn = callState.isSpeakerOn,
                    showVideoToggle = callState.video,
                    onToggleMute = onToggleMute,
                    onToggleCamera = onToggleCamera,
                    onToggleSpeaker = onToggleSpeaker,
                    onHangUp = onHangUp,
                )
            }
            is CallState.Outgoing -> {
                PeerHeader(peerName, subtitle = if (callState.video) "Video calling…" else "Calling…")
                EndCallButton(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp), onClick = onHangUp)
            }
            is CallState.Connecting -> {
                PeerHeader(peerName, subtitle = "Connecting…")
                EndCallButton(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp), onClick = onHangUp)
            }
            is CallState.Incoming -> {
                PeerHeader(peerName, subtitle = if (callState.video) "Incoming video call" else "Incoming voice call")
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    RoundIconButton(icon = Icons.Filled.CallEnd, background = Color(0xFFE53935), onClick = onDecline)
                    RoundIconButton(
                        icon = if (callState.video) Icons.Filled.VideoCall else Icons.Filled.Call,
                        background = Color(0xFF43A047),
                        onClick = onAccept,
                    )
                }
            }
            is CallState.Ended -> {
                PeerHeader(peerName, subtitle = "Call ended")
            }
            CallState.Idle -> Unit
        }
    }
}

@Composable
private fun PeerHeader(peerName: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(peerName, style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
        if (subtitle == "Connecting…") {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
private fun CallControls(
    modifier: Modifier,
    isMuted: Boolean,
    isCameraOn: Boolean,
    isSpeakerOn: Boolean,
    showVideoToggle: Boolean,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onHangUp: () -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        RoundIconButton(
            icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
            background = Color.White.copy(alpha = 0.15f),
            onClick = onToggleMute,
        )
        RoundIconButton(
            icon = if (isSpeakerOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
            background = Color.White.copy(alpha = 0.15f),
            onClick = onToggleSpeaker,
        )
        if (showVideoToggle) {
            RoundIconButton(
                icon = if (isCameraOn) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                background = Color.White.copy(alpha = 0.15f),
                onClick = onToggleCamera,
            )
        }
        RoundIconButton(icon = Icons.Filled.CallEnd, background = Color(0xFFE53935), onClick = onHangUp)
    }
}

@Composable
private fun EndCallButton(modifier: Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        RoundIconButton(icon = Icons.Filled.CallEnd, background = Color(0xFFE53935), onClick = onClick)
    }
}

@Composable
private fun RoundIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, background: Color, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp).background(background, CircleShape),
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
private fun VideoRendererView(
    track: VideoTrack?,
    eglBaseContext: EglBase.Context,
    mirror: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val renderer = remember {
        SurfaceViewRenderer(context).apply {
            init(eglBaseContext, null)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        }
    }

    LaunchedEffect(mirror) { renderer.setMirror(mirror) }

    DisposableEffect(track) {
        track?.addSink(renderer)
        onDispose { track?.removeSink(renderer) }
    }

    DisposableEffect(Unit) {
        onDispose { renderer.release() }
    }

    AndroidView(modifier = modifier, factory = { renderer })
}
