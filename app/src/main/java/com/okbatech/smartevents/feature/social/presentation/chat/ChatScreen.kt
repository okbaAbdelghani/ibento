package com.okbatech.smartevents.feature.social.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.core.designsystem.components.EvenroTextField
import com.okbatech.smartevents.feature.social.domain.model.ChatMessage
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

@Composable
fun ChatRoute(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatContent(
        uiState = uiState,
        onDraftChanged = viewModel::onDraftChanged,
        onSend = viewModel::send,
        onBack = onBack,
    )
}

@Composable
fun ChatContent(
    uiState: ChatUiState,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    onTitleClick: (() -> Unit)? = null,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    uiState.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .let { m -> if (onTitleClick != null) m.clickable(onClick = onTitleClick) else m },
                )
            }

            if (uiState.messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Say hello 👋",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary,
                    )
                }
            } else {
                LazyColumn(
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
            Text(
                message.body,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMine) androidx.compose.ui.graphics.Color.White else extended.textPrimary,
            )
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
