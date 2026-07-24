package com.okbatech.smartevents.feature.auth.presentation.interest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

private val InterestIcons: Map<String, ImageVector> = mapOf(
    "Design" to Icons.Filled.Brush,
    "Music" to Icons.Filled.MusicNote,
    "Art" to Icons.Filled.Palette,
    "Sports" to Icons.Filled.SportsBasketball,
    "Food" to Icons.Filled.Restaurant,
    "Others" to Icons.Filled.MoreHoriz,
)

@Composable
fun SelectInterestRoute(
    onSaved: () -> Unit,
    viewModel: SelectInterestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    SelectInterestScreen(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun SelectInterestScreen(
    uiState: SelectInterestUiState,
    onEvent: (SelectInterestEvent) -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Text(
                text = "Select Your $MaxInterests Interests",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                AvailableInterests.chunked(2).forEach { rowInterests ->
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        rowInterests.forEach { interest ->
                            InterestTile(
                                interest = interest,
                                selected = interest in uiState.selected,
                                onClick = { onEvent(SelectInterestEvent.ToggleInterest(interest)) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            EvenroButton(
                text = "NEXT",
                onClick = { onEvent(SelectInterestEvent.Submit) },
                enabled = uiState.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            )
        }
    }
}

@Composable
private fun InterestTile(
    interest: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = EvenroTheme.extendedColors
    Column(
        modifier = modifier
            .aspectRatio(1.1f)
            .clip(Shapes.medium)
            .background(extended.surfaceMuted, Shapes.medium)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                shape = Shapes.medium,
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = InterestIcons.getValue(interest),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = interest,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectInterestScreenPreview() {
    SmartEventsTheme {
        SelectInterestScreen(
            uiState = SelectInterestUiState(selected = setOf("Design", "Sports", "Food")),
            onEvent = {},
        )
    }
}
