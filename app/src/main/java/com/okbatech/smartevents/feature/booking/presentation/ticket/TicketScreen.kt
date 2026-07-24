package com.okbatech.smartevents.feature.booking.presentation.ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate

@Composable
fun TicketRoute(
    onClose: () -> Unit,
    viewModel: TicketViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TicketScreen(uiState = uiState, onClose = onClose)
}

@Composable
private fun TicketScreen(uiState: TicketUiState, onClose: () -> Unit) {
    val extended = EvenroTheme.extendedColors
    val booking = uiState.booking

    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            if (booking != null) {
                Text(
                    "Your Ticket",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, Shapes.large)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(booking.event.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${formatEventDate(booking.event.startDateTime)} · ${booking.event.venueName}, ${booking.event.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                    )

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(extended.surfaceMuted, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.QrCode2,
                            contentDescription = null,
                            tint = extended.textPrimary,
                            modifier = Modifier.size(96.dp),
                        )
                    }

                    Text(
                        text = booking.qrCode.take(12).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    Text(
                        "Show this code at the entrance",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .background(extended.softPeach, Shapes.medium)
                            .padding(16.dp),
                    ) {
                        Text(
                            "${booking.ticketCount} ticket${if (booking.ticketCount > 1) "s" else ""} · $${booking.totalPrice.toInt()} USD paid",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TicketScreenPreview() {
    SmartEventsTheme { TicketScreen(uiState = TicketUiState(), onClose = {}) }
}
