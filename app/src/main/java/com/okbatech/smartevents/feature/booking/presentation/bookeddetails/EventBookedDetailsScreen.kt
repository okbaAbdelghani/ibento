package com.okbatech.smartevents.feature.booking.presentation.bookeddetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroButtonStyle
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate

@Composable
fun EventBookedDetailsRoute(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenTicket: (String) -> Unit,
    viewModel: EventBookedDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    EventBookedDetailsScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenChat = { onOpenChat(viewModel.bookingId) },
        onOpenTicket = { onOpenTicket(viewModel.bookingId) },
    )
}

@Composable
private fun EventBookedDetailsScreen(
    uiState: EventBookedDetailsUiState,
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenTicket: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    val booking = uiState.booking

    Scaffold(
        bottomBar = {
            if (booking != null) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    EvenroButton(
                        text = "MESSAGE ORGANISER",
                        style = EvenroButtonStyle.Soft,
                        onClick = onOpenChat,
                        modifier = Modifier.weight(1f).height(52.dp),
                    )
                    EvenroButton(
                        text = "VIEW TICKET",
                        onClick = onOpenTicket,
                        modifier = Modifier.weight(1f).height(52.dp),
                    )
                }
            }
        },
    ) { padding ->
        if (booking == null) return@Scaffold

        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = booking.event.imageUrl,
                    contentDescription = booking.event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.4f),
                )
                IconButton(onClick = onBack, modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = androidx.compose.ui.graphics.Color.White)
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "You're going! 🎉", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = booking.event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = "${formatEventDate(booking.event.startDateTime)} · ${booking.event.venueName}, ${booking.event.city}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.textSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                )

                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .background(extended.surfaceMuted, com.okbatech.smartevents.ui.theme.Shapes.medium)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ConfirmationNumber, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "${booking.ticketCount} ticket${if (booking.ticketCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Text(
                        text = "$${booking.totalPrice.toInt()} USD paid",
                        style = MaterialTheme.typography.titleSmall,
                        color = extended.textSecondary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventBookedDetailsScreenPreview() {
    SmartEventsTheme {
        EventBookedDetailsScreen(uiState = EventBookedDetailsUiState(), onBack = {}, onOpenChat = {}, onOpenTicket = {})
    }
}
