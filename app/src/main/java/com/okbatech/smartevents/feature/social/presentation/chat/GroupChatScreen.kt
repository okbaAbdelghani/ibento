package com.okbatech.smartevents.feature.social.presentation.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GroupChatRoute(
    onBack: () -> Unit,
    onOpenGroupDetails: (String) -> Unit,
    viewModel: GroupChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatContent(
        uiState = uiState,
        onDraftChanged = viewModel::onDraftChanged,
        onSend = viewModel::send,
        onBack = onBack,
        onTitleClick = { onOpenGroupDetails(viewModel.eventId) },
    )
}
