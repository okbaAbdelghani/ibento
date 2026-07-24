package com.okbatech.smartevents.feature.profile.presentation.tooltip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.okbatech.smartevents.core.designsystem.components.TooltipOverlay
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

/** Standalone display of the "Tool Tip" / "Tool Tip v2" coach-mark, reached for click-through. */
@Composable
fun TooltipRoute(message: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            TooltipOverlay(
                message = message,
                onDismiss = onDismiss,
                modifier = Modifier.width(260.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TooltipRoutePreview() {
    SmartEventsTheme {
        TooltipRoute(message = "Swipe categories to explore more events", onDismiss = {})
    }
}
