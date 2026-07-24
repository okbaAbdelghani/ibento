package com.okbatech.smartevents.feature.auth.presentation.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.okbatech.smartevents.core.designsystem.components.EvenroTextField
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

@Composable
fun SelectLocationRoute(
    onSaved: () -> Unit,
    viewModel: SelectLocationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    SelectLocationScreen(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun SelectLocationScreen(
    uiState: SelectLocationUiState,
    onEvent: (SelectLocationEvent) -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold(
        bottomBar = {
            EvenroButton(
                text = if (uiState.isLoading) "SAVING..." else "ADD",
                onClick = { onEvent(SelectLocationEvent.Submit) },
                enabled = !uiState.isLoading && uiState.query.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            EvenroTextField(
                value = uiState.query,
                onValueChange = { onEvent(SelectLocationEvent.QueryChanged(it)) },
                placeholder = "Search new address...",
                leadingIcon = Icons.Filled.Search,
                modifier = Modifier.padding(16.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(extended.surfaceMuted),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectLocationScreenPreview() {
    SmartEventsTheme { SelectLocationScreen(uiState = SelectLocationUiState(), onEvent = {}) }
}
