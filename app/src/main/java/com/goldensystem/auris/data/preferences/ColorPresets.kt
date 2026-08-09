// data/preferences/ColorPresets.kt

package com.seuapp.data.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

// ============================================================
// 4. PREDEFINIÇÕES DE CORES
// ============================================================
data class ColorPreset(
    val name: String,
    val icon: ImageVector,
    val primaryColor: Int,
    val secondaryColor: Int,
    val backgroundColor: Int,
    val onPrimaryColor: Int,
    val onSurfaceColor: Int,
    val accentColor: Int
)

val COLOR_PRESETS = listOf(
    ColorPreset(
        name = "Rosa",
        icon = Icons.Rounded.Favorite,
        primaryColor = 0xFFE91E63.toInt(),
        secondaryColor = 0xFFE91E63.toInt(),
        backgroundColor = 0xFF2A000E.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFFE91E63.toInt(),
        accentColor = 0xFFFF4081.toInt()
    ),
    ColorPreset(
        name = "Vermelho",
        icon = Icons.Rounded.Whatshot,
        primaryColor = 0xFFD32F2F.toInt(),
        secondaryColor = 0xFFF44336.toInt(),
        backgroundColor = 0xFF0E0000.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFFD32F2F.toInt(),
        accentColor = 0xFFFF1744.toInt()
    ),
    ColorPreset(
        name = "Verde",
        icon = Icons.Rounded.Park,
        primaryColor = 0xFF2E7D61.toInt(),
        secondaryColor = 0xFF43A047.toInt(),
        backgroundColor = 0xFF000E00.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF2E7D49.toInt(),
        accentColor = 0xFF00E676.toInt()
    ),
    ColorPreset(
        name = "Preto",
        icon = Icons.Rounded.DarkMode,
        primaryColor = 0xFF4D4D4D.toInt(),
        secondaryColor = 0xFF424242.toInt(),
        backgroundColor = 0xFF000000.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFFFFFFFF.toInt(),
        accentColor = 0xFF757575.toInt()
    ),
    ColorPreset(
        name = "Azul",
        icon = Icons.Rounded.WaterDrop,
        primaryColor = 0xFF0D5AA1.toInt(),
        secondaryColor = 0xFF1E88E5.toInt(),
        backgroundColor = 0xFF00101C.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF1B1EDA.toInt(),
        accentColor = 0xFF2979FF.toInt()
    ),
    ColorPreset(
        name = "Roxo",
        icon = Icons.Rounded.AutoAwesome,
        primaryColor = 0xFF7B1FA2.toInt(),
        secondaryColor = 0xFFAB47BC.toInt(),
        backgroundColor = 0xFF150018.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF7B1FA2.toInt(),
        accentColor = 0xFFD500F9.toInt()
    ),
    ColorPreset(
        name = "Amarelo",
        icon = Icons.Rounded.WbSunny,
        primaryColor = 0xFFF9A825.toInt(),
        secondaryColor = 0xFFFFD54F.toInt(),
        backgroundColor = 0xFF090900.toInt(),
        onPrimaryColor = 0xFFFFFEBF.toInt(),
        onSurfaceColor = 0xFFF9A825.toInt(),
        accentColor = 0xFFFFAB00.toInt()
    ),
    ColorPreset(
        name = "Laranja",
        icon = Icons.Rounded.Whatshot,
        primaryColor = 0xFFE65100.toInt(),
        secondaryColor = 0xFFFF9800.toInt(),
        backgroundColor = 0xFF0F0A00.toInt(),
        onPrimaryColor = 0xFFFFDDB3.toInt(),
        onSurfaceColor = 0xFFE65100.toInt(),
        accentColor = 0xFFFF6E40.toInt()
    ),
    ColorPreset(
        name = "Ciano",
        icon = Icons.Rounded.Waves,
        primaryColor = 0xFF00F5FF.toInt(),
        secondaryColor = 0xFF00BCD4.toInt(),
        backgroundColor = 0xFF00090A.toInt(),
        onPrimaryColor = 0xFFD5FBFF.toInt(),
        onSurfaceColor = 0xFF00F5FF.toInt(),
        accentColor = 0xFF00E5FF.toInt()
    ),
    ColorPreset(
        name = "Cinza",
        icon = Icons.Rounded.GraphicEq,
        primaryColor = 0xFF626262.toInt(),
        secondaryColor = 0xFFD3D3D3.toInt(),
        backgroundColor = 0xFF4C4C4C.toInt(),
        onPrimaryColor = 0xFFC6C6C6.toInt(),
        onSurfaceColor = 0xFFCDCDCD.toInt(),
        accentColor = 0xFF757575.toInt()
    ),
    ColorPreset(
        name = "Neon",
        icon = Icons.Rounded.Bolt,
        primaryColor = 0xFF00FF84.toInt(),
        secondaryColor = 0xFF00E2FF.toInt(),
        backgroundColor = 0xFF1A1A1A.toInt(),
        onPrimaryColor = 0xFFFFF7FF.toInt(),
        onSurfaceColor = 0xFF00FFE2.toInt(),
        accentColor = 0xFFFF4081.toInt()
    ),
)