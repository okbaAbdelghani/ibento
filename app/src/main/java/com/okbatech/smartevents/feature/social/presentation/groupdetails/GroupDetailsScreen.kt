package com.okbatech.smartevents.feature.social.presentation.groupdetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroButtonStyle
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate

@Composable
fun GroupDetailsRoute(
    onBack: () -> Unit,
    onOpenGroupChat: (String) -> Unit,
    onOpenGroupMembers: (String) -> Unit,
    viewModel: GroupDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GroupDetailsScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenGroupChat = { onOpenGroupChat(viewModel.eventId) },
        onOpenGroupMembers = { onOpenGroupMembers(viewModel.eventId) },
    )
}

@Composable
private fun GroupDetailsScreen(
    uiState: GroupDetailsUiState,
    onBack: () -> Unit,
    onOpenGroupChat: () -> Unit,
    onOpenGroupMembers: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    val event = uiState.event

    Scaffold(
        bottomBar = {
            if (event != null) {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(16.dp)) {
                    EvenroButton(
                        text = "OPEN GROUP CHAT",
                        onClick = onOpenGroupChat,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    )
                    EvenroButton(
                        text = "VIEW MEMBERS",
                        style = EvenroButtonStyle.Soft,
                        onClick = onOpenGroupMembers,
                        modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 12.dp),
                    )
                }
            }
        },
    ) { padding ->
        if (event == null) return@Scaffold
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.6f),
                )
                IconButton(onClick = onBack, modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = androidx.compose.ui.graphics.Color.White)
                }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(event.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${formatEventDate(event.startDateTime)} · ${event.venueName}, ${event.city}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.textSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    "${uiState.memberCount} members",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupDetailsScreenPreview() {
    SmartEventsTheme {
        GroupDetailsScreen(uiState = GroupDetailsUiState(), onBack = {}, onOpenGroupChat = {}, onOpenGroupMembers = {})
    }
}
