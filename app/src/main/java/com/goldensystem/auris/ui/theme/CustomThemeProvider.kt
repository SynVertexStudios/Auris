// ui/theme/CustomThemeProvider.kt
package com.goldensystem.auris.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.goldensystem.auris.data.preferences.CustomThemeConfig

fun customColorScheme(
    config: CustomThemeConfig,
    isDark: Boolean = true
): ColorScheme {
    // CORES PRINCIPAIS
    val primary = Color(config.primaryColor)
    val onPrimary = Color(config.onPrimaryColor)
    val secondary = Color(config.secondaryColor)
    val onSecondary = Color(config.onSecondaryColor)  // ✅ USA ON SECONDARY
    val tertiary = Color(config.tertiaryColor)        // ✅ USA TERTIARY
    val onTertiary = Color(config.onTertiaryColor)    // ✅ USA ON TERTIARY
    
    // BACKGROUND E SURFACE
    val background = Color(config.backgroundColor)
    val onBackground = Color(config.onBackgroundColor)  // ✅ USA ON BACKGROUND
    val surface = Color(config.surfaceColor)            // ✅ USA SURFACE
    val onSurface = Color(config.onSurfaceColor)
    val surfaceVariant = Color(config.surfaceVariantColor)  // ✅ USA SURFACE VARIANT
    val onSurfaceVariant = Color(config.onSurfaceVariantColor)  // ✅ USA ON SURFACE VARIANT
    
    // CONTAINERS
    val primaryContainer = Color(config.primaryColor).copy(alpha = 0.2f)
    val onPrimaryContainer = onPrimary
    val secondaryContainer = Color(config.secondaryContainerColor)  // ✅ USA SECONDARY CONTAINER
    val onSecondaryContainer = Color(config.onSecondaryContainerColor)  // ✅ USA ON SECONDARY CONTAINER
    val tertiaryContainer = Color(config.tertiaryContainerColor)  // ✅ USA TERTIARY CONTAINER
    val onTertiaryContainer = Color(config.onTertiaryContainerColor)  // ✅ USA ON TERTIARY CONTAINER
    
    // ERROR
    val error = Color(config.errorColor)  // ✅ USA ERROR
    val onError = Color(config.onErrorColor)  // ✅ USA ON ERROR
    val errorContainer = Color(config.errorContainerColor)  // ✅ USA ERROR CONTAINER
    val onErrorContainer = Color(config.onErrorContainerColor)  // ✅ USA ON ERROR CONTAINER
    
    // OUTLINE
    val outline = Color(config.outlineColor)  // ✅ USA OUTLINE
    val outlineVariant = Color(config.outlineVariantColor)  // ✅ USA OUTLINE VARIANT
    
    // OUTROS
    val surfaceTint = Color(config.surfaceTintColor)  // ✅ USA SURFACE TINT
    val inversePrimary = Color(config.inversePrimaryColor)  // ✅ USA INVERSE PRIMARY
    val inverseSurface = Color(config.inverseSurfaceColor)  // ✅ USA INVERSE SURFACE
    val inverseOnSurface = Color(config.inverseOnSurfaceColor)  // ✅ USA INVERSE ON SURFACE
    val scrim = Color(config.scrimColor)  // ✅ USA SCRIM

    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        tertiary = tertiary,
        onTertiary = onTertiary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
        surfaceTint = surfaceTint,
        inversePrimary = inversePrimary,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        scrim = scrim
    )
}