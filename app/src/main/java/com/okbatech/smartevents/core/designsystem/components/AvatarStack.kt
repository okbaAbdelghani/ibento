package com.okbatech.smartevents.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.okbatech.smartevents.ui.theme.EvenroTheme

/**
 * Overlapping circular avatars with a trailing "+N" bubble, used for
 * "Members joined" rows on event cards and details.
 */
@Composable
fun AvatarStack(
    avatarUrls: List<String>,
    modifier: Modifier = Modifier,
    maxVisible: Int = 3,
    avatarSize: androidx.compose.ui.unit.Dp = 28.dp,
    extraCount: Int = 0,
) {
    val visible = avatarUrls.take(maxVisible)
    val overflow = extraCount + (avatarUrls.size - visible.size).coerceAtLeast(0)

    Box(modifier = modifier) {
        visible.forEachIndexed { index, url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .offset(x = (index * avatarSize.value * 0.6f).dp)
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(EvenroTheme.extendedColors.surfaceMuted)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
            )
        }
        if (overflow > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (visible.size * avatarSize.value * 0.6f).dp)
                    .size(avatarSize)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+$overflow",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }
    }
}
