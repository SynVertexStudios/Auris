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
    val primary = Color(config.primaryColor)
    val onPrimary = Color(config.onPrimaryColor)
    val secondary = Color(config.secondaryColor)
    val onSecondary = Color(config.onPrimaryColor)  // 👈 USA ON PRIMARY
    val tertiary = Color(config.accentColor)        // 👈 USA A COR DE ACENTO
    val onTertiary = Color(config.onPrimaryColor)   // 👈 USA ON PRIMARY
    val background = Color(config.backgroundColor)
    val onBackground = Color(config.onSurfaceColor)
    val surface = Color(config.backgroundColor).copy(alpha = 0.8f)
    val onSurface = Color(config.onSurfaceColor)
    val surfaceVariant = Color(config.backgroundColor).copy(alpha = 0.6f)
    val onSurfaceVariant = Color(config.onSurfaceColor).copy(alpha = 0.6f)
    val primaryContainer = primary.copy(alpha = 0.2f)
    val onPrimaryContainer = onPrimary
    val secondaryContainer = secondary.copy(alpha = 0.2f)
    val onSecondaryContainer = onPrimary
    val tertiaryContainer = tertiary.copy(alpha = 0.2f)
    val onTertiaryContainer = onPrimary
    val error = Color(0xFFFF5252.toInt())
    val onError = Color(0xFFFFFFFF.toInt())
    val errorContainer = Color(0xFFFF5252.toInt()).copy(alpha = 0.2f)
    val onErrorContainer = Color(0xFFFFFFFF.toInt())
    val outline = Color(config.accentColor).copy(alpha = 0.5f)
    val outlineVariant = Color(config.accentColor).copy(alpha = 0.3f)
    val surfaceTint = primary
    val inversePrimary = primary
    val inverseSurface = surface
    val inverseOnSurface = onSurface
    val scrim = Color(0x66000000.toInt())
    
    /*vamos englobar essa cor no geral containers
    》》*/val surfaceContainer = Color(config.surfaceContainerColor)

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