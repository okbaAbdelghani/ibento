package com.okbatech.smartevents.feature.events.presentation.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.okbatech.smartevents.core.designsystem.components.EventListRow
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarOpenViewRoute(
    onBack: () -> Unit,
    onOpenEventDetails: (String) -> Unit,
    viewModel: CalendarOpenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarOpenViewScreen(uiState = uiState, onBack = onBack, onOpenEventDetails = onOpenEventDetails)
}

@Composable
private fun CalendarOpenViewScreen(
    uiState: CalendarOpenUiState,
    onBack: () -> Unit,
    onOpenEventDetails: (String) -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = uiState.date.format(DateTimeFormatter.ofPattern("dd MMMM, yyyy")),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            if (uiState.events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No events on this day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary,
                    )
                }
            } else {
                LazyColumn {
                    items(uiState.events, key = { it.id }) { event ->
                        EventListRow(
                            imageUrl = event.imageUrl,
                            title = event.title,
                            dateLabel = formatEventDate(event.startDateTime),
                            locationLabel = "${event.venueName}, ${event.city}",
                            priceLabel = "$${event.priceAmount.toInt()} USD",
                            onClick = { onOpenEventDetails(event.id) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarOpenViewScreenPreview() {
    SmartEventsTheme {
        CalendarOpenViewScreen(uiState = CalendarOpenUiState(date = LocalDate.now()), onBack = {}, onOpenEventDetails = {})
    }
}
