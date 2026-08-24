// presentation/viewmodel/CustomThemeViewModel.kt
package com.goldensystem.auris.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldensystem.auris.data.preferences.CustomThemeConfig
import com.goldensystem.auris.data.preferences.ThemePreferences
import com.goldensystem.auris.data.preferences.WallpaperType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class CustomThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _config = MutableStateFlow(CustomThemeConfig())
    val customThemeConfig: StateFlow<CustomThemeConfig> = _config.asStateFlow()

    init {
        viewModelScope.launch {
            themePreferences.customThemeConfig.collect { config ->
                _config.value = config
            }
        }
    }
    
    // CustomThemeViewModel.kt - Adicione funções para cada cor
fun updatePrimary(color: Int) { _config.update { it.copy(primary = color) } }
fun updateOnPrimary(color: Int) { _config.update { it.copy(onPrimary = color) } }
fun updatePrimaryContainer(color: Int) { _config.update { it.copy(primaryContainer = color) } }
fun updateOnPrimaryContainer(color: Int) { _config.update { it.copy(onPrimaryContainer = color) } }

fun updateSecondary(color: Int) { _config.update { it.copy(secondary = color) } }
fun updateOnSecondary(color: Int) { _config.update { it.copy(onSecondary = color) } }
fun updateSecondaryContainer(color: Int) { _config.update { it.copy(secondaryContainer = color) } }
fun updateOnSecondaryContainer(color: Int) { _config.update { it.copy(onSecondaryContainer = color) } }

fun updateTertiary(color: Int) { _config.update { it.copy(tertiary = color) } }
fun updateOnTertiary(color: Int) { _config.update { it.copy(onTertiary = color) } }
fun updateTertiaryContainer(color: Int) { _config.update { it.copy(tertiaryContainer = color) } }
fun updateOnTertiaryContainer(color: Int) { _config.update { it.copy(onTertiaryContainer = color) } }

fun updateBackground(color: Int) { _config.update { it.copy(background = color) } }
fun updateOnBackground(color: Int) { _config.update { it.copy(onBackground = color) } }
fun updateSurface(color: Int) { _config.update { it.copy(surface = color) } }
fun updateOnSurface(color: Int) { _config.update { it.copy(onSurface = color) } }
fun updateSurfaceVariant(color: Int) { _config.update { it.copy(surfaceVariant = color) } }
fun updateOnSurfaceVariant(color: Int) { _config.update { it.copy(onSurfaceVariant = color) } }

fun updateError(color: Int) { _config.update { it.copy(error = color) } }
fun updateOnError(color: Int) { _config.update { it.copy(onError = color) } }
fun updateErrorContainer(color: Int) { _config.update { it.copy(errorContainer = color) } }
fun updateOnErrorContainer(color: Int) { _config.update { it.copy(onErrorContainer = color) } }

fun updateOutline(color: Int) { _config.update { it.copy(outline = color) } }
fun updateOutlineVariant(color: Int) { _config.update { it.copy(outlineVariant = color) } }
fun updateSurfaceTint(color: Int) { _config.update { it.copy(surfaceTint = color) } }
fun updateInversePrimary(color: Int) { _config.update { it.copy(inversePrimary = color) } }
fun updateInverseSurface(color: Int) { _config.update { it.copy(inverseSurface = color) } }
fun updateInverseOnSurface(color: Int) { _config.update { it.copy(inverseOnSurface = color) } }
fun updateScrim(color: Int) { _config.update { it.copy(scrim = color) } }

    // ===== CORES PRINCIPAIS =====
    fun updatePrimaryColor(color: Int) {
        _config.update { it.copy(primaryColor = color) }
    }
    fun updateSecondaryColor(color: Int) {
        _config.update { it.copy(secondaryColor = color) }
    }
    fun updateBackgroundColor(color: Int) {
        _config.update { it.copy(backgroundColor = color) }
    }
    fun updateOnPrimaryColor(color: Int) {
        _config.update { it.copy(onPrimaryColor = color) }
    }
    fun updateOnSurfaceColor(color: Int) {
        _config.update { it.copy(onSurfaceColor = color) }
    }
    
    // ===== COR DE DETALHES (ACENTO) =====
    fun updateAccentColor(color: Int) {
        _config.update { it.copy(accentColor = color) }  // 👈 UMA COR PARA TUDO!
    }

    // ===== WALLPAPER =====
    fun setWallpaperType(type: WallpaperType) {
        _config.update { it.copy(wallpaperType = type) }
    }
    fun setWallpaperColor(color: Int) {
        _config.update { it.copy(wallpaperColor = color) }
    }
    fun setWallpaperFromGallery(uri: String) {
        _config.update { 
            it.copy(
                wallpaperType = WallpaperType.GALLERY,
                wallpaperUri = uri
            )
        }
    }
    fun setWallpaperFromServer(url: String) {
        _config.update { 
            it.copy(
                wallpaperType = WallpaperType.SERVER,
                wallpaperUrl = url
            )
        }
    }
    fun setWallpaperBlur(blur: Float) {
        _config.update { it.copy(wallpaperBlur = blur) }
    }
    fun setWallpaperDim(dim: Float) {
        _config.update { it.copy(wallpaperDim = dim) }
    }
    
    suspend fun disableCustomTheme() {
        themePreferences.setCustomTheme(_config.value.copy(isEnabled = false))
        _config.value = _config.value.copy(isEnabled = false)
    }

    suspend fun saveCustomTheme() {
        themePreferences.setCustomTheme(_config.value.copy(isEnabled = true))
    }

    suspend fun resetToDefault() {
        themePreferences.resetCustomTheme()
        _config.value = CustomThemeConfig()
        themePreferences.customThemeConfig.collect { config ->
            _config.value = config
        }
    }

    fun saveWallpaperFromGallery(uriString: String) {
        viewModelScope.launch {
            try {
                val uri = Uri.parse(uriString)
                val inputStream = context.contentResolver.openInputStream(uri)
                
                if (inputStream != null) {
                    val cacheDir = File(context.cacheDir, "wallpapers")
                    if (!cacheDir.exists()) cacheDir.mkdirs()
                    
                    val outputFile = File(cacheDir, "wallpaper_${System.currentTimeMillis()}.jpg")
                    
                    FileOutputStream(outputFile).use { output ->
                        inputStream.copyTo(output)
                    }
                    
                    _config.update { currentConfig ->
                        currentConfig.copy(
                            wallpaperUri = outputFile.absolutePath,
                            wallpaperType = WallpaperType.GALLERY
                        )
                    }
                    
                    themePreferences.setCustomTheme(_config.value)
                    
                    Log.d("CustomTheme", "Wallpaper salvo em: ${outputFile.absolutePath}")
                }
            } catch (e: Exception) {
                Log.e("CustomTheme", "Erro ao salvar wallpaper: ${e.message}")
            }
        }
    }

    fun resetWallpaper() {
        _config.update { currentConfig ->
            currentConfig.copy(
                wallpaperUri = null,
                wallpaperType = WallpaperType.SOLID
            )
        }
        viewModelScope.launch {
            themePreferences.setCustomTheme(_config.value)
        }
    }
}