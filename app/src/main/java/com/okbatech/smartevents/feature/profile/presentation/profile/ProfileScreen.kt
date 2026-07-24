package com.okbatech.smartevents.feature.profile.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.okbatech.smartevents.core.designsystem.components.EvenroBottomNavBar
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroButtonStyle
import com.okbatech.smartevents.core.designsystem.components.EvenroNavDestination
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

@Composable
fun ProfileRoute(
    onOpenEditProfile: () -> Unit,
    onOpenOrganizerProfile: (String) -> Unit,
    onOpenMyEvents: () -> Unit,
    onOpenWishlist: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenMenu: () -> Unit,
    onSignedOut: () -> Unit,
    onNavigate: (EvenroNavDestination) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) onSignedOut()
    }

    ProfileScreen(
        uiState = uiState,
        onOpenEditProfile = onOpenEditProfile,
        onOpenOrganizerProfile = { uiState.userId?.let(onOpenOrganizerProfile) },
        onOpenMyEvents = onOpenMyEvents,
        onOpenWishlist = onOpenWishlist,
        onOpenNotifications = onOpenNotifications,
        onOpenMessages = onOpenMessages,
        onOpenMenu = onOpenMenu,
        onSignOut = viewModel::onSignOut,
        onNavigate = onNavigate,
    )
}

@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    onOpenEditProfile: () -> Unit,
    onOpenOrganizerProfile: () -> Unit,
    onOpenMyEvents: () -> Unit,
    onOpenWishlist: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenMenu: () -> Unit,
    onSignOut: () -> Unit,
    onNavigate: (EvenroNavDestination) -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold(
        bottomBar = {
            EvenroBottomNavBar(selected = EvenroNavDestination.Profile, onSelect = onNavigate)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Profile", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onOpenMenu) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = uiState.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(extended.surfaceMuted),
                )
                Text(
                    text = uiState.name.ifBlank { "Guest" },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(text = uiState.email, style = MaterialTheme.typography.bodyMedium, color = extended.textSecondary)
                if (!uiState.bio.isNullOrBlank()) {
                    Text(
                        text = uiState.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ProfileStat(count = uiState.followerCount, label = "Followers")
                    ProfileStat(count = uiState.followingCount, label = "Following")
                    ProfileStat(count = uiState.hostedEventCount, label = "Hosted")
                }

                EvenroButton(
                    text = "EDIT PROFILE",
                    onClick = onOpenEditProfile,
                    style = EvenroButtonStyle.Soft,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                )

                if (uiState.isOrganizer) {
                    EvenroButton(
                        text = "VIEW ORGANIZER PROFILE",
                        onClick = onOpenOrganizerProfile,
                        style = EvenroButtonStyle.Outline,
                        leadingIcon = Icons.Filled.Storefront,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = extended.divider)

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ProfileMenuRow(icon = Icons.Filled.CalendarMonth, label = "My Events", onClick = onOpenMyEvents)
                ProfileMenuRow(icon = Icons.Outlined.FavoriteBorder, label = "Wish List", onClick = onOpenWishlist)
                ProfileMenuRow(icon = Icons.Filled.NotificationsNone, label = "Notifications", onClick = onOpenNotifications)
                ProfileMenuRow(icon = Icons.Filled.ChatBubbleOutline, label = "Messages", onClick = onOpenMessages)
                ProfileMenuRow(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    label = "Sign Out",
                    onClick = onSignOut,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ProfileStat(count: Int, label: String) {
    val extended = EvenroTheme.extendedColors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = extended.textSecondary)
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    val extended = EvenroTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (tint == MaterialTheme.colorScheme.error) tint else extended.textPrimary,
            modifier = Modifier.padding(start = 16.dp).weight(1f),
        )
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = extended.textTertiary)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    SmartEventsTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                name = "MD Rafi Islam",
                email = "rafi@evenro.app",
                followerCount = 812,
                followingCount = 94,
                hostedEventCount = 3,
                isOrganizer = true,
            ),
            onOpenEditProfile = {},
            onOpenOrganizerProfile = {},
            onOpenMyEvents = {},
            onOpenWishlist = {},
            onOpenNotifications = {},
            onOpenMessages = {},
            onOpenMenu = {},
            onSignOut = {},
            onNavigate = {},
        )
    }
}
