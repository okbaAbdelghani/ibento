package com.okbatech.smartevents.feature.auth.presentation.resetpassword

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroTextField
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

@Composable
fun ResetPasswordRoute(
    onBack: () -> Unit,
    onRequestSent: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.requestSent) {
        if (uiState.requestSent) onRequestSent()
    }

    ResetPasswordScreen(uiState = uiState, onEvent = viewModel::onEvent, onBack = onBack)
}

@Composable
private fun ResetPasswordScreen(
    uiState: ResetPasswordUiState,
    onEvent: (ResetPasswordEvent) -> Unit,
    onBack: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors

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
                    text = "Reset Password",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 16.dp),
                )
            }

            Text(
                text = "Please enter your email address to request a password reset",
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )

            EvenroTextField(
                value = uiState.email,
                onValueChange = { onEvent(ResetPasswordEvent.EmailChanged(it)) },
                placeholder = "Your email address",
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
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
                text = if (uiState.isLoading) "SENDING..." else "SEND",
                onClick = { onEvent(ResetPasswordEvent.Submit) },
                enabled = !uiState.isLoading && uiState.email.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 24.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResetPasswordScreenPreview() {
    SmartEventsTheme {
        ResetPasswordScreen(uiState = ResetPasswordUiState(), onEvent = {}, onBack = {})
    }
}
