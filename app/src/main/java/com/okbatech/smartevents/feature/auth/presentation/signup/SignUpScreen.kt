package com.okbatech.smartevents.feature.auth.presentation.signup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroTextField
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

@Composable
fun SignUpRoute(
    onBack: () -> Unit,
    onSignedUp: (email: String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.signedUpEmail) {
        uiState.signedUpEmail?.let(onSignedUp)
    }

    SignUpScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onNavigateToSignIn = onNavigateToSignIn,
    )
}

@Composable
private fun SignUpScreen(
    uiState: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit,
    onBack: () -> Unit,
    onNavigateToSignIn: () -> Unit,
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
                    text = "Sign up",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 16.dp),
                )
            }

            Text(
                text = "Create account and enjoy all services",
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )

            EvenroTextField(
                value = uiState.fullName,
                onValueChange = { onEvent(SignUpEvent.FullNameChanged(it)) },
                placeholder = "Type your full name",
                leadingIcon = Icons.Filled.Person,
            )
            EvenroTextField(
                value = uiState.email,
                onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
                placeholder = "Type your email",
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
                modifier = Modifier.padding(top = 16.dp),
            )
            EvenroTextField(
                value = uiState.password,
                onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
                placeholder = "Type your password",
                leadingIcon = Icons.Filled.Lock,
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(top = 16.dp),
            )
            EvenroTextField(
                value = uiState.confirmPassword,
                onValueChange = { onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
                placeholder = "Type your confirm password",
                leadingIcon = Icons.Filled.Lock,
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            EvenroButton(
                text = if (uiState.isLoading) "SIGNING UP..." else "SIGN UP",
                onClick = { onEvent(SignUpEvent.Submit) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "or continue with",
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.textSecondary,
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onNavigateToSignIn),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    SmartEventsTheme {
        SignUpScreen(uiState = SignUpUiState(), onEvent = {}, onBack = {}, onNavigateToSignIn = {})
    }
}
