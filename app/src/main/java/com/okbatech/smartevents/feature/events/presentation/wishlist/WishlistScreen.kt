package com.okbatech.smartevents.feature.events.presentation.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.core.designsystem.components.EventCard
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate

@Composable
fun WishlistRoute(
    onBack: () -> Unit,
    onOpenEventDetails: (String) -> Unit,
    viewModel: WishlistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WishlistScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenEventDetails = onOpenEventDetails,
        onRemove = viewModel::removeFromWishlist,
        onJoin = viewModel::join,
    )
}

@Composable
private fun WishlistScreen(
    uiState: WishlistUiState,
    onBack: () -> Unit,
    onOpenEventDetails: (String) -> Unit,
    onRemove: (String) -> Unit,
    onJoin: (String) -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Wish List",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            if (uiState.events.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "You haven't saved any events yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
                ) {
                    items(uiState.events, key = { it.id }) { event ->
                        EventCard(
                            imageUrl = event.imageUrl,
                            title = event.title,
                            dateLabel = formatEventDate(event.startDateTime),
                            locationLabel = "${event.venueName}, ${event.city}",
                            attendeeAvatarUrls = emptyList(),
                            isFavorite = true,
                            onFavoriteClick = { onRemove(event.id) },
                            onClick = { onOpenEventDetails(event.id) },
                            priceLabel = "$${event.priceAmount.toInt()} USD",
                            onActionClick = { onJoin(event.id) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WishlistScreenPreview() {
    SmartEventsTheme {
        WishlistScreen(uiState = WishlistUiState(), onBack = {}, onOpenEventDetails = {}, onRemove = {}, onJoin = {})
    }
}
