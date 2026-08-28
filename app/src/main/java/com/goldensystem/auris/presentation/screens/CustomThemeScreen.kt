// presentation/screens/CustomThemeScreen.kt

package com.goldensystem.auris.presentation.screens

import androidx.compose.animation.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.goldensystem.auris.data.preferences.CustomThemeConfig
import com.goldensystem.auris.data.preferences.WallpaperType
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.goldensystem.auris.R
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel
import com.goldensystem.auris.ui.theme.customColorScheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CustomThemeScreen(
    navController: NavController,
    viewModel: CustomThemeViewModel = hiltViewModel()
) {
    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var resetTrigger by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var colorPickerTarget by remember { mutableStateOf<((Int) -> Unit)?>(null) }
    
    var saveJob by remember { mutableStateOf<Job?>(null) }

    val colorScheme = remember(config) { customColorScheme(config, true) }

    LaunchedEffect(resetTrigger) {
        if (resetTrigger) {
            viewModel.resetToDefault()
            resetTrigger = false
        }
    }

    LaunchedEffect(config) {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(800)
            viewModel.saveCustomTheme()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            saveJob?.cancel()
            saveJob = scope.launch {
                viewModel.saveCustomTheme()
            }
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "screen_alpha"
    )

    if (showColorPickerDialog && colorPickerTarget != null) {
    CustomColorPickerDialog(
        initialColor = when {
            // Cores existentes
            colorPickerTarget == viewModel::updatePrimaryColor -> config.primaryColor
            colorPickerTarget == viewModel::updateSecondaryColor -> config.secondaryColor
            colorPickerTarget == viewModel::updateBackgroundColor -> config.backgroundColor
            colorPickerTarget == viewModel::updateOnPrimaryColor -> config.onPrimaryColor
            colorPickerTarget == viewModel::updateOnSurfaceColor -> config.onSurfaceColor
            colorPickerTarget == viewModel::updateAccentColor -> config.accentColor
            
            // 🔽 TODAS AS NOVAS CORES AQUI 🔽
            colorPickerTarget == viewModel::updateTertiaryColor -> config.tertiaryColor
            colorPickerTarget == viewModel::updateOnSecondaryColor -> config.onSecondaryColor
            colorPickerTarget == viewModel::updateSecondaryContainerColor -> config.secondaryContainerColor
            colorPickerTarget == viewModel::updateOnSecondaryContainerColor -> config.onSecondaryContainerColor
            colorPickerTarget == viewModel::updateTertiaryContainerColor -> config.tertiaryContainerColor
            colorPickerTarget == viewModel::updateOnTertiaryContainerColor -> config.onTertiaryContainerColor
            colorPickerTarget == viewModel::updateOnBackgroundColor -> config.onBackgroundColor
            colorPickerTarget == viewModel::updateSurfaceColor -> config.surfaceColor
            colorPickerTarget == viewModel::updateSurfaceVariantColor -> config.surfaceVariantColor
            colorPickerTarget == viewModel::updateOnSurfaceVariantColor -> config.onSurfaceVariantColor
            colorPickerTarget == viewModel::updateErrorColor -> config.errorColor
            colorPickerTarget == viewModel::updateOnErrorColor -> config.onErrorColor
            colorPickerTarget == viewModel::updateErrorContainerColor -> config.errorContainerColor
            colorPickerTarget == viewModel::updateOnErrorContainerColor -> config.onErrorContainerColor
            colorPickerTarget == viewModel::updateOutlineColor -> config.outlineColor
            colorPickerTarget == viewModel::updateOutlineVariantColor -> config.outlineVariantColor
            colorPickerTarget == viewModel::updateSurfaceTintColor -> config.surfaceTintColor
            colorPickerTarget == viewModel::updateInversePrimaryColor -> config.inversePrimaryColor
            colorPickerTarget == viewModel::updateInverseSurfaceColor -> config.inverseSurfaceColor
            colorPickerTarget == viewModel::updateInverseOnSurfaceColor -> config.inverseOnSurfaceColor
            colorPickerTarget == viewModel::updateScrimColor -> config.scrimColor
                  // ⬇️⬇️⬇️ NOVAS CORES QUE FALTAVAM ⬇️⬇️⬇️
            colorPickerTarget == viewModel::updatePrimaryContainerColor -> config.primaryContainerColor
            colorPickerTarget == viewModel::updateOnPrimaryContainerColor -> config.onPrimaryContainerColor
            
            else -> config.primaryColor
        },
        onColorSelected = { color ->
            colorPickerTarget?.invoke(color)
            colorPickerTarget = null
        },
        onDismiss = {
            showColorPickerDialog = false
            colorPickerTarget = null
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.custom_theme_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { 
                            saveJob?.cancel()
                            saveJob = scope.launch { viewModel.saveCustomTheme() }
                            navController.popBackStack() 
                        }
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.auth_cd_back),
                            tint = colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { resetTrigger = true }
                    ) {
                        Icon(
                            Icons.Rounded.RestartAlt,
                            contentDescription = stringResource(R.string.cd_reset),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview do Player
            ThemePreviewCustom(
                config = config,
                colorScheme = colorScheme
            )

            Text(
                stringResource(R.string.custom_theme_colors_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )

            // Seletor de cores
            Card(
               modifier = Modifier.fillMaxWidth(),
               shape = RoundedCornerShape(20.dp),
               colors = CardDefaults.cardColors(
               containerColor = Color.Transparent
               ),
             elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {

                // DENTRO DO CARD, na seção dos ColorPickerRows
Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    Text(
        "✨ ${stringResource(R.string.custom_theme_custom_colors)}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = colorScheme.onSurface
    )
    
    // Cores existentes...
    ColorPickerRow(
        label = stringResource(R.string.custom_theme_primary),
        currentColor = config.primaryColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updatePrimaryColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updatePrimaryColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = stringResource(R.string.custom_theme_secondary),
        currentColor = config.secondaryColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateSecondaryColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateSecondaryColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = stringResource(R.string.custom_theme_background),
        currentColor = config.backgroundColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateBackgroundColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateBackgroundColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = stringResource(R.string.custom_theme_on_primary),
        currentColor = config.onPrimaryColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateOnPrimaryColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateOnPrimaryColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = stringResource(R.string.custom_theme_on_surface),
        currentColor = config.onSurfaceColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateOnSurfaceColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateOnSurfaceColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )

    ColorPickerRow(
        label = stringResource(R.string.custom_theme_accent),
        currentColor = config.accentColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateAccentColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateAccentColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )

    // 🔽 TODAS AS NOVAS CORES AQUI 🔽
    
    ColorPickerRow(
        label = "Teste tertiaryColor",
        currentColor = config.tertiaryColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateTertiaryColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateTertiaryColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste onSecondaryColor",
        currentColor = config.onSecondaryColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateOnSecondaryColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateOnSecondaryColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste secondaryContainerColor",
        currentColor = config.secondaryContainerColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateSecondaryContainerColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateSecondaryContainerColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste onSecondaryContainerColor",
        currentColor = config.onSecondaryContainerColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateOnSecondaryContainerColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateOnSecondaryContainerColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste tertiaryContainerColor",
        currentColor = config.tertiaryContainerColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateTertiaryContainerColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateTertiaryContainerColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste onTertiaryContainerColor",
        currentColor = config.onTertiaryContainerColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateOnTertiaryContainerColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateOnTertiaryContainerColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste onBackgroundColor",
        currentColor = config.onBackgroundColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateOnBackgroundColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateOnBackgroundColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste surfaceColor",
        currentColor = config.surfaceColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateSurfaceColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateSurfaceColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste surfaceVariantColor",
        currentColor = config.surfaceVariantColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateSurfaceVariantColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateSurfaceVariantColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste onSurfaceVariantColor",
        currentColor = config.onSurfaceVariantColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateOnSurfaceVariantColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateOnSurfaceVariantColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste errorColor",
        currentColor = config.errorColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateErrorColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateErrorColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste onErrorColor",
        currentColor = config.onErrorColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateOnErrorColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateOnErrorColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste errorContainerColor",
        currentColor = config.errorContainerColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateErrorContainerColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateErrorContainerColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste onErrorContainerColor",
        currentColor = config.onErrorContainerColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateOnErrorContainerColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateOnErrorContainerColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste outlineColor",
        currentColor = config.outlineColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateOutlineColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateOutlineColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste outlineVariantColor",
        currentColor = config.outlineVariantColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateOutlineVariantColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateOutlineVariantColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste surfaceTintColor",
        currentColor = config.surfaceTintColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateSurfaceTintColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateSurfaceTintColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste inversePrimaryColor",
        currentColor = config.inversePrimaryColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateInversePrimaryColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateInversePrimaryColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste inverseSurfaceColor",
        currentColor = config.inverseSurfaceColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateInverseSurfaceColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateInverseSurfaceColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste inverseOnSurfaceColor",
        currentColor = config.inverseOnSurfaceColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateInverseOnSurfaceColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateInverseOnSurfaceColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
    )
    
    ColorPickerRow(
        label = "Teste scrimColor",
        currentColor = config.scrimColor,
        colors = MAIN_COLORS,
        onColorSelected = { viewModel.updateScrimColor(it) },
        onCustomColorClick = { 
            colorPickerTarget = viewModel::updateScrimColor
            showColorPickerDialog = true
        },
        colorScheme = colorScheme
      )
      // Depois do último ColorPickerRow (scrimColor)
ColorPickerRow(
    label = "Teste primaryContainerColor",
    currentColor = config.primaryContainerColor,
    colors = MAIN_COLORS,
    onColorSelected = { viewModel.updatePrimaryContainerColor(it) },
    onCustomColorClick = { 
        colorPickerTarget = viewModel::updatePrimaryContainerColor
        showColorPickerDialog = true
    },
    colorScheme = colorScheme
)

ColorPickerRow(
    label = "Teste onPrimaryContainerColor",
    currentColor = config.onPrimaryContainerColor,
    colors = MAIN_COLORS,
    onColorSelected = { viewModel.updateOnPrimaryContainerColor(it) },
    onCustomColorClick = { 
        colorPickerTarget = viewModel::updateOnPrimaryContainerColor
        showColorPickerDialog = true
    },
    colorScheme = colorScheme
)
    }
  }

            Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }

// ==================== PREVIEW CARD ====================

@Composable
private fun CustomThemePreviewCard(config: CustomThemeConfig) {
    val colorScheme = remember(config) { customColorScheme(config, true) }
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "preview_card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .scale(cardScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Apenas feedback */ },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.surface)
        )
        
            // Overlay de escurecimento
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = config.wallpaperDim * 0.5f))
            ){
            // Preview do wallpaper simplificado
            when (config.wallpaperType) {
                WallpaperType.SOLID -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(config.backgroundColor))
                    )
                }
                WallpaperType.GALLERY -> {
                    config.wallpaperUri?.let { uri ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uri)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = 1.1f
                                    scaleY = 1.1f
                                    this.alpha = 1f - config.wallpaperDim
                                },
                            contentScale = ContentScale.Crop
                        )
                    } ?: Box(modifier = Modifier.fillMaxSize().background(colorScheme.surface))
                }
                WallpaperType.SERVER -> {
                    config.wallpaperUrl?.let { url ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(url)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = 1.1f
                                    scaleY = 1.1f
                                    this.alpha = 1f - config.wallpaperDim
                                },
                            contentScale = ContentScale.Crop
                        )
                    } ?: Box(modifier = Modifier.fillMaxSize().background(colorScheme.surface))
                }
            }
        }
    }
}

@Composable
private fun ThemePreviewCustom(
    config: CustomThemeConfig,
    colorScheme: ColorScheme
) {
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

// ==================== COLOR PICKER ROW ====================

@Composable
private fun ColorPickerRow(
    label: String,
    currentColor: Int,
    colors: List<Int>,
    onColorSelected: (Int) -> Unit,
    onCustomColorClick: () -> Unit,
    colorScheme: ColorScheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Itens de cores
            items(colors) { color ->
                ColorItem(
                    color = color,
                    isSelected = color == currentColor,
                    onColorSelected = onColorSelected,
                    colorScheme = colorScheme
                )
            }
            
            // Botão "+" para abrir o seletor personalizado
            item {
                CustomColorButton(
                    onClick = onCustomColorClick,
                    colorScheme = colorScheme
                )
            }
        }
    }
}

// ==================== COLOR ITEM ====================

@Composable
private fun ColorItem(
    color: Int,
    isSelected: Boolean,
    onColorSelected: (Int) -> Unit,
    colorScheme: ColorScheme
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val itemScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "color_item_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "color_item_border"
    )
    val size by animateDpAsState(
        targetValue = if (isSelected) 44.dp else 38.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "color_item_size"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(itemScale)
            .clip(RoundedCornerShape(8.dp)) // Mudança: quadrado com bordas arredondadas
            .background(Color(color))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onColorSelected(color) }
            .then(
                if (isSelected) {
                    Modifier.border(borderWidth, Color.White, RoundedCornerShape(8.dp))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(150)) + scaleIn(
                    initialScale = 0.5f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                )
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color(color).contrastTextColor(),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ==================== CUSTOM COLOR BUTTON ====================

@Composable
private fun CustomColorButton(
    onClick: () -> Unit,
    colorScheme: ColorScheme
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "custom_color_button_scale"
    )

    Box(
        modifier = Modifier
            .size(38.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(
                colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.Add,
            contentDescription = stringResource(R.string.custom_theme_custom_color),
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ==================== CUSTOM COLOR PICKER DIALOG ====================

@Composable
private fun CustomColorPickerDialog(
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hue by remember { mutableFloatStateOf(Color(initialColor).hue) }
    var saturation by remember { mutableFloatStateOf(Color(initialColor).saturation) }
    var brightness by remember { mutableFloatStateOf(Color(initialColor).brightness) }
    
    val selectedColor = remember(hue, saturation, brightness) {
        Color.hsv(hue, saturation, brightness)
    }
    
    var colorHex by remember { 
        mutableStateOf(String.format("#%06X", (0xFFFFFF and initialColor))) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.ColorLens,
                    contentDescription = null,
                    tint = selectedColor
                )
                Text(stringResource(R.string.custom_theme_custom_color_title))
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Preview da cor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(selectedColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(selectedColor.contrastTextColor().copy(alpha = 0.1f))
                    )
                    Text(
                        "HSV Color Picker",
                        modifier = Modifier.align(Alignment.Center),
                        color = selectedColor.contrastTextColor(),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Sliders HSV
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HSVSlider(
                        label = stringResource(R.string.custom_theme_hue),
                        value = hue,
                        onValueChange = { hue = it },
                        valueRange = 0f..360f,
                        colors = listOf(
                            Color.Red,
                            Color.Yellow,
                            Color.Green,
                            Color.Cyan,
                            Color.Blue,
                            Color.Magenta,
                            Color.Red
                        )
                    )

                    HSVSlider(
                        label = stringResource(R.string.custom_theme_saturation),
                        value = saturation,
                        onValueChange = { saturation = it },
                        valueRange = 0f..1f,
                        colors = listOf(
                            Color.hsv(hue, 0f, brightness),
                            Color.hsv(hue, 1f, brightness)
                        )
                    )

                    HSVSlider(
                        label = stringResource(R.string.custom_theme_brightness),
                        value = brightness,
                        onValueChange = { brightness = it },
                        valueRange = 0f..1f,
                        colors = listOf(
                            Color.Black,
                            Color.hsv(hue, saturation, 1f)
                        )
                    )
                }

                // Input HEX
                OutlinedTextField(
                    value = colorHex,
                    onValueChange = {
                        colorHex = it
                        try {
                            val color = Color(android.graphics.Color.parseColor(it))
                            hue = color.hue
                            saturation = color.saturation
                            brightness = color.brightness
                        } catch (_: Exception) { }
                    },
                    label = { Text("HEX") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedColor,
                        cursorColor = selectedColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(selectedColor.toArgb())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedColor
                )
            ) {
                Text(stringResource(R.string.custom_theme_apply_color))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.custom_theme_cancel))
            }
        }
    )
}

@Composable
private fun HSVSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    colors: List<Color>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${(value * if (valueRange.endInclusive <= 1f) 100 else 1).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.horizontalGradient(colors)
                )
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                )
            )
        }
    }
}

// ==================== UTILITY FUNCTIONS ====================

fun Color.contrastTextColor(): Color {
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
    return if (luminance > 0.5) Color.Black else Color.White
}

private val Color.hue: Float
    get() {
        val max = maxOf(red, green, blue)
        val min = minOf(red, green, blue)
        val delta = max - min
        
        if (delta == 0f) return 0f
        
        val hue = when (max) {
            red -> ((green - blue) / delta) % 6f
            green -> ((blue - red) / delta) + 2f
            else -> ((red - green) / delta) + 4f
        }
        
        var hueDegrees = hue * 60f
        if (hueDegrees < 0) hueDegrees += 360f
        if (hueDegrees >= 360f) hueDegrees -= 360f
        
        return hueDegrees
    }

private val Color.saturation: Float
    get() {
        val max = maxOf(red, green, blue)
        val min = minOf(red, green, blue)
        return if (max == 0f) 0f else (max - min) / max
    }

private val Color.brightness: Float
    get() = maxOf(red, green, blue)

// Cores
val MAIN_COLORS = listOf(
    0xFF000000.toInt(), 0xFF795548.toInt(), 0xFFE53935.toInt(),
    0xFFFF9800.toInt(), 0xFFFFEB3B.toInt(), 0xFF8BC34A.toInt(),
    0xFF2E7D32.toInt(), 0xFF42A5F5.toInt(), 0xFF0D47A1.toInt(),
    0xFF7B1FA2.toInt(), 0xFFE91E63.toInt(), 0xFFFFFFFF.toInt()
)

val ADDITIONAL_COLORS = listOf(
    0xFFFF6F00.toInt(), 0xFF00BCD4.toInt(), 0xFF00E676.toInt(),
    0xFFFF4081.toInt(), 0xFF651FFF.toInt(), 0xFF2979FF.toInt(),
    0xFFFF6E40.toInt(), 0xFFF50057.toInt(), 0xFF00E5FF.toInt(),
    0xFF76FF03.toInt(), 0xFFD500F9.toInt(), 0xFFFFAB00.toInt()
)