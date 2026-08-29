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

data class CustomThemeConfig(
    val isEnabled: Boolean = false,
    
    // ===== CORES EXISTENTES =====
    val primaryColor: Int = 0xFF6750A4.toInt(),
    val secondaryColor: Int = 0xFFF06292.toInt(),
    val backgroundColor: Int = 0xFF1E1234.toInt(),
    val onPrimaryColor: Int = 0xFFFFFFFF.toInt(),
    val onSurfaceColor: Int = 0xFFE1BEE7.toInt(),
    val accentColor: Int = 0xFFFF8A65.toInt(),
    
    // ===== NOVAS CORES =====
    val tertiaryColor: Int = 0xFF7D5260.toInt(),
    val onSecondaryColor: Int = 0xFFFFFFFF.toInt(),
    val secondaryContainerColor: Int = 0xFFE8DEF8.toInt(),
    val onSecondaryContainerColor: Int = 0xFF1D192B.toInt(),
    val tertiaryContainerColor: Int = 0xFFFFD8E4.toInt(),
    val onTertiaryContainerColor: Int = 0xFF31111D.toInt(),
    val onBackgroundColor: Int = 0xFF1C1B1F.toInt(),
    val surfaceColor: Int = 0xFFFFFBFE.toInt(),
    val surfaceVariantColor: Int = 0xFFE7E0EC.toInt(),
    val onSurfaceVariantColor: Int = 0xFF49454F.toInt(),
    val errorColor: Int = 0xFFB3261E.toInt(),
    val onErrorColor: Int = 0xFFFFFFFF.toInt(),
    val errorContainerColor: Int = 0xFFF9DEDC.toInt(),
    val onErrorContainerColor: Int = 0xFF410E0B.toInt(),
    val outlineColor: Int = 0xFF79747E.toInt(),
    val outlineVariantColor: Int = 0xFFCAC4D0.toInt(),
    val surfaceTintColor: Int = 0xFF6750A4.toInt(),
    val inversePrimaryColor: Int = 0xFFD0BCFF.toInt(),
    val inverseSurfaceColor: Int = 0xFF313033.toInt(),
    val inverseOnSurfaceColor: Int = 0xFFF4EFF4.toInt(),
    val scrimColor: Int = 0xFF000000.toInt(),
    
    // ===== NOVAS CORES QUE FALTAVAM =====
    val primaryContainerColor: Int = 0xFF6750A4.toInt(),
    val onPrimaryContainerColor: Int = 0xFFFFFFFF.toInt(),

    // ===== Containers =====
    val surfaceContainerColor: Int = 0xFF1E1E1E.toInt(),
    val surfaceContainerLowColor: Int = 0xFF1C1C1C.toInt(),
     val surfaceContainerHighColor: Int = 0xFF2A2A2A.toInt(),
    
    // ===== WALLPAPER =====
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
        // Chaves existentes
        private val THEME_ENABLED = booleanPreferencesKey("custom_theme_enabled")
        private val PRIMARY_COLOR = intPreferencesKey("custom_primary_color")
        private val SECONDARY_COLOR = intPreferencesKey("custom_secondary_color")
        private val SURFACE_CONTAINER_COLOR = intPreferencesKey("custom_surface_container_color")
        private val BACKGROUND_COLOR = intPreferencesKey("custom_background_color")
        private val ON_PRIMARY_COLOR = intPreferencesKey("custom_on_primary_color")
        private val ON_SURFACE_COLOR = intPreferencesKey("custom_on_surface_color")
        private val ACCENT_COLOR = intPreferencesKey("custom_accent_color")
        
        // Novas chaves
        private val TERTIARY_COLOR = intPreferencesKey("custom_tertiary_color")
        private val ON_SECONDARY_COLOR = intPreferencesKey("custom_on_secondary_color")
        private val SECONDARY_CONTAINER_COLOR = intPreferencesKey("custom_secondary_container_color")
        private val ON_SECONDARY_CONTAINER_COLOR = intPreferencesKey("custom_on_secondary_container_color")
        private val TERTIARY_CONTAINER_COLOR = intPreferencesKey("custom_tertiary_container_color")
        private val ON_TERTIARY_CONTAINER_COLOR = intPreferencesKey("custom_on_tertiary_container_color")
        private val ON_BACKGROUND_COLOR = intPreferencesKey("custom_on_background_color")
        private val SURFACE_COLOR = intPreferencesKey("custom_surface_color")
        private val SURFACE_VARIANT_COLOR = intPreferencesKey("custom_surface_variant_color")
        private val ON_SURFACE_VARIANT_COLOR = intPreferencesKey("custom_on_surface_variant_color")
        private val ERROR_COLOR = intPreferencesKey("custom_error_color")
        private val ON_ERROR_COLOR = intPreferencesKey("custom_on_error_color")
        private val ERROR_CONTAINER_COLOR = intPreferencesKey("custom_error_container_color")
        private val ON_ERROR_CONTAINER_COLOR = intPreferencesKey("custom_on_error_container_color")
        private val OUTLINE_COLOR = intPreferencesKey("custom_outline_color")
        private val OUTLINE_VARIANT_COLOR = intPreferencesKey("custom_outline_variant_color")
        private val SURFACE_TINT_COLOR = intPreferencesKey("custom_surface_tint_color")
        private val INVERSE_PRIMARY_COLOR = intPreferencesKey("custom_inverse_primary_color")
        private val INVERSE_SURFACE_COLOR = intPreferencesKey("custom_inverse_surface_color")
        private val INVERSE_ON_SURFACE_COLOR = intPreferencesKey("custom_inverse_on_surface_color")
        private val SCRIM_COLOR = intPreferencesKey("custom_scrim_color")
        private val SURFACE_CONTAINER_LOW_COLOR = intPreferencesKey("custom_surface_container_low_color")
        
        // NOVAS CHAVES QUE FALTAVAM
        private val PRIMARY_CONTAINER_COLOR = intPreferencesKey("custom_primary_container_color")
        private val ON_PRIMARY_CONTAINER_COLOR = intPreferencesKey("custom_on_primary_container_color")
        private val SURFACE_CONTAINER_HIGH_COLOR = intPreferencesKey("custom_surface_container_high_color")
        
        // Wallpaper
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
            primaryColor = prefs[PRIMARY_COLOR] ?: 0xFF6750A4.toInt(),
            secondaryColor = prefs[SECONDARY_COLOR] ?: 0xFFF06292.toInt(),
            backgroundColor = prefs[BACKGROUND_COLOR] ?: 0xFF1E1234.toInt(),
            onPrimaryColor = prefs[ON_PRIMARY_COLOR] ?: 0xFFFFFFFF.toInt(),
            onSurfaceColor = prefs[ON_SURFACE_COLOR] ?: 0xFFE1BEE7.toInt(),
            accentColor = prefs[ACCENT_COLOR] ?: 0xFFFF8A65.toInt(),
            surfaceContainerColor = prefs[SURFACE_CONTAINER_COLOR] ?: 0xFF1E1E1E.toInt(),
            tertiaryColor = prefs[TERTIARY_COLOR] ?: 0xFF7D5260.toInt(),
            onSecondaryColor = prefs[ON_SECONDARY_COLOR] ?: 0xFFFFFFFF.toInt(),
            secondaryContainerColor = prefs[SECONDARY_CONTAINER_COLOR] ?: 0xFFE8DEF8.toInt(),
            onSecondaryContainerColor = prefs[ON_SECONDARY_CONTAINER_COLOR] ?: 0xFF1D192B.toInt(),
            tertiaryContainerColor = prefs[TERTIARY_CONTAINER_COLOR] ?: 0xFFFFD8E4.toInt(),
            onTertiaryContainerColor = prefs[ON_TERTIARY_CONTAINER_COLOR] ?: 0xFF31111D.toInt(),
            onBackgroundColor = prefs[ON_BACKGROUND_COLOR] ?: 0xFF1C1B1F.toInt(),
            surfaceColor = prefs[SURFACE_COLOR] ?: 0xFFFFFBFE.toInt(),
            surfaceVariantColor = prefs[SURFACE_VARIANT_COLOR] ?: 0xFFE7E0EC.toInt(),
            onSurfaceVariantColor = prefs[ON_SURFACE_VARIANT_COLOR] ?: 0xFF49454F.toInt(),
            errorColor = prefs[ERROR_COLOR] ?: 0xFFB3261E.toInt(),
            onErrorColor = prefs[ON_ERROR_COLOR] ?: 0xFFFFFFFF.toInt(),
            errorContainerColor = prefs[ERROR_CONTAINER_COLOR] ?: 0xFFF9DEDC.toInt(),
            onErrorContainerColor = prefs[ON_ERROR_CONTAINER_COLOR] ?: 0xFF410E0B.toInt(),
            outlineColor = prefs[OUTLINE_COLOR] ?: 0xFF79747E.toInt(),
            outlineVariantColor = prefs[OUTLINE_VARIANT_COLOR] ?: 0xFFCAC4D0.toInt(),
            surfaceTintColor = prefs[SURFACE_TINT_COLOR] ?: 0xFF6750A4.toInt(),
            inversePrimaryColor = prefs[INVERSE_PRIMARY_COLOR] ?: 0xFFD0BCFF.toInt(),
            inverseSurfaceColor = prefs[INVERSE_SURFACE_COLOR] ?: 0xFF313033.toInt(),
            inverseOnSurfaceColor = prefs[INVERSE_ON_SURFACE_COLOR] ?: 0xFFF4EFF4.toInt(),
            scrimColor = prefs[SCRIM_COLOR] ?: 0xFF000000.toInt(),
            surfaceContainerLowColor = prefs[SURFACE_CONTAINER_LOW_COLOR] ?: 0xFF1C1C1C.toInt(),
            surfaceContainerHighColor = prefs[SURFACE_CONTAINER_HIGH_COLOR] ?: 0xFF2A2A2A.toInt(),
            
            // NOVAS CORES QUE FALTAVAM
            primaryContainerColor = prefs[PRIMARY_CONTAINER_COLOR] ?: 0xFF6750A4.toInt(),
            onPrimaryContainerColor = prefs[ON_PRIMARY_CONTAINER_COLOR] ?: 0xFFFFFFFF.toInt(),
            
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
            prefs[PRIMARY_COLOR] = config.primaryColor
            prefs[SECONDARY_COLOR] = config.secondaryColor
            prefs[BACKGROUND_COLOR] = config.backgroundColor
            prefs[ON_PRIMARY_COLOR] = config.onPrimaryColor
            prefs[ON_SURFACE_COLOR] = config.onSurfaceColor
            prefs[ACCENT_COLOR] = config.accentColor
            prefs[SURFACE_CONTAINER_COLOR] = config.surfaceContainerColor
            prefs[TERTIARY_COLOR] = config.tertiaryColor
            prefs[ON_SECONDARY_COLOR] = config.onSecondaryColor
            prefs[SECONDARY_CONTAINER_COLOR] = config.secondaryContainerColor
            prefs[ON_SECONDARY_CONTAINER_COLOR] = config.onSecondaryContainerColor
            prefs[TERTIARY_CONTAINER_COLOR] = config.tertiaryContainerColor
            prefs[ON_TERTIARY_CONTAINER_COLOR] = config.onTertiaryContainerColor
            prefs[ON_BACKGROUND_COLOR] = config.onBackgroundColor
            prefs[SURFACE_COLOR] = config.surfaceColor
            prefs[SURFACE_VARIANT_COLOR] = config.surfaceVariantColor
            prefs[ON_SURFACE_VARIANT_COLOR] = config.onSurfaceVariantColor
            prefs[ERROR_COLOR] = config.errorColor
            prefs[ON_ERROR_COLOR] = config.onErrorColor
            prefs[ERROR_CONTAINER_COLOR] = config.errorContainerColor
            prefs[ON_ERROR_CONTAINER_COLOR] = config.onErrorContainerColor
            prefs[OUTLINE_COLOR] = config.outlineColor
            prefs[OUTLINE_VARIANT_COLOR] = config.outlineVariantColor
            prefs[SURFACE_TINT_COLOR] = config.surfaceTintColor
            prefs[INVERSE_PRIMARY_COLOR] = config.inversePrimaryColor
            prefs[INVERSE_SURFACE_COLOR] = config.inverseSurfaceColor
            prefs[INVERSE_ON_SURFACE_COLOR] = config.inverseOnSurfaceColor
            prefs[SCRIM_COLOR] = config.scrimColor
            prefs[SURFACE_CONTAINER_LOW_COLOR] = config.surfaceContainerLowColor
            prefs[SURFACE_CONTAINER_HIGH_COLOR] = config.surfaceContainerHighColor
            
            // NOVAS CORES QUE FALTAVAM
            prefs[PRIMARY_CONTAINER_COLOR] = config.primaryContainerColor
            prefs[ON_PRIMARY_CONTAINER_COLOR] = config.onPrimaryContainerColor
            
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
            prefs.remove(PRIMARY_COLOR)
            prefs.remove(SECONDARY_COLOR)
            prefs.remove(BACKGROUND_COLOR)
            prefs.remove(ON_PRIMARY_COLOR)
            prefs.remove(ON_SURFACE_COLOR)
            prefs.remove(ACCENT_COLOR)
            prefs.remove(SURFACE_CONTAINER_COLOR)
            prefs.remove(TERTIARY_COLOR)
            prefs.remove(ON_SECONDARY_COLOR)
            prefs.remove(SECONDARY_CONTAINER_COLOR)
            prefs.remove(ON_SECONDARY_CONTAINER_COLOR)
            prefs.remove(TERTIARY_CONTAINER_COLOR)
            prefs.remove(ON_TERTIARY_CONTAINER_COLOR)
            prefs.remove(ON_BACKGROUND_COLOR)
            prefs.remove(SURFACE_COLOR)
            prefs.remove(SURFACE_VARIANT_COLOR)
            prefs.remove(ON_SURFACE_VARIANT_COLOR)
            prefs.remove(ERROR_COLOR)
            prefs.remove(ON_ERROR_COLOR)
            prefs.remove(ERROR_CONTAINER_COLOR)
            prefs.remove(ON_ERROR_CONTAINER_COLOR)
            prefs.remove(OUTLINE_COLOR)
            prefs.remove(OUTLINE_VARIANT_COLOR)
            prefs.remove(SURFACE_TINT_COLOR)
            prefs.remove(INVERSE_PRIMARY_COLOR)
            prefs.remove(INVERSE_SURFACE_COLOR)
            prefs.remove(INVERSE_ON_SURFACE_COLOR)
            prefs.remove(SCRIM_COLOR)
            prefs.remove(SURFACE_CONTAINER_LOW_COLOR)
            prefs.remove(SURFACE_CONTAINER_HIGH_COLOR)
            
            // NOVAS CORES QUE FALTAVAM
            prefs.remove(PRIMARY_CONTAINER_COLOR)
            prefs.remove(ON_PRIMARY_CONTAINER_COLOR)
            
            prefs.remove(WALLPAPER_TYPE)
            prefs.remove(WALLPAPER_COLOR)
            prefs.remove(WALLPAPER_URI)
            prefs.remove(WALLPAPER_URL)
            prefs.remove(WALLPAPER_BLUR)
            prefs.remove(WALLPAPER_DIM)
        }
    }
}