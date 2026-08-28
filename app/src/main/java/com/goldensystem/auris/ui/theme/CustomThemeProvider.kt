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
    // TODAS AS CORES USAM config. DIRETAMENTE
    val primary = Color(config.primaryColor)
    val onPrimary = Color(config.onPrimaryColor)
    val secondary = Color(config.secondaryColor)
    val onSecondary = Color(config.onSecondaryColor)
    val tertiary = Color(config.tertiaryColor)
    val onTertiary = Color(config.primaryColor)
    val background = Color(config.backgroundColor)
    val onBackground = Color(config.onBackgroundColor)
    val surface = Color(config.surfaceColor)
    val onSurface = Color(config.onSurfaceColor)
    val surfaceVariant = Color(config.surfaceVariantColor)
    val onSurfaceVariant = Color(config.onSurfaceVariantColor)
    val primaryContainer = Color(config.primaryContainerColor)
    val onPrimaryContainer = Color(config.onPrimaryContainerColor)
    val secondaryContainer = Color(config.secondaryContainerColor)
    val onSecondaryContainer = Color(config.onSecondaryContainerColor)
    val tertiaryContainer = Color(config.tertiaryContainerColor)
    val onTertiaryContainer = Color(config.onTertiaryContainerColor)
    val error = Color(config.errorColor)
    val onError = Color(config.onErrorColor)
    val errorContainer = Color(config.errorContainerColor)
    val onErrorContainer = Color(config.onErrorContainerColor)
    val outline = Color(config.outlineColor)
    val outlineVariant = Color(config.outlineVariantColor)
    val surfaceTint = Color(config.surfaceTintColor)
    val inversePrimary = Color(config.inversePrimaryColor)
    val inverseSurface = Color(config.inverseSurfaceColor)
    val inverseOnSurface = Color(config.inverseOnSurfaceColor)
    val scrim = Color(config.scrimColor)

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