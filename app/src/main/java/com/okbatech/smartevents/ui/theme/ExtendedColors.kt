package com.okbatech.smartevents.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors used across Evenro screens that don't map cleanly onto
 * Material3's ColorScheme slots (e.g. the pill "ink" buttons, soft peach chips).
 */
data class EvenroExtendedColors(
    val ink: Color,
    val inkLight: Color,
    val softPeach: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val gold: Color,
    val success: Color,
    val surfaceMuted: Color,
)

val LocalEvenroExtendedColors = staticCompositionLocalOf {
    EvenroExtendedColors(
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
}
