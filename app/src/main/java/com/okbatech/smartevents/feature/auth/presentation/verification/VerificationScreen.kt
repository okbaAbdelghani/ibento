package com.okbatech.smartevents.feature.auth.presentation.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import kotlinx.coroutines.delay

@Composable
fun VerificationRoute(
    contact: String,
    onBack: () -> Unit,
    onVerified: () -> Unit,
    viewModel: VerificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.verified) {
        if (uiState.verified) onVerified()
    }

    VerificationScreen(
        contact = contact,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
    )
}

@Composable
private fun VerificationScreen(
    contact: String,
    uiState: VerificationUiState,
    onEvent: (VerificationEvent) -> Unit,
    onBack: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    var secondsLeft by remember { mutableIntStateOf(60) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Verification",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 16.dp),
                )
            }

            Column(modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)) {
                Text("We've send you the verification code on", style = MaterialTheme.typography.bodyMedium, color = extended.textSecondary)
                Text(contact, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
            }

            OtpInput(
                code = uiState.code,
                onCodeChange = { onEvent(VerificationEvent.CodeChanged(it)) },
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            EvenroButton(
                text = if (uiState.isLoading) "VERIFYING..." else "CONTINUE",
                onClick = { onEvent(VerificationEvent.Submit) },
                enabled = !uiState.isLoading && uiState.code.length == 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 24.dp),
            )

            Text(
                text = if (secondsLeft > 0) "Re-send code in 0:%02d".format(secondsLeft) else "Re-send code",
                style = MaterialTheme.typography.bodyMedium,
                color = if (secondsLeft > 0) MaterialTheme.colorScheme.primary else extended.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun OtpInput(code: String, onCodeChange: (String) -> Unit) {
    Box {
        BasicTextField(
            value = code,
            onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) onCodeChange(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) { index ->
                        val digit = code.getOrNull(index)?.toString() ?: ""
                        val isFocusedSlot = index == code.length
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.surface, Shapes.medium)
                                .border(
                                    width = if (isFocusedSlot) 2.dp else 1.dp,
                                    color = if (isFocusedSlot) MaterialTheme.colorScheme.primary else EvenroTheme.extendedColors.divider,
                                    shape = Shapes.medium,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = digit, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VerificationScreenPreview() {
    SmartEventsTheme {
        VerificationScreen(
            contact = "+1 6358 9248 5789",
            uiState = VerificationUiState(code = "47"),
            onEvent = {},
            onBack = {},
        )
    }
}
