package com.okbatech.smartevents.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.okbatech.smartevents.core.designsystem.components.EvenroButton

/**
 * Stand-in for screens not yet built past Phase 0/1, so the full 59-route graph
 * stays navigable end-to-end. [onBack] is null on graph roots that have no back target.
 */
@Composable
fun PlaceholderScreen(
    screenName: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = screenName, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Coming in a later phase",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            if (onBack != null) {
                EvenroButton(text = "Back", onClick = onBack)
            }
        }
    }
}
