package com.okbatech.smartevents.feature.events.presentation.reviewpopup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroTextField
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

@Composable
fun EventReviewPopupRoute(
    onDismiss: () -> Unit,
    viewModel: EventReviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.submitted) {
        if (uiState.submitted) onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        EventReviewContent(uiState = uiState, onEvent = viewModel::onEvent)
    }
}

@Composable
private fun EventReviewContent(
    uiState: EventReviewUiState,
    onEvent: (EventReviewEvent) -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, Shapes.large)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Rate this event", style = MaterialTheme.typography.titleLarge)
        Text(
            uiState.eventTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = extended.textSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..5).forEach { star ->
                Icon(
                    imageVector = if (star <= uiState.rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Rate $star",
                    tint = extended.gold,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onEvent(EventReviewEvent.RatingChanged(star.toFloat())) },
                )
            }
        }

        EvenroTextField(
            value = uiState.comment,
            onValueChange = { onEvent(EventReviewEvent.CommentChanged(it)) },
            placeholder = "Share your experience (optional)",
            modifier = Modifier.padding(top = 20.dp),
        )

        EvenroButton(
            text = if (uiState.isSubmitting) "SUBMITTING..." else "SUBMIT REVIEW",
            onClick = { onEvent(EventReviewEvent.Submit) },
            enabled = uiState.rating > 0f && !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(52.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EventReviewContentPreview() {
    SmartEventsTheme {
        EventReviewContent(uiState = EventReviewUiState(eventTitle = "Shere Bangla Concert", rating = 4f), onEvent = {})
    }
}
