// presentation/screens/CustomThemeSettingsScreen.kt

package com.goldensystem.auris.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.goldensystem.auris.R
import com.goldensystem.auris.data.preferences.CustomThemeConfig
import com.goldensystem.auris.data.preferences.WallpaperType
import com.goldensystem.auris.presentation.navigation.Screen
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel
import com.goldensystem.auris.ui.theme.customColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThemeSettingsScreen(
    navController: NavController,
    viewModel: CustomThemeViewModel = hiltViewModel()
) {
    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()
    val colorScheme = remember(config) { customColorScheme(config, true) }
    
    var isCustomThemeEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.custom_theme_settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.auth_cd_back),
                            tint = colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header com preview do tema atual
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    color = colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Ícone de tema
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Palette,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    stringResource(R.string.custom_theme_current_theme),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    when (config.wallpaperType) {
                                        WallpaperType.SOLID -> stringResource(R.string.wallpaper_type_solid)
                                        WallpaperType.GALLERY -> stringResource(R.string.wallpaper_type_gallery)
                                        WallpaperType.SERVER -> stringResource(R.string.wallpaper_type_server)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Mini preview das cores
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                config.primaryColor to "Primary",
                                config.secondaryColor to "Secondary",
                                config.backgroundColor to "Background",
                                config.accentColor to "Accent"
                            ).forEach { (color, _) ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = color(color)
                                ) {}
                            }
                        }
                    }
                }
            }

            // Seção: Personalização do Tema
            item {
                SettingsGroupHeader(title = stringResource(R.string.custom_theme_personalization))
            }

            item {
                SettingsCardItem(
                    icon = Icons.Rounded.ColorLens,
                    title = stringResource(R.string.custom_theme_edit_colors),
                    subtitle = stringResource(R.string.custom_theme_edit_colors_subtitle),
                    onClick = {
                        navController.navigate(Screen.CustomTheme.route)
                    }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Rounded.Wallpaper,
                    title = stringResource(R.string.custom_theme_wallpaper),
                    subtitle = stringResource(R.string.custom_theme_wallpaper_subtitle),
                    onClick = {
                        // Navegar para tela de wallpaper (se existir)
                        // navController.navigate(Screen.WallpaperSettings.route)
                    }
                )
            }

            // Seção: Estilos
            item {
                SettingsGroupHeader(title = stringResource(R.string.custom_theme_styles))
            }

            item {
                SettingsCardItem(
                    icon = Icons.Rounded.Style,
                    title = stringResource(R.string.custom_theme_font_style),
                    subtitle = stringResource(R.string.custom_theme_font_style_subtitle),
                    onClick = {
                        // Navegar para tela de fontes
                    }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Rounded.Image,
                    title = stringResource(R.string.custom_theme_icon_style),
                    subtitle = stringResource(R.string.custom_theme_icon_style_subtitle),
                    onClick = {
                        // Navegar para tela de ícones
                    }
                )
            }

            // Seção: Avançado
            item {
                SettingsGroupHeader(title = stringResource(R.string.custom_theme_advanced))
            }

            item {
                SettingsCardItem(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.custom_theme_export_theme),
                    subtitle = stringResource(R.string.custom_theme_export_theme_subtitle),
                    onClick = {
                        // Exportar tema
                    }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.custom_theme_import_theme),
                    subtitle = stringResource(R.string.custom_theme_import_theme_subtitle),
                    onClick = {
                        // Importar tema
                    }
                )
            }

            // Espaço final
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsCardItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Ícone
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Título e subtítulo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chevron
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}