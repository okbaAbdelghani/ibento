package com.okbatech.smartevents.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.okbatech.smartevents.ui.theme.EvenroTheme

/**
 * The compact horizontal event row used on Search / See All Events / My Events / Wish List:
 * small square thumbnail, title, date + location, price, and a JOIN NOW action.
 */
@Composable
fun EventListRow(
    imageUrl: String,
    title: String,
    dateLabel: String,
    locationLabel: String,
    priceLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = "JOIN NOW",
    onActionClick: (() -> Unit)? = null,
) {
    val extended = EvenroTheme.extendedColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp)),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary,
                    modifier = Modifier.padding(start = 4.dp, end = 10.dp),
                )
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = locationLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary,
                    modifier = Modifier.padding(start = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = priceLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            if (onActionClick != null) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = extended.textPrimary,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable(onClick = onActionClick),
                )
            }
        }
    }
}
