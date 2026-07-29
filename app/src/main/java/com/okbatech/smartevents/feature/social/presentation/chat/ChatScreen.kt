package com.okbatech.smartevents.feature.social.presentation.chat

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.core.designsystem.components.EvenroTextField
import com.okbatech.smartevents.core.webrtc.CallForegroundService
import com.okbatech.smartevents.feature.social.domain.model.ChatMessage
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatBubbleTime

@Composable
fun ChatRoute(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ChatContent(
        uiState = uiState,
        onDraftChanged = viewModel::onDraftChanged,
        onSend = viewModel::send,
        onBack = onBack,
        onStartCall = { video ->
            context.startActivity(
                CallForegroundService.callActivityIntent(
                    context = context,
                    callId = "",
                    otherUserId = uiState.otherUserId,
                    otherName = uiState.title,
                    video = video,
                    isIncoming = false,
                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
            )
        },
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ChatContent(
    uiState: ChatUiState,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    onStartCall: (video: Boolean) -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold(
        // Header lives in Scaffold's topBar slot (not the content Column) specifically so it
        // stays pinned in place when the keyboard opens — content between topBar and bottomBar
        // is what Scaffold resizes/repositions for the IME, the top/bottom bars themselves don't
        // move.
        topBar = {
            Row(
                // The old content-Column placement got its top status-bar clearance for free
                // from Scaffold's content padding; the topBar slot doesn't apply that
                // automatically (edge-to-edge draws behind system bars), so it needs its own
                // statusBarsPadding() or it renders underneath the clock/battery icons.
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .weight(1f)
                        .let { m -> if (onTitleClick != null) m.clickable(onClick = onTitleClick) else m },
                ) {
                    Text(
                        uiState.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    val subtitle = if (uiState.isPeerTyping) "typing…" else uiState.presenceText
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isPeerTyping) MaterialTheme.colorScheme.primary else extended.textSecondary,
                        )
                    }
                }
                IconButton(onClick = { onStartCall(false) }) {
                    Icon(Icons.Filled.Call, contentDescription = "Voice call")
                }
                IconButton(onClick = { onStartCall(true) }) {
                    Icon(Icons.Filled.Videocam, contentDescription = "Video call")
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().imePadding().windowInsetsPadding(WindowInsets.navigationBars).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EvenroTextField(
                    value = uiState.draft,
                    onValueChange = onDraftChanged,
                    placeholder = "Type a message...",
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onSend,
                    modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = androidx.compose.ui.graphics.Color.White)
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Say hello 👋",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary,
                    )
                }
            } else {
                val listState = rememberLazyListState()
                // Messages append at the bottom and the input bar sits below the list, so
                // without this a new message (sent or received) can land past the visible
                // viewport with no indication it arrived — looks indistinguishable from the
                // message never having been delivered. Also re-scrolls as the IME inset changes:
                // opening the keyboard shrinks the list's available height (see imePadding() on
                // the input bar below) over the course of its show/hide animation, not in one
                // jump — keying off a single before/after boolean fired the scroll before the
                // resize had finished, landing at a stale position. Tracking the actual inset
                // value re-fires on every step of that animation, so the last call (once it
                // settles) always reflects the final, fully-resized viewport.
                val imeBottomInset = WindowInsets.ime.getBottom(LocalDensity.current)
                LaunchedEffect(uiState.messages.size, imeBottomInset) {
                    if (uiState.messages.isNotEmpty()) listState.scrollToItem(uiState.messages.lastIndex)
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(message = message, isMine = message.senderId == uiState.currentUserId)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, isMine: Boolean) {
    val extended = EvenroTheme.extendedColors
    val onBubbleColor = if (isMine) androidx.compose.ui.graphics.Color.White else extended.textPrimary
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .background(
                    if (isMine) MaterialTheme.colorScheme.primary else extended.surfaceMuted,
                    Shapes.medium,
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Column {
                Text(
                    message.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onBubbleColor,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatBubbleTime(message.sentAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = onBubbleColor.copy(alpha = 0.7f),
                    )
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.deliveredAt != null) Icons.Filled.DoneAll else Icons.Filled.Check,
                            contentDescription = if (message.readAt != null) "Read" else if (message.deliveredAt != null) "Delivered" else "Sent",
                            tint = if (message.readAt != null) androidx.compose.ui.graphics.Color(0xFF4FC3F7) else onBubbleColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    SmartEventsTheme {
        ChatContent(uiState = ChatUiState(title = "Tamim Ikram"), onDraftChanged = {}, onSend = {}, onBack = {})
    }
}
