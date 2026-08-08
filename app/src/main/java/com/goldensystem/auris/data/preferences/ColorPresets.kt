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
        secondaryColor = 0xFFF06292.toInt(),
        backgroundColor = 0xFFFFF0F5.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFFFF4081.toInt()
    ),
    ColorPreset(
        name = "Vermelho",
        icon = Icons.Rounded.Whatshot,
        primaryColor = 0xFFD32F2F.toInt(),
        secondaryColor = 0xFFF44336.toInt(),
        backgroundColor = 0xFFFFEBEE.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFFFF1744.toInt()
    ),
    ColorPreset(
        name = "Verde",
        icon = Icons.Rounded.Park,
        primaryColor = 0xFF2E7D32.toInt(),
        secondaryColor = 0xFF43A047.toInt(),
        backgroundColor = 0xFFE8F5E9.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFF00E676.toInt()
    ),
    ColorPreset(
        name = "Preto",
        icon = Icons.Rounded.DarkMode,
        primaryColor = 0xFF1A1A1A.toInt(),
        secondaryColor = 0xFF424242.toInt(),
        backgroundColor = 0xFF000000.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFFFFFFFF.toInt(),
        accentColor = 0xFF757575.toInt()
    ),
    ColorPreset(
        name = "Azul",
        icon = Icons.Rounded.WaterDrop,
        primaryColor = 0xFF0D47A1.toInt(),
        secondaryColor = 0xFF1E88E5.toInt(),
        backgroundColor = 0xFFE3F2FD.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFF2979FF.toInt()
    ),
    ColorPreset(
        name = "Roxo",
        icon = Icons.Rounded.AutoAwesome,
        primaryColor = 0xFF7B1FA2.toInt(),
        secondaryColor = 0xFFAB47BC.toInt(),
        backgroundColor = 0xFFF3E5F5.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFFD500F9.toInt()
    ),
    ColorPreset(
        name = "Amarelo",
        icon = Icons.Rounded.WbSunny,
        primaryColor = 0xFFF9A825.toInt(),
        secondaryColor = 0xFFFFD54F.toInt(),
        backgroundColor = 0xFFFFFDE7.toInt(),
        onPrimaryColor = 0xFF1A1A1A.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFFFFAB00.toInt()
    ),
    ColorPreset(
        name = "Laranja",
        icon = Icons.Rounded.Whatshot,
        primaryColor = 0xFFE65100.toInt(),
        secondaryColor = 0xFFFF9800.toInt(),
        backgroundColor = 0xFFFFF3E0.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFFFF6E40.toInt()
    ),
    ColorPreset(
        name = "Ciano",
        icon = Icons.Rounded.Waves,
        primaryColor = 0xFF006064.toInt(),
        secondaryColor = 0xFF00BCD4.toInt(),
        backgroundColor = 0xFFE0F7FA.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFF00E5FF.toInt()
    ),
    ColorPreset(
        name = "Marrom",
        icon = Icons.Rounded.Coffee,
        primaryColor = 0xFF4E342E.toInt(),
        secondaryColor = 0xFF795548.toInt(),
        backgroundColor = 0xFFEFEBE9.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFF8D6E63.toInt()
    ),
    ColorPreset(
        name = "Cinza",
        icon = Icons.Rounded.GraphicEq,
        primaryColor = 0xFF616161.toInt(),
        secondaryColor = 0xFF9E9E9E.toInt(),
        backgroundColor = 0xFFF5F5F5.toInt(),
        onPrimaryColor = 0xFFFFFFFF.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFF757575.toInt()
    ),
    ColorPreset(
        name = "Neon",
        icon = Icons.Rounded.Bolt,
        primaryColor = 0xFF00E676.toInt(),
        secondaryColor = 0xFF00BCD4.toInt(),
        backgroundColor = 0xFF1A1A1A.toInt(),
        onPrimaryColor = 0xFF1A1A1A.toInt(),
        onSurfaceColor = 0xFF00E676.toInt(),
        accentColor = 0xFFFF4081.toInt()
    ),
    ColorPreset(
        name = "Pastel",
        icon = Icons.Rounded.Palette,
        primaryColor = 0xFFF8BBD0.toInt(),
        secondaryColor = 0xFFB3E5FC.toInt(),
        backgroundColor = 0xFFFFF8E1.toInt(),
        onPrimaryColor = 0xFF1A1A1A.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFFCE93D8.toInt()
    ),
    ColorPreset(
        name = "Branco",
        icon = Icons.Rounded.LightMode,
        primaryColor = 0xFFFFFFFF.toInt(),
        secondaryColor = 0xFFF5F5F5.toInt(),
        backgroundColor = 0xFFFFFFFF.toInt(),
        onPrimaryColor = 0xFF1A1A1A.toInt(),
        onSurfaceColor = 0xFF1A1A1A.toInt(),
        accentColor = 0xFFE0E0E0.toInt()
    )
)