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
    // NÃO MEXER - já tem config.
    val primary = Color(config.primaryColor)
    val onPrimary = Color(config.onPrimaryColor)
    val secondary = Color(config.secondaryColor)
    val onSecondary = Color(config.onPrimaryColor)
    val tertiary = Color(config.accentColor)
    val onTertiary = Color(config.onPrimaryColor)
    val background = Color(config.backgroundColor)
    val onBackground = Color(config.onSurfaceColor)
    val surface = Color(config.backgroundColor).copy(alpha = 0.8f)
    val onSurface = Color(config.onSurfaceColor)
    val surfaceVariant = Color(config.backgroundColor).copy(alpha = 0.6f)
    val onSurfaceVariant = Color(config.onSurfaceColor).copy(alpha = 0.6f)
    
    // MODIFICADOS - agora usam config.
    val primaryContainer = Color(config.primaryColor).copy(alpha = 0.2f)
    val onPrimaryContainer = Color(config.onPrimaryColor)
    val secondaryContainer = Color(config.secondaryColor).copy(alpha = 0.2f)
    val onSecondaryContainer = Color(config.onPrimaryColor)
    val tertiaryContainer = Color(config.accentColor).copy(alpha = 0.2f)
    val onTertiaryContainer = Color(config.onPrimaryColor)
    val error = Color(config.errorColor)
    val onError = Color(config.onErrorColor)
    val errorContainer = Color(config.errorColor).copy(alpha = 0.2f)
    val onErrorContainer = Color(config.onErrorColor)
    
    // NÃO MEXER - já tem config.
    val outline = Color(config.accentColor).copy(alpha = 0.5f)
    val outlineVariant = Color(config.accentColor).copy(alpha = 0.3f)
    
    // MODIFICADOS - agora usam config.
    val surfaceTint = Color(config.surfaceTintColor)
    val inversePrimary = Color(config.inversePrimaryColor)
    val inverseSurface = Color(config.inverseSurfaceColor)
    val inverseOnSurface = Color(config.inverseOnSurfaceColor)
    val scrim = Color(config.scrimColor)

    // NÃO MEXER
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