package com.okbatech.smartevents.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.PillShape

enum class EvenroButtonStyle { Primary, Accent, Soft, Outline }

/**
 * The pill-shaped button used throughout Evenro (Sign in, Join Now, Buy a Ticket,
 * Follow, Messages). [style] picks the fill matching the screen's intent.
 */
@Composable
fun EvenroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: EvenroButtonStyle = EvenroButtonStyle.Primary,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val extended = EvenroTheme.extendedColors
    val colors = when (style) {
        EvenroButtonStyle.Primary -> ButtonDefaults.buttonColors(
            containerColor = extended.ink,
            contentColor = MaterialTheme.colorScheme.surface,
        )

        EvenroButtonStyle.Accent -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.surface,
        )

        EvenroButtonStyle.Soft -> ButtonDefaults.buttonColors(
            containerColor = extended.softPeach,
            contentColor = MaterialTheme.colorScheme.primary,
        )

        EvenroButtonStyle.Outline -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = extended.textPrimary,
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = PillShape,
        colors = colors,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
