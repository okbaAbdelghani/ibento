package com.okbatech.smartevents.feature.booking.presentation.myevents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.core.designsystem.components.EventCard
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate

@Composable
fun MyEventsRoute(
    onOpenEventDetails: (String) -> Unit,
    onOpenBookedDetails: (String) -> Unit,
    viewModel: MyEventsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MyEventsScreen(
        uiState = uiState,
        onSelectTab = viewModel::selectTab,
        onOpenEventDetails = onOpenEventDetails,
        onOpenBookedDetails = onOpenBookedDetails,
    )
}

@Composable
private fun MyEventsScreen(
    uiState: MyEventsUiState,
    onSelectTab: (MyEventsTab) -> Unit,
    onOpenEventDetails: (String) -> Unit,
    onOpenBookedDetails: (String) -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "My Events",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
            )

            TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                MyEventsTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        text = { Text(tab.name.uppercase()) },
                    )
                }
            }

            if (uiState.visibleBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (uiState.selectedTab == MyEventsTab.Upcoming) {
                            "No upcoming events yet — join one from Home!"
                        } else {
                            "No past events yet"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
                ) {
                    items(uiState.visibleBookings, key = { it.bookingId }) { booking ->
                        EventCard(
                            imageUrl = booking.event.imageUrl,
                            title = booking.event.title,
                            dateLabel = formatEventDate(booking.event.startDateTime),
                            locationLabel = "${booking.event.venueName}, ${booking.event.city}",
                            attendeeAvatarUrls = emptyList(),
                            isFavorite = false,
                            onFavoriteClick = {},
                            onClick = { onOpenEventDetails(booking.event.id) },
                            priceLabel = "${booking.ticketCount}x ticket",
                            actionLabel = "VIEW",
                            onActionClick = { onOpenBookedDetails(booking.bookingId) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyEventsScreenPreview() {
    SmartEventsTheme {
        MyEventsScreen(uiState = MyEventsUiState(), onSelectTab = {}, onOpenEventDetails = {}, onOpenBookedDetails = {})
    }
}
