package com.okbatech.smartevents.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.PillShape

/**
 * The search field + filter button pairing seen on Home and the dedicated Search screens.
 * [containerColor] lets callers match either the light (white bar) or dark (overlay on
 * hero image) variants seen across the Search screens.
 */
@Composable
fun EvenroSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Find amazing events",
    onFilterClick: (() -> Unit)? = null,
    onFocused: (() -> Unit)? = null,
    containerColor: Color = EvenroTheme.extendedColors.surfaceMuted,
    contentColor: Color = EvenroTheme.extendedColors.textSecondary,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .background(containerColor, PillShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = contentColor)
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = contentColor)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = contentColor),
                    modifier = if (onFocused != null) {
                        Modifier.onFocusChanged { if (it.isFocused) onFocused() }
                    } else {
                        Modifier
                    },
                )
            }
        }
        if (onFilterClick != null) {
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(containerColor, CircleShape),
            ) {
                Icon(Icons.Outlined.Tune, contentDescription = "Filter", tint = contentColor)
            }
        }
    }
}
