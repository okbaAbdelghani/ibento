package com.okbatech.smartevents.feature.profile.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

/**
 * The quick-access drawer reachable from Profile's menu icon. Both "Menu v1" and
 * "Menu v2" Figma screens share this same list of destinations, so one route serves both
 * (matching the SearchWhiteBar/SearchColorBar precedent elsewhere in this nav graph).
 */
@Composable
fun MenuRoute(
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenMyEvents: () -> Unit,
    onOpenWishlist: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMessages: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) onSignedOut()
    }

    MenuScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        onOpenEditProfile = onOpenEditProfile,
        onOpenMyEvents = onOpenMyEvents,
        onOpenWishlist = onOpenWishlist,
        onOpenNotifications = onOpenNotifications,
        onOpenMessages = onOpenMessages,
        onSignOut = viewModel::onSignOut,
    )
}

@Composable
private fun MenuScreen(
    uiState: MenuUiState,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenMyEvents: () -> Unit,
    onOpenWishlist: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMessages: () -> Unit,
    onSignOut: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onBack),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .background(MaterialTheme.colorScheme.surface, Shapes.extraLarge)
                    .padding(vertical = 24.dp, horizontal = 20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onOpenProfile)) {
                    AsyncImage(
                        model = uiState.userAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp).clip(CircleShape).background(extended.surfaceMuted),
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(text = uiState.userName.ifBlank { "Guest" }, style = MaterialTheme.typography.titleMedium)
                        Text(text = "View profile", style = MaterialTheme.typography.bodySmall, color = extended.textSecondary)
                    }
                }

                Column(modifier = Modifier.padding(top = 20.dp)) {
                    MenuRow(icon = Icons.Filled.Edit, label = "Edit Profile", onClick = onOpenEditProfile)
                    MenuRow(icon = Icons.Filled.CalendarMonth, label = "My Events", onClick = onOpenMyEvents)
                    MenuRow(icon = Icons.Outlined.FavoriteBorder, label = "Wish List", onClick = onOpenWishlist)
                    MenuRow(icon = Icons.Filled.NotificationsNone, label = "Notifications", onClick = onOpenNotifications)
                    MenuRow(icon = Icons.Filled.ChatBubbleOutline, label = "Messages", onClick = onOpenMessages)
                    MenuRow(icon = Icons.AutoMirrored.Outlined.Logout, label = "Sign Out", onClick = onSignOut, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit, tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary) {
    val extended = EvenroTheme.extendedColors
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (tint == MaterialTheme.colorScheme.error) tint else extended.textPrimary,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    SmartEventsTheme {
        MenuScreen(
            uiState = MenuUiState(userName = "MD Rafi Islam"),
            onBack = {},
            onOpenProfile = {},
            onOpenEditProfile = {},
            onOpenMyEvents = {},
            onOpenWishlist = {},
            onOpenNotifications = {},
            onOpenMessages = {},
            onSignOut = {},
        )
    }
}
