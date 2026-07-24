package com.okbatech.smartevents.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.okbatech.smartevents.ui.theme.EvenroTheme
import kotlin.math.roundToInt

/** A row of 5 stars, filled up to [rating] (0f..5f). */
@Composable
fun RatingStars(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: androidx.compose.ui.unit.Dp = 16.dp,
    maxStars: Int = 5,
) {
    val filledCount = rating.roundToInt().coerceIn(0, maxStars)
    val gold = EvenroTheme.extendedColors.gold

    Row(modifier = modifier) {
        repeat(maxStars) { index ->
            Icon(
                imageVector = if (index < filledCount) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = gold,
                modifier = Modifier.size(starSize),
            )
        }
    }
}
