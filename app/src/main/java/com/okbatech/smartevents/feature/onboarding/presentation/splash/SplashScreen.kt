package com.okbatech.smartevents.feature.onboarding.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

@Composable
fun SplashRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.destination) {
        when (uiState.destination) {
            SplashDestination.Onboarding -> onNavigateToOnboarding()
            SplashDestination.SignIn -> onNavigateToSignIn()
            SplashDestination.Home -> onNavigateToHome()
            null -> Unit
        }
    }

    SplashScreen()
}

@Composable
private fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "e",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Text(
                text = "Evenro",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .size(28.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SmartEventsTheme { SplashScreen() }
}
