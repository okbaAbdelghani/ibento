package com.okbatech.smartevents.feature.social.presentation.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChatInBookedDetailsRoute(
    onBack: () -> Unit,
    viewModel: ChatInBookedDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatContent(
        uiState = uiState,
        onDraftChanged = viewModel::onDraftChanged,
        onSend = viewModel::send,
        onBack = onBack,
    )
}
