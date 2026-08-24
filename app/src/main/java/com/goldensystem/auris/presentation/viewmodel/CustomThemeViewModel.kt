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
    
// Funções existentes (MANTENHA)
fun updatePrimaryColor(color: Int) { _config.update { it.copy(primaryColor = color) } }
fun updateSecondaryColor(color: Int) { _config.update { it.copy(secondaryColor = color) } }
fun updateBackgroundColor(color: Int) { _config.update { it.copy(backgroundColor = color) } }
fun updateOnPrimaryColor(color: Int) { _config.update { it.copy(onPrimaryColor = color) } }
fun updateOnSurfaceColor(color: Int) { _config.update { it.copy(onSurfaceColor = color) } }
fun updateAccentColor(color: Int) { _config.update { it.copy(accentColor = color) } }

// Novas funções (ADICIONE)
fun updateTertiaryColor(color: Int) { _config.update { it.copy(tertiaryColor = color) } }
fun updateOnSecondaryColor(color: Int) { _config.update { it.copy(onSecondaryColor = color) } }
fun updateSecondaryContainerColor(color: Int) { _config.update { it.copy(secondaryContainerColor = color) } }
fun updateOnSecondaryContainerColor(color: Int) { _config.update { it.copy(onSecondaryContainerColor = color) } }
fun updateTertiaryContainerColor(color: Int) { _config.update { it.copy(tertiaryContainerColor = color) } }
fun updateOnTertiaryContainerColor(color: Int) { _config.update { it.copy(onTertiaryContainerColor = color) } }
fun updateOnBackgroundColor(color: Int) { _config.update { it.copy(onBackgroundColor = color) } }
fun updateSurfaceColor(color: Int) { _config.update { it.copy(surfaceColor = color) } }
fun updateSurfaceVariantColor(color: Int) { _config.update { it.copy(surfaceVariantColor = color) } }
fun updateOnSurfaceVariantColor(color: Int) { _config.update { it.copy(onSurfaceVariantColor = color) } }
fun updateErrorColor(color: Int) { _config.update { it.copy(errorColor = color) } }
fun updateOnErrorColor(color: Int) { _config.update { it.copy(onErrorColor = color) } }
fun updateErrorContainerColor(color: Int) { _config.update { it.copy(errorContainerColor = color) } }
fun updateOnErrorContainerColor(color: Int) { _config.update { it.copy(onErrorContainerColor = color) } }
fun updateOutlineColor(color: Int) { _config.update { it.copy(outlineColor = color) } }
fun updateOutlineVariantColor(color: Int) { _config.update { it.copy(outlineVariantColor = color) } }
fun updateSurfaceTintColor(color: Int) { _config.update { it.copy(surfaceTintColor = color) } }
fun updateInversePrimaryColor(color: Int) { _config.update { it.copy(inversePrimaryColor = color) } }
fun updateInverseSurfaceColor(color: Int) { _config.update { it.copy(inverseSurfaceColor = color) } }
fun updateInverseOnSurfaceColor(color: Int) { _config.update { it.copy(inverseOnSurfaceColor = color) } }
fun updateScrimColor(color: Int) { _config.update { it.copy(scrimColor = color) } }

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