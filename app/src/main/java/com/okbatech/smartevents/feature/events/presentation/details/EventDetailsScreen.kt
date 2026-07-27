package com.okbatech.smartevents.feature.events.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.okbatech.smartevents.core.designsystem.components.AvatarStack
import com.okbatech.smartevents.core.designsystem.components.EmptyState
import com.okbatech.smartevents.core.designsystem.components.ErrorState
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.IconCircleButton
import com.okbatech.smartevents.core.designsystem.components.LoadingState
import com.okbatech.smartevents.feature.events.domain.model.EventDetail
import com.okbatech.smartevents.feature.social.domain.model.ChatThreads
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate

@Composable
fun EventDetailsRoute(
    onBack: () -> Unit,
    onOpenBuyTicket: (String) -> Unit,
    onOpenInvite: (String) -> Unit,
    onOpenChat: (threadId: String, title: String) -> Unit,
    onOpenShare: (eventId: String, title: String) -> Unit,
    onOpenOrganizerProfile: (String) -> Unit,
    viewModel: EventDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    EventDetailsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onOpenBuyTicket = { onOpenBuyTicket(viewModel.eventId) },
        onOpenInvite = { onOpenInvite(viewModel.eventId) },
        onOpenChat = onOpenChat,
        onOpenShare = { uiState.event?.let { onOpenShare(viewModel.eventId, it.title) } },
        onOpenOrganizerProfile = onOpenOrganizerProfile,
    )
}

@Composable
private fun EventDetailsScreen(
    uiState: EventDetailsUiState,
    onEvent: (EventDetailsEvent) -> Unit,
    onBack: () -> Unit,
    onOpenBuyTicket: () -> Unit,
    onOpenInvite: () -> Unit,
    onOpenChat: (String, String) -> Unit,
    onOpenShare: () -> Unit,
    onOpenOrganizerProfile: (String) -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    val event = uiState.event

    Scaffold(
        bottomBar = {
            if (event != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    IconCircleButton(
                        icon = Icons.Filled.BookmarkBorder,
                        onClick = { onEvent(EventDetailsEvent.ToggleFavorite) },
                        contentDescription = "Save",
                        containerColor = extended.surfaceMuted,
                        size = 52.dp,
                    )
                    EvenroButton(
                        text = "BUY A TICKET",
                        onClick = onOpenBuyTicket,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                    )
                }
            }
        },
    ) { padding ->
        if (event == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    uiState.isLoading -> LoadingState()
                    uiState.errorMessage != null -> ErrorState(message = uiState.errorMessage, onRetry = { onEvent(EventDetailsEvent.Retry) })
                    else -> EmptyState(title = "Event not found", message = "This event may have been removed.")
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.95f),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconCircleButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = onBack,
                        contentDescription = "Back",
                        containerColor = Color.White.copy(alpha = 0.85f),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconCircleButton(
                            icon = Icons.Filled.Share,
                            onClick = onOpenShare,
                            contentDescription = "Share",
                            containerColor = Color.White.copy(alpha = 0.85f),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        IconCircleButton(
                            icon = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            onClick = { onEvent(EventDetailsEvent.ToggleFavorite) },
                            contentDescription = "Toggle favorite",
                            containerColor = Color.White.copy(alpha = 0.85f),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .background(extended.softPeach, Shapes.medium)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "$${event.priceAmount.toInt()} ${event.currency}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "${event.venueName}, ${event.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary,
                        modifier = Modifier.padding(start = 4.dp, end = 16.dp),
                    )
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = formatEventDate(event.startDateTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarStack(
                            avatarUrls = (1..3).map { "https://i.pravatar.cc/150?img=${event.id.hashCode().let { h -> (h + it).mod(70) }}" },
                            avatarSize = 26.dp,
                        )
                        Text(
                            text = "${event.attendeeCount} Members are joined",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Text(
                        text = "VIEW ALL / INVITE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onOpenInvite),
                    )
                }

                if (uiState.organizer != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                            .clickable { onOpenOrganizerProfile(uiState.organizer.id) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = uiState.organizer.avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(extended.surfaceMuted),
                        )
                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(uiState.organizer.name, style = MaterialTheme.typography.titleSmall)
                            Text("Event Organiser", style = MaterialTheme.typography.bodySmall, color = extended.textSecondary)
                        }
                        IconCircleButton(
                            icon = Icons.Filled.ChatBubbleOutline,
                            onClick = {
                                val me = uiState.userId
                                if (me != null) {
                                    onOpenChat(ChatThreads.direct(me, uiState.organizer.id), uiState.organizer.name)
                                }
                            },
                            containerColor = extended.surfaceMuted,
                            size = 40.dp,
                        )
                        IconCircleButton(
                            icon = Icons.Filled.Call,
                            onClick = {},
                            containerColor = extended.surfaceMuted,
                            size = 40.dp,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                )
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.textSecondary,
                    maxLines = if (uiState.isDescriptionExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (uiState.isDescriptionExpanded) "Read Less" else "Read More",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { onEvent(EventDetailsEvent.ToggleDescription) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventDetailsScreenPreview() {
    SmartEventsTheme {
        EventDetailsScreen(
            uiState = EventDetailsUiState(
                event = EventDetail(
                    id = "1",
                    title = "Shere Bangla Concert",
                    description = "Ultricies arcu venenatis in lorem faucibus lobortis at.",
                    category = "Music",
                    imageUrl = "",
                    startDateTime = System.currentTimeMillis(),
                    endDateTime = System.currentTimeMillis(),
                    venueName = "ABC Avenue",
                    city = "Dhaka",
                    priceAmount = 299.0,
                    currency = "USD",
                    organizerId = "u1",
                    capacity = 100,
                    attendeeCount = 15700,
                ),
            ),
            onEvent = {},
            onBack = {},
            onOpenBuyTicket = {},
            onOpenInvite = {},
            onOpenChat = { _, _ -> },
            onOpenShare = {},
            onOpenOrganizerProfile = {},
        )
    }
}
