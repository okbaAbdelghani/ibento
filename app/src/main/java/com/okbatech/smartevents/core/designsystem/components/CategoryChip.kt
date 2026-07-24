package com.okbatech.smartevents.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.PillShape

/** The pill category filter chip on Home / Filter ("Design", "Art", "Sports", ...). */
@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val extended = EvenroTheme.extendedColors
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else extended.surfaceMuted
    val contentColor = if (selected) MaterialTheme.colorScheme.surface else extended.textPrimary

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(containerColor, PillShape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = contentColor)
    }
}
