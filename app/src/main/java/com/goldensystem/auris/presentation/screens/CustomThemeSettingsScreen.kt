package com.goldensystem.auris.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.goldensystem.auris.R
import com.goldensystem.auris.data.performance.DevicePerformanceTier
import com.goldensystem.auris.data.preferences.CustomThemeConfig
import com.goldensystem.auris.data.preferences.WallpaperType
import com.goldensystem.auris.presentation.navigation.Screen
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel
import com.goldensystem.auris.presentation.viewmodel.DevicePerformanceViewModel
import com.goldensystem.auris.presentation.viewmodel.ExperimentalFeaturesViewModel
import com.goldensystem.auris.ui.theme.customColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThemeSettingsScreen(
    navController: NavController,
    viewModel: CustomThemeViewModel = hiltViewModel(),
    experimentalViewModel: ExperimentalFeaturesViewModel = hiltViewModel(),
    devicePerfViewModel: DevicePerformanceViewModel = hiltViewModel()
) {
    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()
    val colorScheme = remember(config) { customColorScheme(config, true) }
    val useAuroraGlassUi by experimentalViewModel.useAuroraGlassUi.collectAsStateWithLifecycle()

    val perfState by devicePerfViewModel.uiState.collectAsStateWithLifecycle()
    val deviceTier = perfState.tier

    var showPresetsSheet by remember { mutableStateOf(false) }
    var showAuroraWarning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isResetting by remember { mutableStateOf(false) }

    // ✅ Auto-desativa o Aurora Glass se o aparelho for rebaixado para BAIXO
    LaunchedEffect(deviceTier) {
        if (deviceTier == DevicePerformanceTier.BAIXO && useAuroraGlassUi) {
            experimentalViewModel.setUseAuroraGlassUi(false)
        }
    }

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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.auth_cd_back),
                            tint = colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isResetting = true
                                viewModel.resetToDefault()
                                viewModel.saveCustomTheme()
                                delay(300)
                                isResetting = false
                            }
                        },
                        enabled = !isResetting
                    ) {
                        Icon(
                            Icons.Rounded.RestartAlt,
                            contentDescription = stringResource(R.string.custom_theme_reset_to_default),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header com preview do tema atual
            item {
                ThemePreviewHeader(
                    config = config,
                    colorScheme = colorScheme
                )
            }

            // Seção: Personalização do Tema
            item {
                SettingsGroupHeader(
                    title = stringResource(R.string.custom_theme_personalization)
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.custom_theme_presets),
                    subtitle = stringResource(R.string.custom_theme_presets_subtitle),
                    onClick = { showPresetsSheet = true }
                )
            }

            item {
                SettingsCardItem(
                    icon = Icons.Rounded.Wallpaper,
                    title = stringResource(R.string.custom_theme_wallpaper),
                    subtitle = when (config.wallpaperType) {
                        WallpaperType.SOLID -> stringResource(R.string.custom_theme_wallpaper_solid_subtitle)
                        WallpaperType.GALLERY -> stringResource(R.string.custom_theme_wallpaper_gallery_subtitle)
                        WallpaperType.SERVER -> stringResource(R.string.custom_theme_wallpaper_server_subtitle)
                    },
                    onClick = {
                        navController.navigate(Screen.Wallpaper.route)
                    }
                )
            }

            // Seção: Avançado
            item {
                SettingsGroupHeader(
                    title = stringResource(R.string.custom_theme_advance_section)
                )
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

            // ✨ Seção: Recursos experimentais
            item {
                SettingsGroupHeader(
                    title = stringResource(R.string.custom_theme_experimental_section)
                )
            }

            // Lógica condicional do Aurora Glass por tier
            item {
                when (deviceTier) {
                    DevicePerformanceTier.BAIXO -> {
                        SettingsToggleItem(
                            icon = Icons.Rounded.Science,
                            title = stringResource(R.string.custom_theme_experimental_aurora_glass_title),
                            subtitle = stringResource(R.string.custom_theme_experimental_aurora_glass_not_supported),
                            checked = false,
                            enabled = false,
                            onCheckedChange = { }
                        )
                    }
                    DevicePerformanceTier.MEDIO -> {
                        SettingsToggleItem(
                            icon = Icons.Rounded.Science,
                            title = stringResource(R.string.custom_theme_experimental_aurora_glass_title),
                            subtitle = stringResource(R.string.custom_theme_experimental_aurora_glass_subtitle),
                            checked = useAuroraGlassUi,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    showAuroraWarning = true
                                } else {
                                    experimentalViewModel.setUseAuroraGlassUi(false)
                                }
                            }
                        )
                    }
                    DevicePerformanceTier.BOM -> {
                        SettingsToggleItem(
                            icon = Icons.Rounded.Science,
                            title = stringResource(R.string.custom_theme_experimental_aurora_glass_title),
                            subtitle = stringResource(R.string.custom_theme_experimental_aurora_glass_subtitle),
                            checked = useAuroraGlassUi,
                            onCheckedChange = { experimentalViewModel.setUseAuroraGlassUi(it) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showPresetsSheet) {
        ThemePresetsBottomSheet(
            viewModel = viewModel,
            onDismiss = { showPresetsSheet = false }
        )
    }

    // Dialog de aviso para desempenho MÉDIO
    if (showAuroraWarning) {
        AlertDialog(
            onDismissRequest = { showAuroraWarning = false },
            icon = {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.custom_theme_aurora_warning_title)) },
            text = { Text(stringResource(R.string.custom_theme_aurora_warning_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        experimentalViewModel.setUseAuroraGlassUi(true)
                        showAuroraWarning = false
                    }
                ) {
                    Text(stringResource(R.string.action_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuroraWarning = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = colorScheme.surfaceContainerHigh,
            titleContentColor = colorScheme.onSurface,
            textContentColor = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemePreviewHeader(
    config: CustomThemeConfig,
    colorScheme: ColorScheme
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp)),
        color = colorScheme.surfaceContainer,
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    config.primaryColor to "Primary",
                    config.secondaryColor to "Secondary",
                    config.backgroundColor to "Background",
                    config.accentColor to "Accent"
                ).forEach { (colorValue, _) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(colorValue)
                    ) {}
                }
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
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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

            Column(modifier = Modifier.weight(1f)) {
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

            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.1f else 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.4f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.6f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.6f)
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}