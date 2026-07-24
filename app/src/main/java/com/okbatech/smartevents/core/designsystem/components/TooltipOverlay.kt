package com.okbatech.smartevents.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes

/**
 * A dismissible coach-mark bubble ("Tool Tip" / "Tool Tip v2" in the Figma file), shown
 * once near a feature the first time a user could discover it. Callers position it via
 * [modifier] (e.g. anchored above a FAB) and clear a DataStore "seen" flag on [onDismiss].
 */
@Composable
fun TooltipOverlay(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = "Got it",
) {
    val extended = EvenroTheme.extendedColors

    Column(
        modifier = modifier
            .background(extended.ink, Shapes.medium)
            .padding(16.dp),
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = actionLabel.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onDismiss),
            )
        }
    }
}
