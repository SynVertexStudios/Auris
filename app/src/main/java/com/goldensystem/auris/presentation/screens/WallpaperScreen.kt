// presentation/screens/WallpaperScreen.kt

package com.goldensystem.auris.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.goldensystem.auris.data.preferences.CustomThemeConfig
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.goldensystem.auris.R
import com.goldensystem.auris.data.preferences.WallpaperType
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel
import com.goldensystem.auris.ui.theme.customColorScheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================
// 1. CONFIGURAÇÃO DAS CATEGORIAS
// ============================================================
private val CATEGORY_CONFIG = mapOf(
    WallpaperCategory.ANIME to ("anime" to 7),
    WallpaperCategory.CARR  to ("carr" to 12),
    WallpaperCategory.NEON  to ("neon" to 7),
    WallpaperCategory.OTRS  to ("otrs" to 7),
    WallpaperCategory.SPACE to ("space" to 10)
)

private const val BASE_URL = "https://raw.githubusercontent.com/pereirasaymonsilva-a11y/Auris/main/assets/wallpaper"

val WALLPAPER_CATEGORIES = mutableMapOf<WallpaperCategory, List<String>>().apply {
    CATEGORY_CONFIG.forEach { (category, config) ->
        val (folder, count) = config
        val urls = (1..count).map { index ->
            "$BASE_URL/$folder/Wallpaper$index.jpg"
        }
        this[category] = urls
    }
    this[WallpaperCategory.ALL] = this.values.flatten()
}

enum class WallpaperCategory {
    ALL, SPACE, CARR, ANIME, NEON, OTRS
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WallpaperScreen(
    navController: NavController,
    viewModel: CustomThemeViewModel = hiltViewModel()
) {
    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf(WallpaperCategory.ALL) }
    
    // Debounce para salvar automaticamente
    var saveJob by remember { mutableStateOf<Job?>(null) }
    
    val colorScheme = remember(config) { customColorScheme(config, true) }

    // Launcher para galeria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.saveWallpaperFromGallery(it.toString())
        }
    }

    // Salvar com debounce
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.custom_theme_wallpaper_title),
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
            // Preview do wallpaper atual
            WallpaperPreviewCard(
                config = config,
                colorScheme = colorScheme
            )

            // Tipo de Wallpaper
            Text(
                stringResource(R.string.custom_theme_wallpaper_source),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

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
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary,
                            disabledSelectedContainerColor = colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        interactionSource = interactionSource
                    )
                }
            }

            // Conteúdo baseado no tipo
            when (config.wallpaperType) {
                WallpaperType.SOLID -> SolidWallpaperContent(
                    config = config,
                    viewModel = viewModel,
                    colorScheme = colorScheme
                )
                WallpaperType.GALLERY -> GalleryWallpaperContent(
                    config = config,
                    viewModel = viewModel,
                    galleryLauncher = galleryLauncher,
                    colorScheme = colorScheme
                )
                WallpaperType.SERVER -> ServerWallpaperContent(
                    config = config,
                    viewModel = viewModel,
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    colorScheme = colorScheme
                )
            }

            // Controles adicionais (blur e dim) - apenas para tipos com imagem
            if (config.wallpaperType != WallpaperType.SOLID) {
                Divider(color = colorScheme.surfaceVariant)
                
                Text(
                    stringResource(R.string.wallpaper_effects),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SliderWithLabel(
                        label = stringResource(R.string.wallpaper_blur),
                        value = config.wallpaperBlur,
                        onValueChange = { viewModel.setWallpaperBlur(it) },
                        valueRange = 0f..1f,
                        colorScheme = colorScheme
                    )
                    SliderWithLabel(
                        label = stringResource(R.string.wallpaper_dim),
                        value = config.wallpaperDim,
                        onValueChange = { viewModel.setWallpaperDim(it) },
                        valueRange = 0f..0.8f,
                        colorScheme = colorScheme
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==================== WALLPAPER PREVIEW CARD ====================

@Composable
private fun WallpaperPreviewCard(
    config: CustomThemeConfig,
    colorScheme: ColorScheme
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
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
        ) {
            when (config.wallpaperType) {
                WallpaperType.SOLID -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(config.backgroundColor))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.1f),
                                            Color.Transparent
                                        ),
                                        radius = 1000f,
                                        center = Offset(300f, 200f)
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.2f)
                                        ),
                                        radius = 800f,
                                        center = Offset(200f, 600f)
                                    )
                                )
                        )
                        Text(
                            stringResource(R.string.custom_theme_solid_color_wallpaper),
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(config.backgroundColor).contrastTextColor(),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
                WallpaperType.GALLERY -> {
                    config.wallpaperUri?.let { uri ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uri)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.custom_theme_selected_wallpaper),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: EmptyWallpaperPlaceholder(colorScheme)
                }
                WallpaperType.SERVER -> {
                    config.wallpaperUrl?.let { url ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(url)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.custom_theme_selected_wallpaper),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: EmptyWallpaperPlaceholder(colorScheme)
                }
            }

            // Overlay indicando preview
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            ),
                            startY = 0.6f
                        )
                    )
            )
            
            Text(
                stringResource(R.string.custom_theme_wallpaper_preview),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EmptyWallpaperPlaceholder(colorScheme: ColorScheme) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.Image,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.custom_theme_no_image_selected),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== SOLID WALLPAPER CONTENT ====================

@Composable
private fun SolidWallpaperContent(
    config: CustomThemeConfig,
    viewModel: CustomThemeViewModel,
    colorScheme: ColorScheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(R.string.custom_theme_background_color),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface
        )

        // Cores principais
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MAIN_COLORS) { color ->
                ColorItem(
                    color = color,
                    isSelected = color == config.backgroundColor,
                    onColorSelected = { viewModel.updateBackgroundColor(it) },
                    size = 44.dp
                )
            }
        }

        // Cores adicionais
        var showAdditional by remember { mutableStateOf(false) }
        
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
                        color = colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ADDITIONAL_COLORS) { color ->
                            ColorItem(
                                color = color,
                                isSelected = color == config.backgroundColor,
                                onColorSelected = { viewModel.updateBackgroundColor(it) },
                                size = 40.dp
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
    }
}

// ==================== GALLERY WALLPAPER CONTENT ====================

@Composable
private fun GalleryWallpaperContent(
    config: CustomThemeConfig,
    viewModel: CustomThemeViewModel,
    galleryLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Uri?>,
    colorScheme: ColorScheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary
            )
        ) {
            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.wallpaper_select_from_gallery))
        }

        if (config.wallpaperUri != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                AsyncImage(
                    model = Uri.parse(config.wallpaperUri),
                    contentDescription = stringResource(R.string.custom_theme_selected_wallpaper),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Botão para remover
            TextButton(
    onClick = { viewModel.resetWallpaper() },
    modifier = Modifier.fillMaxWidth(),
    colors = ButtonDefaults.textButtonColors(
        contentColor = colorScheme.error
    )
) {
    Icon(Icons.Rounded.Delete, contentDescription = null)
    Spacer(Modifier.width(8.dp))
    Text(stringResource(R.string.custom_theme_remove_wallpaper))
}
        }
    }
}

// ==================== SERVER WALLPAPER CONTENT ====================

@Composable
private fun ServerWallpaperContent(
    config: CustomThemeConfig,
    viewModel: CustomThemeViewModel,
    selectedCategory: WallpaperCategory,
    onCategoryChange: (WallpaperCategory) -> Unit,
    colorScheme: ColorScheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(R.string.wallpaper_server_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant
        )

        // Categorias
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
                    onClick = { onCategoryChange(category) },
                    label = {
                        Text(
                            when (category) {
                                WallpaperCategory.ALL -> stringResource(R.string.wallpaper_category_all)
                                WallpaperCategory.CARR -> stringResource(R.string.wallpaper_category_cars)
                                WallpaperCategory.ANIME -> stringResource(R.string.wallpaper_category_anime)
                                WallpaperCategory.SPACE -> stringResource(R.string.wallpaper_category_space)
                                WallpaperCategory.NEON -> stringResource(R.string.wallpaper_category_neon)
                                WallpaperCategory.OTRS -> stringResource(R.string.wallpaper_category_others)
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorScheme.primary,
                        selectedLabelColor = colorScheme.onPrimary
                    )
                )
            }
        }

        // Wallpapers
        val wallpapers = WALLPAPER_CATEGORIES[selectedCategory] ?: emptyList()
        
        if (wallpapers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.wallpaper_no_wallpapers),
                    color = colorScheme.onSurfaceVariant
                )
            }
        } else {
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
                        onSelect = { viewModel.setWallpaperFromServer(url) },
                        colorScheme = colorScheme
                    )
                }
            }
        }
    }
}

// ==================== SERVER WALLPAPER ITEM ====================

@Composable
private fun ServerWallpaperItem(
    url: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    colorScheme: ColorScheme
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
                    Modifier.border(3.dp, colorScheme.primary, RoundedCornerShape(16.dp))
                } else Modifier
            )
    ) {
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

// ==================== COMPONENTES REUTILIZÁVEIS ====================

@Composable
private fun ColorItem(
    color: Int,
    isSelected: Boolean,
    onColorSelected: (Int) -> Unit,
    size: androidx.compose.ui.unit.Dp = 36.dp
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

    Box(
        modifier = Modifier
            .size(size)
            .scale(itemScale)
            .clip(RoundedCornerShape(8.dp))
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

@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    colorScheme: ColorScheme
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = colorScheme.primary,
                activeTrackColor = colorScheme.primary
            )
        )
    }
}

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
                        Color.Gray.copy(alpha = 0.2f),
                        Color.Gray.copy(alpha = 0.5f),
                        Color.Gray.copy(alpha = 0.2f)
                    ),
                    startX = shimmerState * 2f - 1f,
                    endX = shimmerState * 2f + 1f
                )
            )
    )
}