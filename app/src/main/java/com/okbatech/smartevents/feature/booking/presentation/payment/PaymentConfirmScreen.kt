package com.okbatech.smartevents.feature.booking.presentation.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroButtonStyle
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

/** The "Payment" success/confirmation state — reached after [PaymentRoute] completes a charge. */
@Composable
fun PaymentConfirmRoute(
    onBackToHome: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(88.dp).background(extended.success, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
            Text(
                text = "Payment successful",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                text = "Your booking is confirmed. Your ticket is ready in My Events.",
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
            EvenroButton(
                text = "BACK TO HOME",
                onClick = onBackToHome,
                style = EvenroButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentConfirmScreenPreview() {
    SmartEventsTheme {
        PaymentConfirmRoute(onBackToHome = {})
    }
}
