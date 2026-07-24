package com.okbatech.smartevents.feature.booking.presentation.scancard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
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
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

/**
 * There's no camera/OCR integration behind this build, so "scanning" is a manual
 * fallback entry — the viewfinder below is illustrative only.
 */
@Composable
fun ScanCardRoute(onCardScanned: (String) -> Unit) {
    ScanCardScreen(onCardScanned = onCardScanned)
}

@Composable
private fun ScanCardScreen(onCardScanned: (String) -> Unit) {
    val extended = EvenroTheme.extendedColors
    var manualNumber by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            EvenroButton(
                text = "USE THIS CARD",
                onClick = { onCardScanned(manualNumber) },
                enabled = manualNumber.length >= 12,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            Text("Scan Card", style = MaterialTheme.typography.titleLarge)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(vertical = 24.dp)
                    .background(extended.surfaceMuted, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(48.dp),
                )
            }
            Text(
                "No camera scanning yet — type the card number instead",
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            EvenroTextField(
                value = manualNumber,
                onValueChange = { manualNumber = it.filter(Char::isDigit).take(16) },
                placeholder = "Card number",
                keyboardType = KeyboardType.Number,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanCardScreenPreview() {
    SmartEventsTheme { ScanCardScreen(onCardScanned = {}) }
}
