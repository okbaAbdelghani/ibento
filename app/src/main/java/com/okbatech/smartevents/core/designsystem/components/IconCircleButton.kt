package com.okbatech.smartevents.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The translucent circular icon button used for back arrows, favorite hearts,
 * and message/phone actions layered over hero images.
 */
@Composable
fun IconCircleButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 40.dp,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .background(color = containerColor, shape = CircleShape),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint)
    }
}
