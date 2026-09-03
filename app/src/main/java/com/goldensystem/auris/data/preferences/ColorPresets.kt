// data/preferences/ColorPresets.kt

package com.goldensystem.auris.data.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.goldensystem.auris.R

// ============================================================
// PREDEFINIÇÕES DE CORES
// ============================================================

data class ColorPreset(
    val nameResId: Int,  // ← USA R.string.cor_xxx
    val icon: ImageVector,

    // Cores para tema escuro
    val darkPrimaryColor: Int,
    val darkSecondaryColor: Int,
    val darkBackgroundColor: Int,
    val darkOnPrimaryColor: Int,
    val darkOnSurfaceColor: Int,
    val darkAccentColor: Int,
    val darkSurfaceContainerColor: Int,
    val darkSurfaceContainerLowColor: Int,
    val darkSurfaceContainerHighColor: Int,
    val darkSurfaceContainerLowestColor: Int,

    // Cores para tema claro
    val lightPrimaryColor: Int,
    val lightSecondaryColor: Int,
    val lightBackgroundColor: Int,
    val lightOnPrimaryColor: Int,
    val lightOnSurfaceColor: Int,
    val lightAccentColor: Int,
    val lightSurfaceContainerColor: Int,
    val lightSurfaceContainerLowColor: Int,
    val lightSurfaceContainerHighColor: Int,
    val lightSurfaceContainerLowestColor: Int
) {
    fun getColors(isDark: Boolean): PresetColors {
        return if (isDark) {
            PresetColors(
                primaryColor = darkPrimaryColor,
                secondaryColor = darkSecondaryColor,
                backgroundColor = darkBackgroundColor,
                onPrimaryColor = darkOnPrimaryColor,
                onSurfaceColor = darkOnSurfaceColor,
                accentColor = darkAccentColor,
                surfaceContainerColor = darkSurfaceContainerColor,
                surfaceContainerLowColor = darkSurfaceContainerLowColor,
                surfaceContainerHighColor = darkSurfaceContainerHighColor,
                surfaceContainerLowestColor = darkSurfaceContainerLowestColor
            )
        } else {
            PresetColors(
                primaryColor = lightPrimaryColor,
                secondaryColor = lightSecondaryColor,
                backgroundColor = lightBackgroundColor,
                onPrimaryColor = lightOnPrimaryColor,
                onSurfaceColor = lightOnSurfaceColor,
                accentColor = lightAccentColor,
                surfaceContainerColor = lightSurfaceContainerColor,
                surfaceContainerLowColor = lightSurfaceContainerLowColor,
                surfaceContainerHighColor = lightSurfaceContainerHighColor,
                surfaceContainerLowestColor = lightSurfaceContainerLowestColor
            )
        }
    }
}

data class PresetColors(
    val primaryColor: Int,
    val secondaryColor: Int,
    val backgroundColor: Int,
    val onPrimaryColor: Int,
    val onSurfaceColor: Int,
    val accentColor: Int,
    val surfaceContainerColor: Int,
    val surfaceContainerLowColor: Int,
    val surfaceContainerHighColor: Int,
    val surfaceContainerLowestColor: Int
)

val COLOR_PRESETS = listOf(

    // AZUL
    ColorPreset(
        nameResId = R.string.cor_blue,
        icon = Icons.Rounded.WaterDrop,
        darkPrimaryColor = 0xFF0D5AA1.toInt(),
        darkSecondaryColor = 0xFF1E88E5.toInt(),
        darkBackgroundColor = 0xFF00101C.toInt(),
        darkOnPrimaryColor = 0xFFFFFFFF.toInt(),
        darkOnSurfaceColor = 0xFF1B1EDA.toInt(),
        darkAccentColor = 0xFF2979FF.toInt(),
        darkSurfaceContainerColor = 0xFF001E35.toInt(),
        darkSurfaceContainerLowColor = 0xFF00101C.toInt(),
        darkSurfaceContainerHighColor = 0xFF002C4A.toInt(),
        darkSurfaceContainerLowestColor = 0xFF000A14.toInt(),
        lightPrimaryColor = 0xFF608DC0.toInt(),
        lightSecondaryColor = 0xFF1976D2.toInt(),
        lightBackgroundColor = 0xFFF5F9FF.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF0D47A1.toInt(),
        lightAccentColor = 0xFF2979FF.toInt(),
        lightSurfaceContainerColor = 0xFFE8F1FC.toInt(),
        lightSurfaceContainerLowColor = 0xFFF5F9FF.toInt(),
        lightSurfaceContainerHighColor = 0xFFD6E7FA.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),

    // VERMELHO
    ColorPreset(
        nameResId = R.string.cor_red,
        icon = Icons.Rounded.Whatshot,
        darkPrimaryColor = 0xFFD32F2F.toInt(),
        darkSecondaryColor = 0xFFF44336.toInt(),
        darkBackgroundColor = 0xFF0E0000.toInt(),
        darkOnPrimaryColor = 0xFFFFFFFF.toInt(),
        darkOnSurfaceColor = 0xFFD32F2F.toInt(),
        darkAccentColor = 0xFFFF1744.toInt(),
        darkSurfaceContainerColor = 0xFF1F0000.toInt(),
        darkSurfaceContainerLowColor = 0xFF0E0000.toInt(),
        darkSurfaceContainerHighColor = 0xFF2F0000.toInt(),
        darkSurfaceContainerLowestColor = 0xFF070000.toInt(),
        lightPrimaryColor = 0xFFFF7E7E.toInt(),
        lightSecondaryColor = 0xFFD32F2F.toInt(),
        lightBackgroundColor = 0xFFFFF5F5.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFFFF0000.toInt(),
        lightAccentColor = 0xFFFF1744.toInt(),
        lightSurfaceContainerColor = 0xFFFCE8E8.toInt(),
        lightSurfaceContainerLowColor = 0xFFFFF5F5.toInt(),
        lightSurfaceContainerHighColor = 0xFFF6D6D6.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),

    // CIANO
    ColorPreset(
        nameResId = R.string.cor_cyan,
        icon = Icons.Rounded.Waves,
        darkPrimaryColor = 0xFF00F5FF.toInt(),
        darkSecondaryColor = 0xFF00BCD4.toInt(),
        darkBackgroundColor = 0xFF00090A.toInt(),
        darkOnPrimaryColor = 0xFFD5FBFF.toInt(),
        darkOnSurfaceColor = 0xFF00F5FF.toInt(),
        darkAccentColor = 0xFF00E5FF.toInt(),
        darkSurfaceContainerColor = 0xFF001417.toInt(),
        darkSurfaceContainerLowColor = 0xFF00090A.toInt(),
        darkSurfaceContainerHighColor = 0xFF002025.toInt(),
        darkSurfaceContainerLowestColor = 0xFF000406.toInt(),
        lightPrimaryColor = 0xFF65C8D3.toInt(),
        lightSecondaryColor = 0xFF0097A7.toInt(),
        lightBackgroundColor = 0xFFF2FCFD.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF009BB2.toInt(),
        lightAccentColor = 0xFF00B8D4.toInt(),
        lightSurfaceContainerColor = 0xFFE0F5F7.toInt(),
        lightSurfaceContainerLowColor = 0xFFF2FCFD.toInt(),
        lightSurfaceContainerHighColor = 0xFFCBECEF.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),

    // VERDE
    ColorPreset(
        nameResId = R.string.cor_green,
        icon = Icons.Rounded.Park,
        darkPrimaryColor = 0xFF2E7D61.toInt(),
        darkSecondaryColor = 0xFF43A047.toInt(),
        darkBackgroundColor = 0xFF000E00.toInt(),
        darkOnPrimaryColor = 0xFFFFFFFF.toInt(),
        darkOnSurfaceColor = 0xFF2E7D49.toInt(),
        darkAccentColor = 0xFF00E676.toInt(),
        darkSurfaceContainerColor = 0xFF001A00.toInt(),
        darkSurfaceContainerLowColor = 0xFF000E00.toInt(),
        darkSurfaceContainerHighColor = 0xFF002600.toInt(),
        darkSurfaceContainerLowestColor = 0xFF000700.toInt(),
        lightPrimaryColor = 0xFF7BDD80.toInt(),
        lightSecondaryColor = 0xFF388E3C.toInt(),
        lightBackgroundColor = 0xFFF4FBF5.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF31AC3A.toInt(),
        lightAccentColor = 0xFF00A152.toInt(),
        lightSurfaceContainerColor = 0xFFE3F3E5.toInt(),
        lightSurfaceContainerLowColor = 0xFFF4FBF5.toInt(),
        lightSurfaceContainerHighColor = 0xFFD1EAD4.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),

    // PRETO
    ColorPreset(
        nameResId = R.string.cor_black,
        icon = Icons.Rounded.DarkMode,
        darkPrimaryColor = 0xFF4D4D4D.toInt(),
        darkSecondaryColor = 0xFF424242.toInt(),
        darkBackgroundColor = 0xFF000000.toInt(),
        darkOnPrimaryColor = 0xFFFFFFFF.toInt(),
        darkOnSurfaceColor = 0xFFFFFFFF.toInt(),
        darkAccentColor = 0xFF757575.toInt(),
        darkSurfaceContainerColor = 0xFF1A1A1A.toInt(),
        darkSurfaceContainerLowColor = 0xFF000000.toInt(),
        darkSurfaceContainerHighColor = 0xFF2A2A2A.toInt(),
        darkSurfaceContainerLowestColor = 0xFF000000.toInt(),
        lightPrimaryColor = 0xFF828282.toInt(),
        lightSecondaryColor = 0xFF616161.toInt(),
        lightBackgroundColor = 0xFFF7F7F7.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF212121.toInt(),
        lightAccentColor = 0xFF616161.toInt(),
        lightSurfaceContainerColor = 0xFFEAEAEA.toInt(),
        lightSurfaceContainerLowColor = 0xFFF7F7F7.toInt(),
        lightSurfaceContainerHighColor = 0xFFDDDDDD.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),

    // ROSA
    ColorPreset(
        nameResId = R.string.cor_pink,
        icon = Icons.Rounded.Favorite,
        darkPrimaryColor = 0xFFE91E63.toInt(),
        darkSecondaryColor = 0xFFE91E63.toInt(),
        darkBackgroundColor = 0xFF2A000E.toInt(),
        darkOnPrimaryColor = 0xFFFFFFFF.toInt(),
        darkOnSurfaceColor = 0xFFE91E63.toInt(),
        darkAccentColor = 0xFFFF4081.toInt(),
        darkSurfaceContainerColor = 0xFF3D0015.toInt(),
        darkSurfaceContainerLowColor = 0xFF2A000E.toInt(),
        darkSurfaceContainerHighColor = 0xFF4A001A.toInt(),
        darkSurfaceContainerLowestColor = 0xFF150007.toInt(),
        lightPrimaryColor = 0xFFFF7EB1.toInt(),
        lightSecondaryColor = 0xFFD81B60.toInt(),
        lightBackgroundColor = 0xFFFFF4F8.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFFBA136C.toInt(),
        lightAccentColor = 0xFFFF4081.toInt(),
        lightSurfaceContainerColor = 0xFFFBE3EC.toInt(),
        lightSurfaceContainerLowColor = 0xFFFFF4F8.toInt(),
        lightSurfaceContainerHighColor = 0xFFF4D0DE.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),

    // ROXO
    ColorPreset(
        nameResId = R.string.cor_purple,
        icon = Icons.Rounded.AutoAwesome,
        darkPrimaryColor = 0xFF7B1FA2.toInt(),
        darkSecondaryColor = 0xFFAB47BC.toInt(),
        darkBackgroundColor = 0xFF150018.toInt(),
        darkOnPrimaryColor = 0xFFFFFFFF.toInt(),
        darkOnSurfaceColor = 0xFF7B1FA2.toInt(),
        darkAccentColor = 0xFFD500F9.toInt(),
        darkSurfaceContainerColor = 0xFF25002D.toInt(),
        darkSurfaceContainerLowColor = 0xFF150018.toInt(),
        darkSurfaceContainerHighColor = 0xFF350042.toInt(),
        darkSurfaceContainerLowestColor = 0xFF0A000C.toInt(),
        lightPrimaryColor = 0xFF884BA2.toInt(),
        lightSecondaryColor = 0xFF8E24AA.toInt(),
        lightBackgroundColor = 0xFFFBF5FC.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF77148C.toInt(),
        lightAccentColor = 0xFFAA00FF.toInt(),
        lightSurfaceContainerColor = 0xFFF0E3F4.toInt(),
        lightSurfaceContainerLowColor = 0xFFFBF5FC.toInt(),
        lightSurfaceContainerHighColor = 0xFFE4D1E9.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),

    // AMARELO
    ColorPreset(
        nameResId = R.string.cor_yellow,
        icon = Icons.Rounded.WbSunny,
        darkPrimaryColor = 0xFFF9A825.toInt(),
        darkSecondaryColor = 0xFFFFD54F.toInt(),
        darkBackgroundColor = 0xFF090900.toInt(),
        darkOnPrimaryColor = 0xFFFFFEBF.toInt(),
        darkOnSurfaceColor = 0xFFF9A825.toInt(),
        darkAccentColor = 0xFFFFAB00.toInt(),
        darkSurfaceContainerColor = 0xFF141400.toInt(),
        darkSurfaceContainerLowColor = 0xFF090900.toInt(),
        darkSurfaceContainerHighColor = 0xFF1F1F00.toInt(),
        darkSurfaceContainerLowestColor = 0xFF040400.toInt(),
        lightPrimaryColor = 0xFFFFF489.toInt(),
        lightSecondaryColor = 0xFFF9A225.toInt(),
        lightBackgroundColor = 0xFFFFFCF2.toInt(),
        lightOnPrimaryColor = 0xFF3E3600.toInt(),
        lightOnSurfaceColor = 0xFF5D4C00.toInt(),
        lightAccentColor = 0xFFFFD200.toInt(),
        lightSurfaceContainerColor = 0xFFFFF3CF.toInt(),
        lightSurfaceContainerLowColor = 0xFFFFFCF2.toInt(),
        lightSurfaceContainerHighColor = 0xFFFFE9A8.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),

    // LARANJA
    ColorPreset(
        nameResId = R.string.cor_orange,
        icon = Icons.Rounded.Whatshot,
        darkPrimaryColor = 0xFFE65100.toInt(),
        darkSecondaryColor = 0xFFFF9800.toInt(),
        darkBackgroundColor = 0xFF0F0A00.toInt(),
        darkOnPrimaryColor = 0xFFFFDDB3.toInt(),
        darkOnSurfaceColor = 0xFFE65100.toInt(),
        darkAccentColor = 0xFFFF6E40.toInt(),
        darkSurfaceContainerColor = 0xFF1F1400.toInt(),
        darkSurfaceContainerLowColor = 0xFF0F0A00.toInt(),
        darkSurfaceContainerHighColor = 0xFF2F1E00.toInt(),
        darkSurfaceContainerLowestColor = 0xFF080500.toInt(),
        lightPrimaryColor = 0xFFE6986E.toInt(),
        lightSecondaryColor = 0xFFEF6C00.toInt(),
        lightBackgroundColor = 0xFFFFF8F3.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF8D3200.toInt(),
        lightAccentColor = 0xFFFF5722.toInt(),
        lightSurfaceContainerColor = 0xFFFCE9DD.toInt(),
        lightSurfaceContainerLowColor = 0xFFFFF8F3.toInt(),
        lightSurfaceContainerHighColor = 0xFFF7D7C2.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),

    // NEON
    ColorPreset(
        nameResId = R.string.cor_neon,
        icon = Icons.Rounded.Bolt,
        darkPrimaryColor = 0xFF00FF84.toInt(),
        darkSecondaryColor = 0xFF00E2FF.toInt(),
        darkBackgroundColor = 0xFF1A1A1A.toInt(),
        darkOnPrimaryColor = 0xFFFFF7FF.toInt(),
        darkOnSurfaceColor = 0xFF00FFE2.toInt(),
        darkAccentColor = 0xFFFF4081.toInt(),
        darkSurfaceContainerColor = 0xFF2A2A2A.toInt(),
        darkSurfaceContainerLowColor = 0xFF1A1A1A.toInt(),
        darkSurfaceContainerHighColor = 0xFF3A3A3A.toInt(),
        darkSurfaceContainerLowestColor = 0xFF0D0D0D.toInt(),
        lightPrimaryColor = 0xFF8EFFFE.toInt(),
        lightSecondaryColor = 0xFF008FA3.toInt(),
        lightBackgroundColor = 0xFFF4FFFA.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF005C3A.toInt(),
        lightAccentColor = 0xFF00A878.toInt(),
        lightSurfaceContainerColor = 0xFFE0F5EC.toInt(),
        lightSurfaceContainerLowColor = 0xFFF4FFFA.toInt(),
        lightSurfaceContainerHighColor = 0xFFCDEBDD.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    )
)