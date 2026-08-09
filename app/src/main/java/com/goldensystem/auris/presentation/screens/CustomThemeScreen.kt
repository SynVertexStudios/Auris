// presentation/screens/CustomThemeScreen.kt

@file:OptIn(ExperimentalAnimationApi::class)

package com.goldensystem.auris.presentation.screens

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.RenderEffect
import com.seuapp.data.preferences.ColorPreset
import com.seuapp.data.preferences.COLOR_PRESETS
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import android.graphics.Shader
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.goldensystem.auris.R
import com.goldensystem.auris.data.preferences.CustomThemeConfig
import com.goldensystem.auris.data.preferences.WallpaperType
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel
import com.goldensystem.auris.ui.theme.customColorScheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================
// 1. CONFIGURAÇÃO DAS CATEGORIAS (Fácil de editar)
// ============================================================
// Chave: Categoria / Valor: (nome_da_pasta, quantidade_de_imagens)
private val CATEGORY_CONFIG = mapOf(
    WallpaperCategory.ANIME to ("anime" to 7),   // Wallpaper1 até Wallpaper7
    WallpaperCategory.CARR  to ("carr" to 12),   // Wallpaper1 até Wallpaper12
    WallpaperCategory.NEON  to ("neon" to 7),    // Wallpaper1 até Wallpaper7
    WallpaperCategory.OTRS  to ("otrs" to 7),    // Wallpaper1 até Wallpaper7
    WallpaperCategory.SPACE to ("space" to 10)   // Wallpaper1 até Wallpaper10
)

// ============================================================
// 2. GERADOR DE URLs (Automático)
// ============================================================
private const val BASE_URL = "https://raw.githubusercontent.com/pereirasaymonsilva-a11y/Auris/main/assets/wallpaper"

// Mapa principal com as listas separadas por categoria (já inclui a ALL)
val WALLPAPER_CATEGORIES = mutableMapOf<WallpaperCategory, List<String>>().apply {
    CATEGORY_CONFIG.forEach { (category, config) ->
        val (folder, count) = config
        // Gera os links de Wallpaper1 até WallpaperN
        val urls = (1..count).map { index ->
            "$BASE_URL/$folder/Wallpaper$index.jpg"
        }
        this[category] = urls
    }
    // Adiciona a categoria ALL com TODAS as imagens juntas
    this[WallpaperCategory.ALL] = this.values.flatten()
}

// Lista global com todos os wallpapers (para manter compatibilidade com seu código antigo)
val SERVER_WALLPAPERS: List<String> = WALLPAPER_CATEGORIES[WallpaperCategory.ALL] ?: emptyList()

// ============================================================
// 3. ENUM (Mantenha o seu ou use este)
// ============================================================
enum class WallpaperCategory {
    ALL, SPACE, CARR, ANIME, NEON, OTRS
}

// Cores principais
val MAIN_COLORS = listOf(
    0xFF000000.toInt(), // Preto
    0xFF795548.toInt(), // Marrom
    0xFFE53935.toInt(), // Vermelho
    0xFFFF9800.toInt(), // Laranja
    0xFFFFEB3B.toInt(), // Amarelo
    0xFF8BC34A.toInt(), // Verde claro
    0xFF2E7D32.toInt(), // Verde escuro
    0xFF42A5F5.toInt(), // Azul claro
    0xFF0D47A1.toInt(), // Azul escuro
    0xFF7B1FA2.toInt(), // Roxo
    0xFFE91E63.toInt(), // Rosa
    0xFFFFFFFF.toInt()  // Branco
)

// Cores adicionais
val ADDITIONAL_COLORS = listOf(
    0xFFFF6F00.toInt(), // Âmbar
    0xFF00BCD4.toInt(), // Ciano
    0xFF00E676.toInt(), // Verde neon
    0xFFFF4081.toInt(), // Rosa neon
    0xFF651FFF.toInt(), // Roxo profundo
    0xFF2979FF.toInt(), // Azul vibrante
    0xFFFF6E40.toInt(), // Coral
    0xFFF50057.toInt(), // Vermelho neon
    0xFF00E5FF.toInt(), // Ciano claro
    0xFF76FF03.toInt(), // Verde limão
    0xFFD500F9.toInt(), // Magenta
    0xFFFFAB00.toInt()  // Ouro
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CustomThemeScreen(
    navController: NavController,
    viewModel: CustomThemeViewModel = hiltViewModel()
) {
    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    var resetTrigger by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var colorPickerTarget by remember { mutableStateOf<((Int) -> Unit)?>(null) }
    
    // Debounce para salvar automaticamente
    var saveJob by remember { mutableStateOf<Job?>(null) }

    // Preview do tema
    val previewColorScheme = remember(config) {
        customColorScheme(config, true)
    }

    // Reset
    LaunchedEffect(resetTrigger) {
        if (resetTrigger) {
            viewModel.resetToDefault()
            resetTrigger = false
        }
    }

    // Salvar com debounce
    LaunchedEffect(config) {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(800) // Aguarda 800ms sem mudanças
            viewModel.saveCustomTheme()
        }
    }

    // Salvar ao sair da tela
    DisposableEffect(Unit) {
    onDispose {
        saveJob?.cancel()
        saveJob = scope.launch {
            viewModel.saveCustomTheme()
        }
    }
}

    // Animações
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "screen_alpha"
    )

// presentation/screens/CustomThemeScreen.kt

// Dialog de color picker
if (showColorPickerDialog && colorPickerTarget != null) {
    CustomColorPickerDialog(
        initialColor = when {
            colorPickerTarget == viewModel::updatePrimaryColor -> config.primaryColor
            colorPickerTarget == viewModel::updateSecondaryColor -> config.secondaryColor
            colorPickerTarget == viewModel::updateBackgroundColor -> config.backgroundColor
            colorPickerTarget == viewModel::updateOnPrimaryColor -> config.onPrimaryColor
            colorPickerTarget == viewModel::updateOnSurfaceColor -> config.onSurfaceColor
            colorPickerTarget == viewModel::updateAccentColor -> config.accentColor
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
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { 
                            saveJob?.cancel()
                            saveJob = scope.launch {
                            viewModel.saveCustomTheme()
                            }
                            navController.popBackStack() 
                        }
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.auth_cd_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { resetTrigger = true }
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = stringResource(R.string.cd_reset))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = previewColorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(previewColorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .graphicsLayer {
    alpha = animatedAlpha
},
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview do Player - Tamanho aumentado
            CustomThemePreviewCard(config = config)

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = previewColorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.custom_theme_colors)) },
                    modifier = Modifier
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.custom_theme_wallpaper)) },
                    modifier = Modifier
                )
            }

            // Conteúdo das tabs
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300, delayMillis = 50)) with
                            fadeOut(animationSpec = tween(200))
                },
                label = "tab_content"
            ) { tab ->
                when (tab) {
                    0 -> ColorPickerSection(
                        config = config,
                        viewModel = viewModel,
                        onCustomColorClick = { target ->
                            colorPickerTarget = target
                            showColorPickerDialog = true
                        }
                    )
                    1 -> WallpaperSection(
                        config = config,
                        viewModel = viewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==================== PREVIEW CARD ====================

// CustomThemeScreen.kt

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
            .height(260.dp)
            .scale(cardScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Apenas feedback */ },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.surface)
        ) {
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
                        // 🔥 BLUR NO BOX QUE ENVOLVE O ASYNCIMAGE
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(radius = (config.wallpaperBlur * 18f).dp)
                        ) {
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
                        }
                    } ?: Box(modifier = Modifier.fillMaxSize().background(colorScheme.surface))
                }
                WallpaperType.SERVER -> {
                    config.wallpaperUrl?.let { url ->
                        // 🔥 BLUR NO BOX QUE ENVOLVE O ASYNCIMAGE
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(radius = (config.wallpaperBlur * 18f).dp)
                        ) {
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
                        }
                    } ?: Box(modifier = Modifier.fillMaxSize().background(colorScheme.surface))
                }
            }

            // Overlay de escurecimento (dim)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = config.wallpaperDim * 0.5f))
            )

            PlayerPreviewContent(colorScheme = colorScheme)
        }
    }
}

@Composable
private fun PlayerPreviewContent(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Topo: informações da música
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Album Art com efeito de gradiente
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colorScheme.primary,
                                colorScheme.secondary
                            )
                        )
                    )
            ) {
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = colorScheme.onPrimary,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    "♪ Blinding Lights",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    "The Weeknd",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        // Meio: Barra de progresso com tempo
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Barra de progresso estilizada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    colorScheme.primary,
                                    colorScheme.secondary
                                )
                            )
                        )
                        .clip(RoundedCornerShape(3.dp))
                )
                // Bolinha do progresso
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .offset(x = (45 - 7).dp) // 45% da largura - metade do tamanho
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colorScheme.primary,
                                    colorScheme.secondary
                                )
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "1:30",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    "3:20",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        // Base: Controles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botões de controle
            listOf(
                Icons.Rounded.SkipPrevious,
                Icons.Rounded.PlayArrow,
                Icons.Rounded.SkipNext
            ).forEachIndexed { index, icon ->
                val controlInteractionSource = remember { MutableInteractionSource() }
                val isControlPressed by controlInteractionSource.collectIsPressedAsState()
                val controlScale by animateFloatAsState(
                    targetValue = if (isControlPressed) 0.85f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "control_scale_$index"
                )

                Surface(
                    shape = CircleShape,
                    color = colorScheme.primary.copy(
                        alpha = when (index) {
                            0 -> 0.15f
                            1 -> 0.25f
                            else -> 0.15f
                        }
                    ),
                    modifier = Modifier
                        .size(if (index == 1) 56.dp else 44.dp)
                        .scale(controlScale)
                        .clickable(
                            interactionSource = controlInteractionSource,
                            indication = null
                        ) { /* Apenas feedback */ }
                ) {
                    Icon(
                        if (index == 1) Icons.Rounded.PlayArrow else icon,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (index == 1) 14.dp else 10.dp)
                    )
                }
                Spacer(Modifier.width(if (index == 1) 20.dp else 12.dp))
            }
        }
        
        // Indicador de qualidade
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "♫ FLAC • 24bit",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ColorPickerSection(
    config: CustomThemeConfig,
    viewModel: CustomThemeViewModel,
    onCustomColorClick: ((Int) -> Unit) -> Unit
) {
    var selectedPreset by remember { mutableStateOf<String?>(null) }
    var showPresets by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.custom_theme_colors_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ===== CORES INDIVIDUAIS =====
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "✨ Cores Personalizadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                ColorPickerRow(
                    label = "Primary",
                    currentColor = config.primaryColor,
                    mainColors = MAIN_COLORS,
                    additionalColors = ADDITIONAL_COLORS,
                    onColorSelected = { 
                        selectedPreset = null 
                        viewModel.updatePrimaryColor(it) 
                    },
                    onCustomColorClick = { onCustomColorClick(viewModel::updatePrimaryColor) }
                )
                
                ColorPickerRow(
                    label = "Secondary",
                    currentColor = config.secondaryColor,
                    mainColors = MAIN_COLORS,
                    additionalColors = ADDITIONAL_COLORS,
                    onColorSelected = { 
                        selectedPreset = null 
                        viewModel.updateSecondaryColor(it) 
                    },
                    onCustomColorClick = { onCustomColorClick(viewModel::updateSecondaryColor) }
                )
                
                ColorPickerRow(
                    label = "Background",
                    currentColor = config.backgroundColor,
                    mainColors = MAIN_COLORS,
                    additionalColors = ADDITIONAL_COLORS,
                    onColorSelected = { 
                        selectedPreset = null 
                        viewModel.updateBackgroundColor(it) 
                    },
                    onCustomColorClick = { onCustomColorClick(viewModel::updateBackgroundColor) }
                )
                
                ColorPickerRow(
                    label = "On Primary (Texto)",
                    currentColor = config.onPrimaryColor,
                    mainColors = listOf(0xFFFFFFFF.toInt(), 0xFF000000.toInt()),
                    additionalColors = ADDITIONAL_COLORS,
                    onColorSelected = { 
                        selectedPreset = null 
                        viewModel.updateOnPrimaryColor(it) 
                    },
                    onCustomColorClick = { onCustomColorClick(viewModel::updateOnPrimaryColor) }
                )
                
                ColorPickerRow(
                    label = "On Surface (Texto)",
                    currentColor = config.onSurfaceColor,
                    mainColors = listOf(0xFFFFFFFF.toInt(), 0xFF000000.toInt()),
                    additionalColors = ADDITIONAL_COLORS,
                    onColorSelected = { 
                        selectedPreset = null 
                        viewModel.updateOnSurfaceColor(it) 
                    },
                    onCustomColorClick = { onCustomColorClick(viewModel::updateOnSurfaceColor) }
                    )
                }
            }
        }
    }

@Composable
private fun PresetItem(
    preset: ColorPreset,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val itemScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "preset_item_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .scale(itemScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onSelect() }
    ) {
        // Círculo com as cores
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            Color(preset.primaryColor),
                            Color(preset.secondaryColor),
                            Color(preset.accentColor),
                            Color(preset.primaryColor)
                        )
                    )
                )
                .then(
                    if (isSelected) {
                        Modifier.border(3.dp, Color.White, CircleShape)
                    } else Modifier
                )
        ) {
            // Ícone central
            Icon(
                preset.icon,
                contentDescription = preset.name,
                tint = Color(preset.onPrimaryColor).copy(alpha = 0.9f),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )
        }
        
        // Nome da predefinição
        Text(
            preset.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

@Composable
private fun ColorPickerRow(
    label: String,
    currentColor: Int,
    mainColors: List<Int>,
    additionalColors: List<Int>,
    onColorSelected: (Int) -> Unit,
    onCustomColorClick: () -> Unit
) {
    var showAdditional by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )

        // Cores principais
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mainColors) { color ->
                ColorItem(
                    color = color,
                    isSelected = color == currentColor,
                    onColorSelected = onColorSelected
                )
            }
        }

        // Cores adicionais com toggle
        AnimatedContent(
            targetState = showAdditional,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(200))
            }
        ) { show ->
            if (!show) {
                TextButton(
                    onClick = { showAdditional = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.ExpandMore, contentDescription = null)
                    Text(stringResource(R.string.custom_theme_more_colors))
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.custom_theme_additional_colors),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(additionalColors) { color ->
                            ColorItem(
                                color = color,
                                isSelected = color == currentColor,
                                onColorSelected = onColorSelected
                            )
                        }
                    }
                    TextButton(
                        onClick = { showAdditional = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.ExpandLess, contentDescription = null)
                        Text(stringResource(R.string.custom_theme_less_colors))
                    }
                }
            }
        }

        // Botão cor personalizada
        TextButton(
            onClick = onCustomColorClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.ColorLens, contentDescription = null)
            Text(stringResource(R.string.custom_theme_custom_color))
        }
    }
}

@Composable
private fun ColorItem(
    color: Int,
    isSelected: Boolean,
    onColorSelected: (Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val itemScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "color_item_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "color_item_border"
    )
    val size by animateDpAsState(
        targetValue = if (isSelected) 44.dp else 36.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "color_item_size"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(itemScale)
            .clip(CircleShape)
            .background(Color(color))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onColorSelected(color) }
            .then(
                if (isSelected) {
                    Modifier.border(borderWidth, Color.White, CircleShape)
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

// ==================== CUSTOM COLOR PICKER DIALOG (HSV) ====================

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
                    // Texto de contraste
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
                    // Matiz (Hue)
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

                    // Saturação (Saturation)
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

                    // Brilho (Brightness/Value)
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
        
        // Slider com gradiente
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

// ==================== WALLPAPER SECTION ====================

@Composable
private fun WallpaperSection(
    config: CustomThemeConfig,
    viewModel: CustomThemeViewModel
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(WallpaperCategory.ALL) }

    // Launcher para galeria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.saveWallpaperFromGallery(it.toString())
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.custom_theme_wallpaper_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tipo de Wallpaper
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WallpaperType.entries.forEach { type ->
                val isSelected = config.wallpaperType == type
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val chipScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.96f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "chip_scale_${type.name}"
                )

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setWallpaperType(type) },
                    label = {
                        Text(
                            when (type) {
                                WallpaperType.SOLID -> stringResource(R.string.wallpaper_type_solid)
                                WallpaperType.GALLERY -> stringResource(R.string.wallpaper_type_gallery)
                                WallpaperType.SERVER -> stringResource(R.string.wallpaper_type_server)
                            }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .scale(chipScale),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    interactionSource = interactionSource
                )
            }
        }

        when (config.wallpaperType) {
            WallpaperType.SOLID -> {
                ColorPickerRow(
                    label = stringResource(R.string.custom_theme_background_color),
                    currentColor = config.backgroundColor,
                    mainColors = MAIN_COLORS,
                    additionalColors = ADDITIONAL_COLORS + listOf(0xFF1E1234.toInt()),
                    onColorSelected = { viewModel.updateBackgroundColor(it) },
                    onCustomColorClick = { viewModel::updateBackgroundColor }
                )

                // Preview da cor
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(config.backgroundColor)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.custom_theme_wallpaper_preview),
                            color = Color(config.backgroundColor).contrastTextColor(),
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            WallpaperType.GALLERY -> {
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.wallpaper_select_from_gallery))
                }

                if (config.wallpaperUri != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = Uri.parse(config.wallpaperUri),
                            contentDescription = stringResource(R.string.custom_theme_selected_wallpaper),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Rounded.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.custom_theme_no_image_selected),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            WallpaperType.SERVER -> {
                Text(
                    stringResource(R.string.wallpaper_server_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Categorias com ícones Material
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WallpaperCategory.entries.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    when (category) {
                                        WallpaperCategory.ALL -> "Todos"
                                        WallpaperCategory.CARR -> "Carros"
                                        WallpaperCategory.ANIME -> "Anime"
                                        WallpaperCategory.SPACE -> "Espaço"
                                        WallpaperCategory.NEON -> "Neon"
                                        WallpaperCategory.OTRS -> "Outros"
                                    }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Wallpapers
                val wallpapers = WALLPAPER_CATEGORIES[selectedCategory] ?: emptyList()
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(wallpapers) { url ->
                        ServerWallpaperItem(
                            url = url,
                            isSelected = config.wallpaperUrl == url,
                            onSelect = { viewModel.setWallpaperFromServer(url) }
                        )
                    }
                }
            }
        }

        // Controles adicionais (blur e dim)
        if (config.wallpaperType != WallpaperType.SOLID) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                SliderWithLabel(
                    label = stringResource(R.string.wallpaper_blur),
                    value = config.wallpaperBlur,
                    onValueChange = { viewModel.setWallpaperBlur(it) },
                    valueRange = 0f..1f
                )
                SliderWithLabel(
                    label = stringResource(R.string.wallpaper_dim),
                    value = config.wallpaperDim,
                    onValueChange = { viewModel.setWallpaperDim(it) },
                    valueRange = 0f..0.8f
                )
            }
        }
    }
}

// ==================== SERVER WALLPAPER ITEM ====================

@Composable
private fun ServerWallpaperItem(
    url: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val itemScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "server_item_scale"
    )

    Box(
        modifier = Modifier
            .width(120.dp)
            .height(180.dp)
            .scale(itemScale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onSelect() }
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                } else Modifier
            )
    ) {
        // Shimmer deslizante melhorado
        if (isLoading) {
            ShimmerLoading(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp)
            )
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .listener(
                    onStart = { isLoading = true },
                    onSuccess = { _, _ -> isLoading = false },
                    onError = { _, _ -> isLoading = false }
                )
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Seleção
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ==================== SHIMMER LOADING MELHORADO ====================

@Composable
private fun ShimmerLoading(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val transition = rememberInfiniteTransition()
    val shimmerState by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Gray.copy(alpha = 0.3f),
                        Color.Gray.copy(alpha = 0.6f),
                        Color.Gray.copy(alpha = 0.3f)
                    ),
                    startX = shimmerState * 2f - 1f,
                    endX = shimmerState * 2f + 1f
                )
            )
    )
}

// ==================== SLIDER WITH LABEL ====================

@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column {
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
                "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ==================== UTILITY FUNCTIONS ====================

fun Color.contrastTextColor(): Color {
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
    return if (luminance > 0.5) Color.Black else Color.White
}

// Extensão para HSV - CORRIGIDA
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
        
        // 🔥 GARANTE QUE FIQUE ENTRE 0 E 360
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