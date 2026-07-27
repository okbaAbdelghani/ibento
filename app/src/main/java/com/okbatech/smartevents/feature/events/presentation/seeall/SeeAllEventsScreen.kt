package com.okbatech.smartevents.feature.events.presentation.seeall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.okbatech.smartevents.core.designsystem.components.EmptyState
import com.okbatech.smartevents.core.designsystem.components.ErrorState
import com.okbatech.smartevents.core.designsystem.components.EventCard
import com.okbatech.smartevents.core.designsystem.components.LoadingState
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate

@Composable
fun SeeAllEventsRoute(
    onBack: () -> Unit,
    onOpenEventDetails: (String) -> Unit,
    viewModel: SeeAllEventsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SeeAllEventsScreen(uiState = uiState, onEvent = viewModel::onEvent, onBack = onBack, onOpenEventDetails = onOpenEventDetails)
}

@Composable
private fun SeeAllEventsScreen(
    uiState: SeeAllEventsUiState,
    onEvent: (SeeAllEventsEvent) -> Unit,
    onBack: () -> Unit,
    onOpenEventDetails: (String) -> Unit,
) {
    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "All Events",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
                when {
                    uiState.isLoading && uiState.events.isEmpty() -> {
                        item { LoadingState(modifier = Modifier.fillMaxWidth()) }
                    }
                    uiState.errorMessage != null && uiState.events.isEmpty() -> {
                        item {
                            ErrorState(
                                message = uiState.errorMessage,
                                onRetry = { onEvent(SeeAllEventsEvent.Retry) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    uiState.events.isEmpty() -> {
                        item { EmptyState(title = "No events yet", modifier = Modifier.fillMaxWidth()) }
                    }
                    else -> {
                        items(uiState.events, key = { it.id }) { event ->
                            EventCard(
                                imageUrl = event.imageUrl,
                                title = event.title,
                                dateLabel = formatEventDate(event.startDateTime),
                                locationLabel = "${event.venueName}, ${event.city}",
                                attendeeAvatarUrls = emptyList(),
                                isFavorite = event.id in uiState.favoriteEventIds,
                                onFavoriteClick = { onEvent(SeeAllEventsEvent.ToggleFavorite(event.id)) },
                                onClick = { onOpenEventDetails(event.id) },
                                priceLabel = "$${event.priceAmount.toInt()} USD",
                                onActionClick = { onEvent(SeeAllEventsEvent.Join(event.id)) },
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
private fun SeeAllEventsScreenPreview() {
    SmartEventsTheme {
        SeeAllEventsScreen(uiState = SeeAllEventsUiState(), onEvent = {}, onBack = {}, onOpenEventDetails = {})
    }
}
