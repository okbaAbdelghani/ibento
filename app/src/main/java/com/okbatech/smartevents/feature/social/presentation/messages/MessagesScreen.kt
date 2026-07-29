package com.okbatech.smartevents.feature.social.presentation.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.okbatech.smartevents.feature.social.domain.model.ChatThreads
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatBubbleTime

@Composable
fun MessagesRoute(
    onBack: () -> Unit,
    onOpenChat: (threadId: String, title: String, otherUserId: String) -> Unit,
    onOpenGroupChat: (eventId: String) -> Unit,
    viewModel: MessagesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MessagesScreen(
        uiState = uiState,
        onSelectTab = viewModel::selectTab,
        onOpenDirectChat = { otherUserId, otherUserName ->
            val me = uiState.currentUserId
            if (me != null) onOpenChat(ChatThreads.direct(me, otherUserId), otherUserName, otherUserId)
        },
        onOpenGroupChat = onOpenGroupChat,
        onBack = onBack,
    )
}

@Composable
private fun MessagesScreen(
    uiState: MessagesUiState,
    onSelectTab: (MessagesTab) -> Unit,
    onOpenDirectChat: (String, String) -> Unit,
    onOpenGroupChat: (String) -> Unit,
    onBack: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, top = 8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Messages", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp))
            }

            TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                MessagesTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        text = { Text(tab.name.uppercase()) },
                    )
                }
            }

            when (uiState.selectedTab) {
                MessagesTab.Direct -> {
                    if (uiState.conversations.isEmpty()) {
                        EmptyState("No conversations yet")
                    } else {
                        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)) {
                            items(uiState.conversations, key = { it.person.id }) { conversation ->
                                DirectRow(
                                    conversation = conversation,
                                    onClick = { onOpenDirectChat(conversation.person.id, conversation.person.name) },
                                )
                            }
                        }
                    }
                }
                MessagesTab.Groups -> {
                    if (uiState.groups.isEmpty()) {
                        EmptyState("Join an event to start a group chat")
                    } else {
                        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)) {
                            items(uiState.groups, key = { it.event.id }) { booking ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenGroupChat(booking.event.id) }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AsyncImage(
                                        model = booking.event.imageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(48.dp).clip(CircleShape).background(extended.surfaceMuted),
                                    )
                                    Text(
                                        booking.event.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(start = 12.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectRow(conversation: DirectConversation, onClick: () -> Unit) {
    val extended = EvenroTheme.extendedColors
    val person = conversation.person
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = person.avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(48.dp).clip(CircleShape).background(extended.surfaceMuted),
        )
        Text(
            person.name,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 12.dp).weight(1f),
        )
        Column(horizontalAlignment = Alignment.End) {
            if (conversation.lastMessageAt != null) {
                Text(
                    formatBubbleTime(conversation.lastMessageAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (conversation.unreadCount > 0) Color(0xFFFF9800) else extended.textSecondary,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                )
            }
            if (conversation.unreadCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .widthIn(min = 20.dp)
                        .height(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF9800)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    val extended = EvenroTheme.extendedColors
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = extended.textSecondary)
    }
}

@Preview(showBackground = true)
@Composable
private fun MessagesScreenPreview() {
    SmartEventsTheme {
        MessagesScreen(uiState = MessagesUiState(), onSelectTab = {}, onOpenDirectChat = { _, _ -> }, onOpenGroupChat = {}, onBack = {})
    }
}
