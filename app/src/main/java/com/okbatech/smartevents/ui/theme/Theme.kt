package com.okbatech.smartevents.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val EvenroLightColorScheme = lightColorScheme(
    primary = EvenroOrange,
    onPrimary = EvenroSurface,
    primaryContainer = EvenroOrangeLight,
    onPrimaryContainer = EvenroOrangeDark,
    secondary = EvenroInk,
    onSecondary = EvenroSurface,
    background = EvenroBackground,
    onBackground = EvenroTextPrimary,
    surface = EvenroSurface,
    onSurface = EvenroTextPrimary,
    surfaceVariant = EvenroSurfaceMuted,
    onSurfaceVariant = EvenroTextSecondary,
    outline = EvenroDivider,
    error = EvenroError,
)

private val EvenroExtendedColorsInstance = EvenroExtendedColors(
    ink = EvenroInk,
    inkLight = EvenroInkLight,
    softPeach = EvenroSoftPeach,
    divider = EvenroDivider,
    textPrimary = EvenroTextPrimary,
    textSecondary = EvenroTextSecondary,
    textTertiary = EvenroTextTertiary,
    gold = EvenroGold,
    success = EvenroSuccess,
    surfaceMuted = EvenroSurfaceMuted,
)

/**
 * Evenro is a single, deliberately light-only theme matching the Figma reference
 * (no dark-mode variant exists in the source design).
 */
@Composable
fun SmartEventsTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalEvenroExtendedColors provides EvenroExtendedColorsInstance) {
        MaterialTheme(
            colorScheme = EvenroLightColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}

object EvenroTheme {
    val extendedColors: EvenroExtendedColors
        @Composable
        get() = LocalEvenroExtendedColors.current
}
