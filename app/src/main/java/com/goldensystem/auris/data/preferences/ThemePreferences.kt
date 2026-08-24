// data/preferences/ThemePreferences.kt
package com.goldensystem.auris.data.preferences

import android.graphics.Bitmap
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// data/preferences/ThemePreferences.kt
data class CustomThemeConfig(
    val isEnabled: Boolean = false,
    
    // CORES PRINCIPAIS
    val primary: Int = 0xFF6750A4.toInt(),
    val onPrimary: Int = 0xFFFFFFFF.toInt(),
    val primaryContainer: Int = 0xFFEADDFF.toInt(),
    val onPrimaryContainer: Int = 0xFF21005D.toInt(),
    
    val secondary: Int = 0xFF625B71.toInt(),
    val onSecondary: Int = 0xFFFFFFFF.toInt(),
    val secondaryContainer: Int = 0xFFE8DEF8.toInt(),
    val onSecondaryContainer: Int = 0xFF1D192B.toInt(),
    
    val tertiary: Int = 0xFF7D5260.toInt(),
    val onTertiary: Int = 0xFFFFFFFF.toInt(),
    val tertiaryContainer: Int = 0xFFFFD8E4.toInt(),
    val onTertiaryContainer: Int = 0xFF31111D.toInt(),
    
    // FUNDO E SUPERFÍCIE
    val background: Int = 0xFFFFFBFE.toInt(),
    val onBackground: Int = 0xFF1C1B1F.toInt(),
    val surface: Int = 0xFFFFFBFE.toInt(),
    val onSurface: Int = 0xFF1C1B1F.toInt(),
    val surfaceVariant: Int = 0xFFE7E0EC.toInt(),
    val onSurfaceVariant: Int = 0xFF49454F.toInt(),
    
    // ERRO
    val error: Int = 0xFFB3261E.toInt(),
    val onError: Int = 0xFFFFFFFF.toInt(),
    val errorContainer: Int = 0xFFF9DEDC.toInt(),
    val onErrorContainer: Int = 0xFF410E0B.toInt(),
    
    // OUTRAS
    val outline: Int = 0xFF79747E.toInt(),
    val outlineVariant: Int = 0xFFCAC4D0.toInt(),
    val surfaceTint: Int = 0xFF6750A4.toInt(),
    val inversePrimary: Int = 0xFFD0BCFF.toInt(),
    val inverseSurface: Int = 0xFF313033.toInt(),
    val inverseOnSurface: Int = 0xFFF4EFF4.toInt(),
    val scrim: Int = 0xFF000000.toInt(),
    
    // WALLPAPER (mantém igual)
    val wallpaperType: WallpaperType = WallpaperType.SOLID,
    val wallpaperColor: Int = 0xFF1E1234.toInt(),
    val wallpaperUri: String? = null,
    val wallpaperUrl: String? = null,
    val wallpaperBlur: Float = 0.5f,
    val wallpaperDim: Float = 0.3f
)

enum class WallpaperType {
    SOLID,
    GALLERY,
    SERVER
}

@Singleton
class ThemePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
    private val THEME_ENABLED = booleanPreferencesKey("custom_theme_enabled")
    
    // CORES PRINCIPAIS
    private val PRIMARY = intPreferencesKey("custom_primary")
    private val ON_PRIMARY = intPreferencesKey("custom_on_primary")
    private val PRIMARY_CONTAINER = intPreferencesKey("custom_primary_container")
    private val ON_PRIMARY_CONTAINER = intPreferencesKey("custom_on_primary_container")
    
    private val SECONDARY = intPreferencesKey("custom_secondary")
    private val ON_SECONDARY = intPreferencesKey("custom_on_secondary")
    private val SECONDARY_CONTAINER = intPreferencesKey("custom_secondary_container")
    private val ON_SECONDARY_CONTAINER = intPreferencesKey("custom_on_secondary_container")
    
    private val TERTIARY = intPreferencesKey("custom_tertiary")
    private val ON_TERTIARY = intPreferencesKey("custom_on_tertiary")
    private val TERTIARY_CONTAINER = intPreferencesKey("custom_tertiary_container")
    private val ON_TERTIARY_CONTAINER = intPreferencesKey("custom_on_tertiary_container")
    
    private val BACKGROUND = intPreferencesKey("custom_background")
    private val ON_BACKGROUND = intPreferencesKey("custom_on_background")
    private val SURFACE = intPreferencesKey("custom_surface")
    private val ON_SURFACE = intPreferencesKey("custom_on_surface")
    private val SURFACE_VARIANT = intPreferencesKey("custom_surface_variant")
    private val ON_SURFACE_VARIANT = intPreferencesKey("custom_on_surface_variant")
    
    private val ERROR = intPreferencesKey("custom_error")
    private val ON_ERROR = intPreferencesKey("custom_on_error")
    private val ERROR_CONTAINER = intPreferencesKey("custom_error_container")
    private val ON_ERROR_CONTAINER = intPreferencesKey("custom_on_error_container")
    
    private val OUTLINE = intPreferencesKey("custom_outline")
    private val OUTLINE_VARIANT = intPreferencesKey("custom_outline_variant")
    private val SURFACE_TINT = intPreferencesKey("custom_surface_tint")
    private val INVERSE_PRIMARY = intPreferencesKey("custom_inverse_primary")
    private val INVERSE_SURFACE = intPreferencesKey("custom_inverse_surface")
    private val INVERSE_ON_SURFACE = intPreferencesKey("custom_inverse_on_surface")
    private val SCRIM = intPreferencesKey("custom_scrim")
    
    // WALLPAPER (mantém igual)
    private val WALLPAPER_TYPE = stringPreferencesKey("wallpaper_type")
    private val WALLPAPER_COLOR = intPreferencesKey("wallpaper_color")
    private val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
    private val WALLPAPER_URL = stringPreferencesKey("wallpaper_url")
    private val WALLPAPER_BLUR = floatPreferencesKey("wallpaper_blur")
    private val WALLPAPER_DIM = floatPreferencesKey("wallpaper_dim")
}

    val customThemeConfig: Flow<CustomThemeConfig> = dataStore.data.map { prefs ->
    CustomThemeConfig(
        isEnabled = prefs[THEME_ENABLED] ?: false,
        
        primary = prefs[PRIMARY] ?: 0xFF6750A4.toInt(),
        onPrimary = prefs[ON_PRIMARY] ?: 0xFFFFFFFF.toInt(),
        primaryContainer = prefs[PRIMARY_CONTAINER] ?: 0xFFEADDFF.toInt(),
        onPrimaryContainer = prefs[ON_PRIMARY_CONTAINER] ?: 0xFF21005D.toInt(),
        
        secondary = prefs[SECONDARY] ?: 0xFF625B71.toInt(),
        onSecondary = prefs[ON_SECONDARY] ?: 0xFFFFFFFF.toInt(),
        secondaryContainer = prefs[SECONDARY_CONTAINER] ?: 0xFFE8DEF8.toInt(),
        onSecondaryContainer = prefs[ON_SECONDARY_CONTAINER] ?: 0xFF1D192B.toInt(),
        
        tertiary = prefs[TERTIARY] ?: 0xFF7D5260.toInt(),
        onTertiary = prefs[ON_TERTIARY] ?: 0xFFFFFFFF.toInt(),
        tertiaryContainer = prefs[TERTIARY_CONTAINER] ?: 0xFFFFD8E4.toInt(),
        onTertiaryContainer = prefs[ON_TERTIARY_CONTAINER] ?: 0xFF31111D.toInt(),
        
        background = prefs[BACKGROUND] ?: 0xFFFFFBFE.toInt(),
        onBackground = prefs[ON_BACKGROUND] ?: 0xFF1C1B1F.toInt(),
        surface = prefs[SURFACE] ?: 0xFFFFFBFE.toInt(),
        onSurface = prefs[ON_SURFACE] ?: 0xFF1C1B1F.toInt(),
        surfaceVariant = prefs[SURFACE_VARIANT] ?: 0xFFE7E0EC.toInt(),
        onSurfaceVariant = prefs[ON_SURFACE_VARIANT] ?: 0xFF49454F.toInt(),
        
        error = prefs[ERROR] ?: 0xFFB3261E.toInt(),
        onError = prefs[ON_ERROR] ?: 0xFFFFFFFF.toInt(),
        errorContainer = prefs[ERROR_CONTAINER] ?: 0xFFF9DEDC.toInt(),
        onErrorContainer = prefs[ON_ERROR_CONTAINER] ?: 0xFF410E0B.toInt(),
        
        outline = prefs[OUTLINE] ?: 0xFF79747E.toInt(),
        outlineVariant = prefs[OUTLINE_VARIANT] ?: 0xFFCAC4D0.toInt(),
        surfaceTint = prefs[SURFACE_TINT] ?: 0xFF6750A4.toInt(),
        inversePrimary = prefs[INVERSE_PRIMARY] ?: 0xFFD0BCFF.toInt(),
        inverseSurface = prefs[INVERSE_SURFACE] ?: 0xFF313033.toInt(),
        inverseOnSurface = prefs[INVERSE_ON_SURFACE] ?: 0xFFF4EFF4.toInt(),
        scrim = prefs[SCRIM] ?: 0xFF000000.toInt(),
        
        wallpaperType = try {
            val typeName = prefs[WALLPAPER_TYPE] ?: WallpaperType.SOLID.name
            WallpaperType.valueOf(typeName)
        } catch (_: Exception) {
            WallpaperType.SOLID
        },
        wallpaperColor = prefs[WALLPAPER_COLOR] ?: 0xFF1E1234.toInt(),
        wallpaperUri = prefs[WALLPAPER_URI],
        wallpaperUrl = prefs[WALLPAPER_URL],
        wallpaperBlur = prefs[WALLPAPER_BLUR] ?: 0.5f,
        wallpaperDim = prefs[WALLPAPER_DIM] ?: 0.3f
    )
}

    suspend fun setCustomTheme(config: CustomThemeConfig) {
    dataStore.edit { prefs ->
        prefs[THEME_ENABLED] = config.isEnabled
        
        prefs[PRIMARY] = config.primary
        prefs[ON_PRIMARY] = config.onPrimary
        prefs[PRIMARY_CONTAINER] = config.primaryContainer
        prefs[ON_PRIMARY_CONTAINER] = config.onPrimaryContainer
        
        prefs[SECONDARY] = config.secondary
        prefs[ON_SECONDARY] = config.onSecondary
        prefs[SECONDARY_CONTAINER] = config.secondaryContainer
        prefs[ON_SECONDARY_CONTAINER] = config.onSecondaryContainer
        
        prefs[TERTIARY] = config.tertiary
        prefs[ON_TERTIARY] = config.onTertiary
        prefs[TERTIARY_CONTAINER] = config.tertiaryContainer
        prefs[ON_TERTIARY_CONTAINER] = config.onTertiaryContainer
        
        prefs[BACKGROUND] = config.background
        prefs[ON_BACKGROUND] = config.onBackground
        prefs[SURFACE] = config.surface
        prefs[ON_SURFACE] = config.onSurface
        prefs[SURFACE_VARIANT] = config.surfaceVariant
        prefs[ON_SURFACE_VARIANT] = config.onSurfaceVariant
        
        prefs[ERROR] = config.error
        prefs[ON_ERROR] = config.onError
        prefs[ERROR_CONTAINER] = config.errorContainer
        prefs[ON_ERROR_CONTAINER] = config.onErrorContainer
        
        prefs[OUTLINE] = config.outline
        prefs[OUTLINE_VARIANT] = config.outlineVariant
        prefs[SURFACE_TINT] = config.surfaceTint
        prefs[INVERSE_PRIMARY] = config.inversePrimary
        prefs[INVERSE_SURFACE] = config.inverseSurface
        prefs[INVERSE_ON_SURFACE] = config.inverseOnSurface
        prefs[SCRIM] = config.scrim
        
        // WALLPAPER
        prefs[WALLPAPER_TYPE] = config.wallpaperType.name
        prefs[WALLPAPER_COLOR] = config.wallpaperColor
        prefs[WALLPAPER_URI] = config.wallpaperUri ?: ""
        prefs[WALLPAPER_URL] = config.wallpaperUrl ?: ""
        prefs[WALLPAPER_BLUR] = config.wallpaperBlur
        prefs[WALLPAPER_DIM] = config.wallpaperDim
    }
}

    suspend fun resetCustomTheme() {
    dataStore.edit { prefs ->
        prefs.remove(THEME_ENABLED)
        
        prefs.remove(PRIMARY)
        prefs.remove(ON_PRIMARY)
        prefs.remove(PRIMARY_CONTAINER)
        prefs.remove(ON_PRIMARY_CONTAINER)
        
        prefs.remove(SECONDARY)
        prefs.remove(ON_SECONDARY)
        prefs.remove(SECONDARY_CONTAINER)
        prefs.remove(ON_SECONDARY_CONTAINER)
        
        prefs.remove(TERTIARY)
        prefs.remove(ON_TERTIARY)
        prefs.remove(TERTIARY_CONTAINER)
        prefs.remove(ON_TERTIARY_CONTAINER)
        
        prefs.remove(BACKGROUND)
        prefs.remove(ON_BACKGROUND)
        prefs.remove(SURFACE)
        prefs.remove(ON_SURFACE)
        prefs.remove(SURFACE_VARIANT)
        prefs.remove(ON_SURFACE_VARIANT)
        
        prefs.remove(ERROR)
        prefs.remove(ON_ERROR)
        prefs.remove(ERROR_CONTAINER)
        prefs.remove(ON_ERROR_CONTAINER)
        
        prefs.remove(OUTLINE)
        prefs.remove(OUTLINE_VARIANT)
        prefs.remove(SURFACE_TINT)
        prefs.remove(INVERSE_PRIMARY)
        prefs.remove(INVERSE_SURFACE)
        prefs.remove(INVERSE_ON_SURFACE)
        prefs.remove(SCRIM)
        
        // WALLPAPER
        prefs.remove(WALLPAPER_TYPE)
        prefs.remove(WALLPAPER_COLOR)
        prefs.remove(WALLPAPER_URI)
        prefs.remove(WALLPAPER_URL)
        prefs.remove(WALLPAPER_BLUR)
        prefs.remove(WALLPAPER_DIM)
        }
    }
}