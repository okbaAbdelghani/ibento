package com.okbatech.smartevents.feature.map.presentation.mapview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.core.designsystem.components.EventListRow
import com.okbatech.smartevents.core.designsystem.components.EvenroBottomNavBar
import com.okbatech.smartevents.core.designsystem.components.EvenroNavDestination
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate
import kotlin.math.absoluteValue

@Composable
fun MapViewRoute(
    onOpenEventDetails: (String) -> Unit,
    onNavigate: (EvenroNavDestination) -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MapViewScreen(
        uiState = uiState,
        onSelectEvent = viewModel::selectEvent,
        onOpenEventDetails = onOpenEventDetails,
        onNavigate = onNavigate,
    )
}

/**
 * A stylized map — event pins are laid out deterministically from the event id rather than
 * real coordinates, since there's no geocoding/Maps SDK behind this build yet.
 */
@Composable
private fun MapViewScreen(
    uiState: MapViewUiState,
    onSelectEvent: (String) -> Unit,
    onOpenEventDetails: (String) -> Unit,
    onNavigate: (EvenroNavDestination) -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    val selectedEvent = uiState.nearbyEvents.firstOrNull { it.id == uiState.selectedEventId }

    Scaffold(
        bottomBar = { EvenroBottomNavBar(selected = EvenroNavDestination.Map, onSelect = onNavigate) },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(extended.surfaceMuted),
        ) {
            uiState.nearbyEvents.forEach { event ->
                val (xFraction, yFraction) = pinPosition(event.id)
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = event.title,
                    tint = if (event.id == uiState.selectedEventId) MaterialTheme.colorScheme.primary else extended.textTertiary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (xFraction * 300).dp,
                            y = (yFraction * 500).dp,
                        )
                        .size(32.dp)
                        .clickable { onSelectEvent(event.id) },
                )
            }

            if (selectedEvent != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, Shapes.large)
                        .padding(12.dp),
                ) {
                    EventListRow(
                        imageUrl = selectedEvent.imageUrl,
                        title = selectedEvent.title,
                        dateLabel = formatEventDate(selectedEvent.startDateTime),
                        locationLabel = "${selectedEvent.venueName}, ${selectedEvent.city}",
                        priceLabel = "$${selectedEvent.priceAmount.toInt()} USD",
                        onClick = { onOpenEventDetails(selectedEvent.id) },
                    )
                }
            }
        }
    }
}

private fun pinPosition(eventId: String): Pair<Float, Float> {
    val hash = eventId.hashCode().absoluteValue
    val x = (hash % 100) / 100f
    val y = ((hash / 100) % 100) / 100f
    return x to y
}

@Preview(showBackground = true)
@Composable
private fun MapViewScreenPreview() {
    SmartEventsTheme {
        MapViewScreen(
            uiState = MapViewUiState(
                nearbyEvents = listOf(
                    EventSummary("1", "Concert", "Music", "", System.currentTimeMillis(), "Venue", "Dhaka", 20.0, "USD", 100),
                ),
                selectedEventId = "1",
            ),
            onSelectEvent = {},
            onOpenEventDetails = {},
            onNavigate = {},
        )
    }
}
