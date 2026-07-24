package com.okbatech.smartevents.feature.booking.presentation.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun PaymentRoute(
    newCard: MockCard?,
    onNewCardConsumed: () -> Unit,
    onBack: () -> Unit,
    onOpenAddCard: () -> Unit,
    onPaid: (String) -> Unit,
    viewModel: PaymentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(newCard) {
        if (newCard != null) {
            viewModel.addCard(newCard)
            onNewCardConsumed()
        }
    }

    LaunchedEffect(uiState.bookingId) {
        uiState.bookingId?.let(onPaid)
    }

    PaymentScreen(
        uiState = uiState,
        onSelectCard = viewModel::selectCard,
        onBack = onBack,
        onOpenAddCard = onOpenAddCard,
        onPay = viewModel::pay,
    )
}

@Composable
private fun PaymentScreen(
    uiState: PaymentUiState,
    onSelectCard: (String) -> Unit,
    onBack: () -> Unit,
    onOpenAddCard: () -> Unit,
    onPay: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold(
        bottomBar = {
            EvenroButton(
                text = if (uiState.isProcessing) "PROCESSING..." else "PAY $${uiState.totalPrice.toInt()} USD",
                onClick = onPay,
                enabled = !uiState.isProcessing && uiState.selectedCardId != null,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Payment",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Text("Payment method", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))

            uiState.cards.forEach { card ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCard(card.id) }
                        .background(extended.surfaceMuted, Shapes.medium)
                        .padding(16.dp)
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "${card.brand} •••• ${card.last4}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 12.dp).weight(1f),
                    )
                    RadioButton(selected = uiState.selectedCardId == card.id, onClick = { onSelectCard(card.id) })
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAddCard)
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    "Add New Card",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentScreenPreview() {
    SmartEventsTheme {
        PaymentScreen(uiState = PaymentUiState(), onSelectCard = {}, onBack = {}, onOpenAddCard = {}, onPay = {})
    }
}
