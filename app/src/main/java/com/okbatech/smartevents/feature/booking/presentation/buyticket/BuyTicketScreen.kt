package com.okbatech.smartevents.feature.booking.presentation.buyticket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

@Composable
fun BuyTicketRoute(
    onBack: () -> Unit,
    onContinue: (eventId: String, ticketCount: Int) -> Unit,
    viewModel: BuyTicketViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BuyTicketScreen(
        uiState = uiState,
        onIncrement = viewModel::increment,
        onDecrement = viewModel::decrement,
        onBack = onBack,
        onContinue = { onContinue(viewModel.eventId, uiState.ticketCount) },
    )
}

@Composable
private fun BuyTicketScreen(
    uiState: BuyTicketUiState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    val event = uiState.event

    Scaffold(
        bottomBar = {
            EvenroButton(
                text = "CONTINUE",
                onClick = onContinue,
                enabled = event != null,
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(16.dp).height(52.dp),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Buy Ticket",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            if (event != null) {
                Text(event.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                Text(
                    "${event.venueName}, ${event.city}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.textSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                        .background(extended.surfaceMuted, Shapes.medium)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Number of tickets", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "$${event.priceAmount.toInt()} ${event.currency} each",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = uiState.ticketCount.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        IconButton(
                            onClick = onIncrement,
                            modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase", tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Total", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$${uiState.totalPrice.toInt()} ${event.currency}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BuyTicketScreenPreview() {
    SmartEventsTheme {
        BuyTicketScreen(uiState = BuyTicketUiState(), onIncrement = {}, onDecrement = {}, onBack = {}, onContinue = {})
    }
}
