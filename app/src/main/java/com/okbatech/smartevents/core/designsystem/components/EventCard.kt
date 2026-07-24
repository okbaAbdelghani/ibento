package com.okbatech.smartevents.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes

/**
 * The event card used on Home, See All Events, and Wish List — image header with a
 * favorite toggle, date/location, avatar stack of attendees, and a price/join action.
 */
@Composable
fun EventCard(
    imageUrl: String,
    title: String,
    dateLabel: String,
    locationLabel: String,
    attendeeAvatarUrls: List<String>,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    priceLabel: String? = null,
    actionLabel: String = "JOIN NOW",
    onActionClick: (() -> Unit)? = null,
) {
    val extended = EvenroTheme.extendedColors
    Column(
        modifier = modifier
            .clip(Shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.4f)
                .clip(Shapes.large),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                        ),
                    ),
            )
            IconCircleButton(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                onClick = onFavoriteClick,
                contentDescription = "Toggle favorite",
                containerColor = Color.White.copy(alpha = 0.9f),
                tint = MaterialTheme.colorScheme.primary,
                size = 36.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
            )
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary,
                    modifier = Modifier.padding(start = 6.dp, end = 12.dp),
                )
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = locationLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary,
                    modifier = Modifier.padding(start = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (attendeeAvatarUrls.isNotEmpty()) {
                    AvatarStack(avatarUrls = attendeeAvatarUrls, avatarSize = 24.dp)
                } else if (priceLabel != null) {
                    Text(
                        text = priceLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                if (onActionClick != null) {
                    EvenroButton(
                        text = actionLabel,
                        onClick = onActionClick,
                        style = EvenroButtonStyle.Primary,
                        modifier = Modifier.size(width = 118.dp, height = 38.dp),
                    )
                }
            }
        }
    }
}
