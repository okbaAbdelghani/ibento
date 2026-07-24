package com.okbatech.smartevents.feature.social.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.feature.social.domain.model.AppNotification
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatRelativeTime

@Composable
fun NotificationRoute(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationScreen(uiState = uiState, onNotificationClicked = viewModel::onNotificationClicked, onBack = onBack)
}

@Composable
private fun NotificationScreen(
    uiState: NotificationUiState,
    onNotificationClicked: (String) -> Unit,
    onBack: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, top = 8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Notifications", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp))
            }

            if (uiState.notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            tint = extended.textTertiary,
                            modifier = Modifier.size(56.dp),
                        )
                        Text(
                            "No notifications yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extended.textSecondary,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
            } else {
                LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationRow(notification = notification, onClick = { onNotificationClicked(notification.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: AppNotification, onClick: () -> Unit) {
    val extended = EvenroTheme.extendedColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp, end = 12.dp)
                .size(8.dp)
                .background(
                    if (notification.isRead) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.primary,
                    CircleShape,
                ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(notification.title, style = MaterialTheme.typography.titleSmall)
            Text(
                notification.body,
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                formatRelativeTime(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = extended.textTertiary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationScreenPreview() {
    SmartEventsTheme { NotificationScreen(uiState = NotificationUiState(), onNotificationClicked = {}, onBack = {}) }
}
