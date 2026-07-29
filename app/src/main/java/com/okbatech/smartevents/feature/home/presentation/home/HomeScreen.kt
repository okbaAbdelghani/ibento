package com.okbatech.smartevents.feature.home.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.okbatech.smartevents.core.designsystem.components.CategoryChip
import com.okbatech.smartevents.core.designsystem.components.EmptyState
import com.okbatech.smartevents.core.designsystem.components.ErrorState
import com.okbatech.smartevents.core.designsystem.components.EventCard
import com.okbatech.smartevents.core.designsystem.components.EvenroBottomNavBar
import com.okbatech.smartevents.core.designsystem.components.EvenroNavDestination
import com.okbatech.smartevents.core.designsystem.components.EvenroSearchBar
import com.okbatech.smartevents.core.designsystem.components.LoadingState
import com.okbatech.smartevents.core.designsystem.components.SectionHeader
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate

@Composable
fun HomeRoute(
    onOpenEventDetails: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenFilter: () -> Unit,
    onOpenSeeAll: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onOpenAddEvent: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMessages: () -> Unit,
    onNavigate: (EvenroNavDestination) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onOpenEventDetails = onOpenEventDetails,
        onOpenSearch = onOpenSearch,
        onOpenFilter = onOpenFilter,
        onOpenSeeAll = onOpenSeeAll,
        onOpenLocationPicker = onOpenLocationPicker,
        onOpenAddEvent = onOpenAddEvent,
        onOpenNotifications = onOpenNotifications,
        onOpenMessages = onOpenMessages,
        onNavigate = onNavigate,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onOpenEventDetails: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenFilter: () -> Unit,
    onOpenSeeAll: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onOpenAddEvent: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMessages: () -> Unit,
    onNavigate: (EvenroNavDestination) -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.justJoinedEventId) {
        if (uiState.justJoinedEventId != null) {
            snackbarHostState.showSnackbar("You're going! Check My Events for your ticket.")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (uiState.showAddEventTooltip) {
                    com.okbatech.smartevents.core.designsystem.components.TooltipOverlay(
                        message = "Tap here to create your first event",
                        onDismiss = { onEvent(HomeEvent.DismissAddEventTooltip) },
                        modifier = Modifier.width(220.dp).padding(bottom = 8.dp),
                    )
                }
                FloatingActionButton(
                    onClick = {
                        onEvent(HomeEvent.DismissAddEventTooltip)
                        onOpenAddEvent()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add event", tint = androidx.compose.ui.graphics.Color.White)
                }
            }
        },
        bottomBar = {
            EvenroBottomNavBar(
                selected = EvenroNavDestination.Home,
                onSelect = onNavigate,
            )
        },
    ) { padding ->
        // isLoadingEvents doubles as the refresh indicator here — refreshEvents() (triggered by
        // RetryLoadEvents below) flips it true/false around the same load this screen already
        // reacts to, so a manual pull and the existing initial-load/retry flow share one flag.
        PullToRefreshBox(
            isRefreshing = uiState.isLoadingEvents,
            onRefresh = { onEvent(HomeEvent.RetryLoadEvents) },
            modifier = Modifier.padding(padding).fillMaxSize(),
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = uiState.userAvatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(extended.surfaceMuted),
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("Hi Welcome 👋", style = MaterialTheme.typography.bodySmall, color = extended.textSecondary)
                            Text(uiState.userName.ifBlank { "Guest" }, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            IconButton(onClick = onOpenMessages) {
                                Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "Messages")
                            }
                            if (uiState.unreadMessagesCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-6).dp, y = 6.dp)
                                        .size(10.dp)
                                        .background(androidx.compose.ui.graphics.Color(0xFFFF9800), CircleShape),
                                )
                            }
                        }
                        IconButton(onClick = onOpenNotifications) {
                            Icon(Icons.Filled.NotificationsNone, contentDescription = "Notifications")
                        }
                    }
                }
            }

            item {
                EvenroSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(HomeEvent.SearchQueryChanged(it)) },
                    onFilterClick = onOpenFilter,
                    onFocused = onOpenSearch,
                )
            }

            val noEventsYet = uiState.featuredEvents.isEmpty() && uiState.categoryEvents.isEmpty()

            if (uiState.isLoadingEvents && noEventsYet) {
                item { LoadingState(modifier = Modifier.fillMaxWidth()) }
            } else if (uiState.eventsErrorMessage != null && noEventsYet) {
                item {
                    ErrorState(
                        message = uiState.eventsErrorMessage,
                        onRetry = { onEvent(HomeEvent.RetryLoadEvents) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                item {
                    SectionHeader(title = "Popular Events 🔥", onViewAllClick = onOpenSeeAll)
                }

                item {
                    if (uiState.featuredEvents.isEmpty()) {
                        EmptyState(title = "No featured events yet", modifier = Modifier.fillMaxWidth())
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(uiState.featuredEvents, key = { it.id }) { event ->
                                EventCard(
                                    imageUrl = event.imageUrl,
                                    title = event.title,
                                    dateLabel = formatEventDate(event.startDateTime),
                                    locationLabel = "${event.venueName}, ${event.city}",
                                    attendeeAvatarUrls = emptyList(),
                                    isFavorite = event.id in uiState.favoriteEventIds,
                                    onFavoriteClick = { onEvent(HomeEvent.ToggleFavorite(event.id)) },
                                    onClick = { onOpenEventDetails(event.id) },
                                    priceLabel = "$${event.priceAmount.toInt()} USD",
                                    onActionClick = { onEvent(HomeEvent.JoinEvent(event.id)) },
                                    modifier = Modifier.width(260.dp),
                                )
                            }
                        }
                    }
                }

                item {
                    SectionHeader(title = "Choose By Category ✨")
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HomeCategories.forEach { category ->
                            CategoryChip(
                                label = category,
                                selected = category == uiState.filter.category,
                                onClick = { onEvent(HomeEvent.CategorySelected(category)) },
                            )
                        }
                    }
                }

                if (uiState.categoryEvents.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No events in ${uiState.filter.category}",
                            message = "Try a different category.",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    items(uiState.categoryEvents, key = { it.id }) { event ->
                        EventCard(
                            imageUrl = event.imageUrl,
                            title = event.title,
                            dateLabel = formatEventDate(event.startDateTime),
                            locationLabel = "${event.venueName}, ${event.city}",
                            attendeeAvatarUrls = emptyList(),
                            isFavorite = event.id in uiState.favoriteEventIds,
                            onFavoriteClick = { onEvent(HomeEvent.ToggleFavorite(event.id)) },
                            onClick = { onOpenEventDetails(event.id) },
                            priceLabel = "$${event.priceAmount.toInt()} USD",
                            onActionClick = { onOpenEventDetails(event.id) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SmartEventsTheme {
        HomeScreen(
            uiState = HomeUiState(
                userName = "MD Rafi Islam",
                currentCity = "Dhaka, 1202",
                featuredEvents = listOf(
                    EventSummary(
                        id = "1",
                        title = "International Band Music Concert",
                        category = "Design",
                        imageUrl = "",
                        startDateTime = System.currentTimeMillis(),
                        venueName = "ABC Avenue",
                        city = "Dhaka",
                        priceAmount = 299.0,
                        currency = "USD",
                        attendeeCount = 15700,
                    ),
                ),
            ),
            onEvent = {},
            onOpenLocationPicker = {},
            onOpenEventDetails = {},
            onOpenSearch = {},
            onOpenFilter = {},
            onOpenSeeAll = {},
            onOpenAddEvent = {},
            onOpenNotifications = {},
            onOpenMessages = {},
            onNavigate = {},
        )
    }
}
