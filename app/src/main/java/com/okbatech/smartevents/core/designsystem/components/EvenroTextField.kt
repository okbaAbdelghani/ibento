package com.okbatech.smartevents.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes

/**
 * The soft rounded input used on Sign in / Sign up / Reset Password
 * (light gray fill, leading icon, no visible border until focused).
 */
@Composable
fun EvenroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    val extended = EvenroTheme.extendedColors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = extended.textTertiary) },
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null, tint = extended.textSecondary) }
        },
        trailingIcon = trailingIcon,
        singleLine = true,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = Shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = extended.surfaceMuted,
            unfocusedContainerColor = extended.surfaceMuted,
            disabledContainerColor = extended.surfaceMuted,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = extended.surfaceMuted,
        ),
    )
}
