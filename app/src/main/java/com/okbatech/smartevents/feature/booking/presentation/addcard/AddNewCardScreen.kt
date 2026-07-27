package com.okbatech.smartevents.feature.booking.presentation.addcard

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroTextField
import com.okbatech.smartevents.feature.booking.presentation.payment.MockCard
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import java.util.UUID

@Composable
fun AddNewCardRoute(
    scannedNumber: String?,
    onBack: () -> Unit,
    onOpenScanCard: () -> Unit,
    onSaved: (MockCard) -> Unit,
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(scannedNumber) {
        if (scannedNumber != null) cardNumber = scannedNumber
    }

    AddNewCardScreen(
        cardNumber = cardNumber,
        expiry = expiry,
        cvv = cvv,
        onCardNumberChange = { cardNumber = it },
        onExpiryChange = { expiry = it },
        onCvvChange = { cvv = it },
        onBack = onBack,
        onOpenScanCard = onOpenScanCard,
        onSave = {
            val last4 = cardNumber.takeLast(4).ifBlank { "0000" }
            onSaved(MockCard(id = "card-${UUID.randomUUID()}", last4 = last4, brand = "Card"))
        },
    )
}

@Composable
private fun AddNewCardScreen(
    cardNumber: String,
    expiry: String,
    cvv: String,
    onCardNumberChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenScanCard: () -> Unit,
    onSave: () -> Unit,
) {
    val canSave = cardNumber.length >= 12 && expiry.isNotBlank() && cvv.length >= 3

    Scaffold(
        bottomBar = {
            EvenroButton(
                text = "SAVE CARD",
                onClick = onSave,
                enabled = canSave,
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
                    text = "Add New Card",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
                IconButton(onClick = onOpenScanCard, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan card")
                }
            }

            EvenroTextField(
                value = cardNumber,
                onValueChange = { onCardNumberChange(it.filter(Char::isDigit).take(16)) },
                placeholder = "Card number",
                leadingIcon = Icons.Filled.CreditCard,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.padding(top = 16.dp),
            )

            Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EvenroTextField(
                    value = expiry,
                    onValueChange = { onExpiryChange(it.take(5)) },
                    placeholder = "MM/YY",
                    modifier = Modifier.weight(1f),
                )
                EvenroTextField(
                    value = cvv,
                    onValueChange = { onCvvChange(it.filter(Char::isDigit).take(4)) },
                    placeholder = "CVV",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddNewCardScreenPreview() {
    SmartEventsTheme {
        AddNewCardScreen(
            cardNumber = "",
            expiry = "",
            cvv = "",
            onCardNumberChange = {},
            onExpiryChange = {},
            onCvvChange = {},
            onBack = {},
            onOpenScanCard = {},
            onSave = {},
        )
    }
}
