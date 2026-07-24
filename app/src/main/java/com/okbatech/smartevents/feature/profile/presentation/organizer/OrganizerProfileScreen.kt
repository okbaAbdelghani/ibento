package com.okbatech.smartevents.feature.profile.presentation.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroButtonStyle
import com.okbatech.smartevents.core.designsystem.components.EventListRow
import com.okbatech.smartevents.core.designsystem.components.RatingStars
import com.okbatech.smartevents.core.navigation.OrganizerTab
import com.okbatech.smartevents.feature.auth.domain.model.User
import com.okbatech.smartevents.feature.events.domain.model.Review
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate

@Composable
fun OrganizerProfileRoute(
    onBack: () -> Unit,
    onOpenEventDetails: (String) -> Unit,
    onOpenEditProfile: () -> Unit,
    viewModel: OrganizerProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OrganizerProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onSelectTab = viewModel::selectTab,
        onToggleFollow = viewModel::toggleFollow,
        onOpenEventDetails = onOpenEventDetails,
        onOpenEditProfile = onOpenEditProfile,
    )
}

@Composable
private fun OrganizerProfileScreen(
    uiState: OrganizerProfileUiState,
    onBack: () -> Unit,
    onSelectTab: (OrganizerTab) -> Unit,
    onToggleFollow: () -> Unit,
    onOpenEventDetails: (String) -> Unit,
    onOpenEditProfile: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    val organizer = uiState.organizer

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Organizer Profile",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = organizer?.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(88.dp).clip(CircleShape).background(extended.surfaceMuted),
                )
                Text(
                    text = organizer?.name.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 12.dp),
                )
                if (!organizer?.city.isNullOrBlank()) {
                    Text(
                        text = listOfNotNull(organizer?.city, organizer?.country).joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    OrganizerStat(count = organizer?.followerCount ?: 0, label = "Followers")
                    OrganizerStat(count = organizer?.followingCount ?: 0, label = "Following")
                    OrganizerStat(count = uiState.hostedEvents.size, label = "Events")
                }

                if (uiState.isOwnProfile) {
                    EvenroButton(
                        text = "EDIT PROFILE",
                        onClick = onOpenEditProfile,
                        style = EvenroButtonStyle.Soft,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    )
                } else {
                    EvenroButton(
                        text = if (uiState.isFollowing) "FOLLOWING" else "FOLLOW",
                        onClick = onToggleFollow,
                        style = if (uiState.isFollowing) EvenroButtonStyle.Soft else EvenroButtonStyle.Primary,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    )
                }
            }

            TabRow(selectedTabIndex = uiState.selectedTab.ordinal, modifier = Modifier.padding(top = 20.dp)) {
                OrganizerTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        text = { Text(tab.name.uppercase()) },
                    )
                }
            }

            when (uiState.selectedTab) {
                OrganizerTab.About -> AboutTab(organizer)
                OrganizerTab.Events -> EventsTab(uiState.hostedEvents, onOpenEventDetails)
                OrganizerTab.Reviews -> ReviewsTab(uiState.reviews, uiState.averageRating)
            }
        }
    }
}

@Composable
private fun AboutTab(organizer: User?) {
    val extended = EvenroTheme.extendedColors
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = "About", style = MaterialTheme.typography.titleMedium)
        Text(
            text = organizer?.bio?.takeIf { it.isNotBlank() } ?: "This organizer hasn't added a bio yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = extended.textSecondary,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (!organizer?.email.isNullOrBlank()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = extended.divider)
            Text(text = "Contact", style = MaterialTheme.typography.titleMedium)
            Text(
                text = organizer?.email.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun EventsTab(events: List<com.okbatech.smartevents.feature.events.domain.model.EventSummary>, onOpenEventDetails: (String) -> Unit) {
    val extended = EvenroTheme.extendedColors
    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No events hosted yet", style = MaterialTheme.typography.bodyMedium, color = extended.textSecondary)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
    ) {
        items(events, key = { it.id }) { event ->
            EventListRow(
                imageUrl = event.imageUrl,
                title = event.title,
                dateLabel = formatEventDate(event.startDateTime),
                locationLabel = "${event.venueName}, ${event.city}",
                priceLabel = "$${event.priceAmount.toInt()} ${event.currency}",
                onClick = { onOpenEventDetails(event.id) },
                onActionClick = { onOpenEventDetails(event.id) },
                actionLabel = "VIEW",
            )
        }
    }
}

@Composable
private fun ReviewsTab(reviews: List<Review>, averageRating: Float) {
    val extended = EvenroTheme.extendedColors
    if (reviews.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No reviews yet", style = MaterialTheme.typography.bodyMedium, color = extended.textSecondary)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = String.format("%.1f", averageRating), style = MaterialTheme.typography.headlineSmall)
                RatingStars(rating = averageRating, modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "(${reviews.size})",
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        items(reviews, key = { it.id }) { review ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = review.authorAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(extended.surfaceMuted),
                    )
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(text = review.authorName, style = MaterialTheme.typography.titleSmall)
                        RatingStars(rating = review.rating, starSize = 12.dp)
                    }
                    Text(
                        text = formatEventDate(review.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary,
                    )
                }
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.textSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun OrganizerStat(count: Int, label: String) {
    val extended = EvenroTheme.extendedColors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = extended.textSecondary)
    }
}

@Preview(showBackground = true)
@Composable
private fun OrganizerProfileScreenPreview() {
    SmartEventsTheme {
        OrganizerProfileScreen(
            uiState = OrganizerProfileUiState(
                organizer = User(id = "u1", name = "Tamim Ikram", email = "tamim@evenro.app", followerCount = 3583, followingCount = 167),
            ),
            onBack = {},
            onSelectTab = {},
            onToggleFollow = {},
            onOpenEventDetails = {},
            onOpenEditProfile = {},
        )
    }
}
