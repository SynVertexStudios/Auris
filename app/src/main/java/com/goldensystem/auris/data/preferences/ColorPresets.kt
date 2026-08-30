// data/preferences/ColorPresets.kt

package com.goldensystem.auris.data.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

// ============================================================
// 4. PREDEFINIÇÕES DE CORES
// ============================================================
data class ColorPreset(
    val name: String,
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
    ColorPreset(
        name = "Azul",
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

        lightPrimaryColor = 0xFF1565C0.toInt(),
        lightSecondaryColor = 0xFF90CAF9.toInt(),
        lightBackgroundColor = 0xFFF5F9FF.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF0D47A1.toInt(),
        lightAccentColor = 0xFF2962FF.toInt(),
        lightSurfaceContainerColor = 0xFFE3F2FD.toInt(),
        lightSurfaceContainerLowColor = 0xFFF5F9FF.toInt(),
        lightSurfaceContainerHighColor = 0xFFD6EAF8.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),
    ColorPreset(
        name = "Vermelho",
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

        lightPrimaryColor = 0xFFD32F2F.toInt(),
        lightSecondaryColor = 0xFFFFCDD2.toInt(),
        lightBackgroundColor = 0xFFFFF5F5.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFFB71C1C.toInt(),
        lightAccentColor = 0xFFFF1744.toInt(),
        lightSurfaceContainerColor = 0xFFFFE5E5.toInt(),
        lightSurfaceContainerLowColor = 0xFFFFF5F5.toInt(),
        lightSurfaceContainerHighColor = 0xFFFFD6D6.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),
    ColorPreset(
        name = "Ciano",
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

        lightPrimaryColor = 0xFF00838F.toInt(),
        lightSecondaryColor = 0xFF80DEEA.toInt(),
        lightBackgroundColor = 0xFFF5FFFF.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF004D40.toInt(),
        lightAccentColor = 0xFF00BCD4.toInt(),
        lightSurfaceContainerColor = 0xFFE0F7FA.toInt(),
        lightSurfaceContainerLowColor = 0xFFF5FFFF.toInt(),
        lightSurfaceContainerHighColor = 0xFFCCF0F5.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),
    ColorPreset(
        name = "Verde",
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

        lightPrimaryColor = 0xFF2E7D32.toInt(),
        lightSecondaryColor = 0xFFA5D6A7.toInt(),
        lightBackgroundColor = 0xFFF5FFF5.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF1B5E20.toInt(),
        lightAccentColor = 0xFF00C853.toInt(),
        lightSurfaceContainerColor = 0xFFE8F5E9.toInt(),
        lightSurfaceContainerLowColor = 0xFFF5FFF5.toInt(),
        lightSurfaceContainerHighColor = 0xFFDCEEDD.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),
    ColorPreset(
        name = "Preto",
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

        lightPrimaryColor = 0xFF616161.toInt(),
        lightSecondaryColor = 0xFFBDBDBD.toInt(),
        lightBackgroundColor = 0xFFFFFFFF.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF000000.toInt(),
        lightAccentColor = 0xFF9E9E9E.toInt(),
        lightSurfaceContainerColor = 0xFFF5F5F5.toInt(),
        lightSurfaceContainerLowColor = 0xFFFFFFFF.toInt(),
        lightSurfaceContainerHighColor = 0xFFEBEBEB.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),
    ColorPreset(
        name = "Rosa",
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

        lightPrimaryColor = 0xFFFFAFCA.toInt(),
        lightSecondaryColor = 0xFFF48FB1.toInt(),
        lightBackgroundColor = 0xFFFFFFFF.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFFFF50A8.toInt(),
        lightAccentColor = 0xFFFF4081.toInt(),
        lightSurfaceContainerColor = 0xFFFFD9E3.toInt(),
        lightSurfaceContainerLowColor = 0xFFFFE8F0.toInt(),
        lightSurfaceContainerHighColor = 0xFFFFEBF2.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),
    ColorPreset(
        name = "Roxo",
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

        lightPrimaryColor = 0xFF7B1FA2.toInt(),
        lightSecondaryColor = 0xFFCE93D8.toInt(),
        lightBackgroundColor = 0xFFFBF5FF.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF4A148C.toInt(),
        lightAccentColor = 0xFFD500F9.toInt(),
        lightSurfaceContainerColor = 0xFFF3E5F5.toInt(),
        lightSurfaceContainerLowColor = 0xFFFBF5FF.toInt(),
        lightSurfaceContainerHighColor = 0xFFEDDDF2.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),
    ColorPreset(
        name = "Amarelo",
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

        lightPrimaryColor = 0xFFF9A825.toInt(),
        lightSecondaryColor = 0xFFFFF59D.toInt(),
        lightBackgroundColor = 0xFFFFFFF5.toInt(),
        lightOnPrimaryColor = 0xFFFFFEBF.toInt(),
        lightOnSurfaceColor = 0xFF5D4037.toInt(),
        lightAccentColor = 0xFFFFAB00.toInt(),
        lightSurfaceContainerColor = 0xFFFFF8E1.toInt(),
        lightSurfaceContainerLowColor = 0xFFFFFFF5.toInt(),
        lightSurfaceContainerHighColor = 0xFFFFF0C0.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),
    ColorPreset(
        name = "Laranja",
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

        lightPrimaryColor = 0xFFE65100.toInt(),
        lightSecondaryColor = 0xFFFFCC80.toInt(),
        lightBackgroundColor = 0xFFFFF5F0.toInt(),
        lightOnPrimaryColor = 0xFFFFDDB3.toInt(),
        lightOnSurfaceColor = 0xFFBF360C.toInt(),
        lightAccentColor = 0xFFFF6E40.toInt(),
        lightSurfaceContainerColor = 0xFFFFF3E0.toInt(),
        lightSurfaceContainerLowColor = 0xFFFFF5F0.toInt(),
        lightSurfaceContainerHighColor = 0xFFFFE8CC.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),
    ColorPreset(
        name = "Neon",
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

        lightPrimaryColor = 0xFF00C853.toInt(),
        lightSecondaryColor = 0xFF00E5FF.toInt(),
        lightBackgroundColor = 0xFFF5FFF5.toInt(),
        lightOnPrimaryColor = 0xFFFFFFFF.toInt(),
        lightOnSurfaceColor = 0xFF1A1A1A.toInt(),
        lightAccentColor = 0xFFFF4081.toInt(),
        lightSurfaceContainerColor = 0xFFE8F5E9.toInt(),
        lightSurfaceContainerLowColor = 0xFFF5FFF5.toInt(),
        lightSurfaceContainerHighColor = 0xFFD9EDDA.toInt(),
        lightSurfaceContainerLowestColor = 0xFFFFFFFF.toInt()
    ),
)