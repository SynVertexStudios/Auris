// presentation/screens/WallpaperScreen.kt

package com.goldensystem.auris.presentation.screens

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.with
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.goldensystem.auris.R
import com.goldensystem.auris.data.preferences.CustomThemeConfig
import com.goldensystem.auris.data.preferences.WallpaperType
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel
import com.goldensystem.auris.ui.theme.customColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================
// CONFIGURAÇÃO DAS CATEGORIAS
// ============================================================

private val CATEGORY_CONFIG = mapOf(
    WallpaperCategory.ANIME to ("anime" to 7),
    WallpaperCategory.CARR to ("carr" to 12),
    WallpaperCategory.NEON to ("neon" to 7),
    WallpaperCategory.OTRS to ("otrs" to 7),
    WallpaperCategory.SPACE to ("space" to 10)
)

private const val BASE_URL =
    "https://raw.githubusercontent.com/pereirasaymonsilva-a11y/Auris/main/assets/wallpaper"

val WALLPAPER_CATEGORIES = mutableMapOf<WallpaperCategory, List<String>>().apply {
    CATEGORY_CONFIG.forEach { (category, config) ->
        val (folder, count) = config

        this[category] = (1..count).map { index ->
            "$BASE_URL/$folder/Wallpaper$index.jpg"
        }
    }

    this[WallpaperCategory.ALL] = values.flatten()
}

enum class WallpaperCategory {
    ALL,
    SPACE,
    CARR,
    ANIME,
    NEON,
    OTRS
}

// ============================================================
// TELA PRINCIPAL
// ============================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WallpaperScreen(
    navController: NavController,
    viewModel: CustomThemeViewModel = hiltViewModel()
) {
    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedCategory by remember {
        mutableStateOf(WallpaperCategory.ALL)
    }

    val colorScheme = remember(config) {
        customColorScheme(config, true)
    }

    // --------------------------------------------------------
    // Galeria
    // --------------------------------------------------------

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.saveWallpaperFromGallery(it.toString())
        }
    }

    // --------------------------------------------------------
    // Salvamento automático com debounce
    // --------------------------------------------------------

    LaunchedEffect(config) {
        delay(800)
        viewModel.saveCustomTheme()
    }

    // ========================================================
    // UI
    // ========================================================

    Scaffold(
        containerColor = colorScheme.background,

        topBar = {
            WallpaperTopBar(
                colorScheme = colorScheme,
                onBack = {
                    scope.launch {
                        viewModel.saveCustomTheme()
                        navController.popBackStack()
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ------------------------------------------------
            // Preview
            // ------------------------------------------------

            WallpaperPreviewCard(
                config = config,
                colorScheme = colorScheme
            )

            // ------------------------------------------------
            // Fonte
            // ------------------------------------------------

            SectionTitle(
                title = stringResource(R.string.custom_theme_wallpaper_source),
                colorScheme = colorScheme
            )

            WallpaperTypeSelector(
                config = config,
                viewModel = viewModel,
                colorScheme = colorScheme
            )

            // ------------------------------------------------
            // Conteúdo
            // ------------------------------------------------

            AnimatedContent(
                targetState = config.wallpaperType,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(220)
                    ) with fadeOut(
                        animationSpec = tween(150)
                    )
                },
                label = "wallpaper_type_content"
            ) { type ->

                when (type) {

                    WallpaperType.SOLID -> {
                        SolidWallpaperContent(
                            config = config,
                            viewModel = viewModel,
                            colorScheme = colorScheme
                        )
                    }

                    WallpaperType.GALLERY -> {
                        GalleryWallpaperContent(
                            config = config,
                            viewModel = viewModel,
                            galleryLauncher = galleryLauncher,
                            colorScheme = colorScheme
                        )
                    }

                    WallpaperType.SERVER -> {
                        ServerWallpaperContent(
                            config = config,
                            viewModel = viewModel,
                            selectedCategory = selectedCategory,
                            onCategoryChange = {
                                selectedCategory = it
                            },
                            colorScheme = colorScheme
                        )
                    }
                }
            }

            // ------------------------------------------------
            // Efeitos
            // ------------------------------------------------

            AnimatedVisibility(
                visible = config.wallpaperType != WallpaperType.SOLID,
                enter = fadeIn() + scaleIn(initialScale = 0.98f),
                exit = fadeOut()
            ) {

                WallpaperEffects(
                    config = config,
                    viewModel = viewModel,
                    colorScheme = colorScheme
                )
            }
        Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

// ============================================================
// TOP BAR
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WallpaperTopBar(
    colorScheme: ColorScheme,
    onBack: () -> Unit
) {
    Surface(
        color = colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(
                        R.string.custom_theme_wallpaper_title
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(
                            R.string.auth_cd_back
                        ),
                        tint = colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

// ============================================================
// SECTION TITLE
// ============================================================

@Composable
private fun SectionTitle(
    title: String,
    colorScheme: ColorScheme
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = colorScheme.onSurface
    )
}

// ============================================================
// WALLPAPER TYPE SELECTOR
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WallpaperTypeSelector(
    config: CustomThemeConfig,
    viewModel: CustomThemeViewModel,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        WallpaperType.entries.forEach { type ->

            val isSelected = config.wallpaperType == type

            val interactionSource = remember {
                MutableInteractionSource()
            }

            val isPressed by interactionSource
                .collectIsPressedAsState()

            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.96f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "type_scale_${type.name}"
            )

            FilterChip(
                selected = isSelected,
                onClick = {
                    viewModel.setWallpaperType(type)
                },
                modifier = Modifier
                    .weight(1f)
                    .scale(scale),
                label = {
                    Text(
                        when (type) {
                            WallpaperType.SOLID ->
                                stringResource(
                                    R.string.wallpaper_type_solid
                                )

                            WallpaperType.GALLERY ->
                                stringResource(
                                    R.string.wallpaper_type_gallery
                                )

                            WallpaperType.SERVER ->
                                stringResource(
                                    R.string.wallpaper_type_server
                                )
                        },
                        maxLines = 1
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorScheme.primary,
                    selectedLabelColor = colorScheme.onPrimary,
                    selectedLeadingIconColor = colorScheme.onPrimary,
                    labelColor = colorScheme.onSurfaceVariant
                ),
                interactionSource = interactionSource
            )
        }
    }
}

// ============================================================
// PREVIEW
// ============================================================

@Composable
private fun WallpaperPreviewCard(
    config: CustomThemeConfig,
    colorScheme: ColorScheme
) {
    val context = LocalContext.current

    val previewShape = RoundedCornerShape(28.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(215.dp),
        shape = previewShape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // ------------------------------------------------
            // Background
            // ------------------------------------------------

            when (config.wallpaperType) {

                WallpaperType.SOLID -> {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color(config.backgroundColor)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.Transparent
                                    ),
                                    radius = 900f,
                                    center = Offset(
                                        350f,
                                        0f
                                    )
                                )
                            )
                    )
                }

                WallpaperType.GALLERY -> {

                    if (config.wallpaperUri != null) {

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(config.wallpaperUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(
                                R.string.custom_theme_selected_wallpaper
                            ),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                    } else {
                        EmptyWallpaperPlaceholder(
                            colorScheme = colorScheme
                        )
                    }
                }

                WallpaperType.SERVER -> {

                    if (config.wallpaperUrl != null) {

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(config.wallpaperUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(
                                R.string.custom_theme_selected_wallpaper
                            ),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                    } else {
                        EmptyWallpaperPlaceholder(
                            colorScheme = colorScheme
                        )
                    }
                }
            }

            // ------------------------------------------------
            // Dim
            // ------------------------------------------------

            if (config.wallpaperType != WallpaperType.SOLID) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(
                                alpha = config.wallpaperDim
                            )
                        )
                )
            }

            // ------------------------------------------------
            // Bottom gradient
            // ------------------------------------------------

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // ------------------------------------------------
            // Preview label
            // ------------------------------------------------

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.45f)
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(7.dp)
                    )

                    Text(
                        text = stringResource(
                            R.string.custom_theme_wallpaper_preview
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ============================================================
// EMPTY PREVIEW
// ============================================================

@Composable
private fun EmptyWallpaperPlaceholder(
    colorScheme: ColorScheme
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = colorScheme.surface
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Rounded.Image,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = stringResource(
                    R.string.custom_theme_no_image_selected
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// SOLID WALLPAPER
// ============================================================

@Composable
private fun SolidWallpaperContent(
    config: CustomThemeConfig,
    viewModel: CustomThemeViewModel,
    colorScheme: ColorScheme
) {

    var showAdditional by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Text(
            text = stringResource(
                R.string.custom_theme_background_color
            ),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(
                horizontal = 2.dp
            )
        ) {

            items(MAIN_COLORS) { color ->

                ColorItem(
                    color = color,
                    isSelected = color == config.backgroundColor,
                    onColorSelected = {
                        viewModel.updateBackgroundColor(it)
                    },
                    size = 46.dp
                )
            }
        }

        if (!showAdditional) {

            OutlinedButton(
                onClick = {
                    showAdditional = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp)
            ) {

                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = null
                )

                Spacer(
                    Modifier.width(7.dp)
                )

                Text(
                    stringResource(
                        R.string.custom_theme_more_colors
                    )
                )
            }

        } else {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = stringResource(
                        R.string.custom_theme_additional_colors
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(
                        horizontal = 2.dp
                    )
                ) {

                    items(ADDITIONAL_COLORS) { color ->

                        ColorItem(
                            color = color,
                            isSelected =
                                color == config.backgroundColor,
                            onColorSelected = {
                                viewModel.updateBackgroundColor(it)
                            },
                            size = 42.dp
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        showAdditional = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                ) {

                    Icon(
                        Icons.Rounded.ExpandLess,
                        contentDescription = null
                    )

                    Spacer(
                        Modifier.width(7.dp)
                    )

                    Text(
                        stringResource(
                            R.string.custom_theme_less_colors
                        )
                    )
                }
            }
        }
    }
}

// ============================================================
// GALLERY WALLPAPER
// ============================================================

@Composable
private fun GalleryWallpaperContent(
    config: CustomThemeConfig,
    viewModel: CustomThemeViewModel,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    colorScheme: ColorScheme
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Button(
            onClick = {
                galleryLauncher.launch("image/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            )
        ) {

            Icon(
                Icons.Rounded.PhotoLibrary,
                contentDescription = null
            )

            Spacer(
                Modifier.width(9.dp)
            )

            Text(
                stringResource(
                    R.string.wallpaper_select_from_gallery
                ),
                fontWeight = FontWeight.SemiBold
            )
        }

        AnimatedVisibility(
            visible = config.wallpaperUri != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    )
                ) {

                    AsyncImage(
                        model = Uri.parse(
                            config.wallpaperUri
                        ),
                        contentDescription = stringResource(
                            R.string.custom_theme_selected_wallpaper
                        ),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.resetWallpaper()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.error
                    )
                ) {

                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = null
                    )

                    Spacer(
                        Modifier.width(7.dp)
                    )

                    Text(
                        stringResource(
                            R.string.custom_theme_remove_wallpaper
                        )
                    )
                }
            }
        }
    }
}

// ============================================================
// SERVER WALLPAPER
// ============================================================
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ServerWallpaperContent(
    config: CustomThemeConfig,
    viewModel: CustomThemeViewModel,
    selectedCategory: WallpaperCategory,
    onCategoryChange: (WallpaperCategory) -> Unit,
    colorScheme: ColorScheme
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Text(
            text = stringResource(
                R.string.wallpaper_server_subtitle
            ),
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant
        )

        // ----------------------------------------------------
        // Categorias
        // ----------------------------------------------------

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            WallpaperCategory.entries.forEach { category ->

                val isSelected =
                    selectedCategory == category

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onCategoryChange(category)
                    },
                    label = {

                        Text(
                            when (category) {

                                WallpaperCategory.ALL ->
                                    stringResource(
                                        R.string.wallpaper_category_all
                                    )

                                WallpaperCategory.CARR ->
                                    stringResource(
                                        R.string.wallpaper_category_cars
                                    )

                                WallpaperCategory.ANIME ->
                                    stringResource(
                                        R.string.wallpaper_category_anime
                                    )

                                WallpaperCategory.SPACE ->
                                    stringResource(
                                        R.string.wallpaper_category_space
                                    )

                                WallpaperCategory.NEON ->
                                    stringResource(
                                        R.string.wallpaper_category_neon
                                    )

                                WallpaperCategory.OTRS ->
                                    stringResource(
                                        R.string.wallpaper_category_others
                                    )
                            }
                        )
                    },
                    leadingIcon = if (isSelected) {

                        {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                    } else {
                        null
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor =
                            colorScheme.primary,
                        selectedLabelColor =
                            colorScheme.onPrimary,
                        selectedLeadingIconColor =
                            colorScheme.onPrimary
                    )
                )
            }
        }

        // ----------------------------------------------------
        // Wallpapers
        // ----------------------------------------------------

        val wallpapers =
            WALLPAPER_CATEGORIES[selectedCategory]
                ?: emptyList()

        AnimatedContent(
            targetState = wallpapers,
            transitionSpec = {
                fadeIn(tween(200)) with
                        fadeOut(tween(120))
            },
            label = "wallpaper_list"
        ) { currentWallpapers ->

            if (currentWallpapers.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = stringResource(
                            R.string.wallpaper_no_wallpapers
                        ),
                        color = colorScheme.onSurfaceVariant
                    )
                }

            } else {

                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp),
                    contentPadding =
                        PaddingValues(horizontal = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(205.dp)
                ) {

                    items(
                        items = currentWallpapers,
                        key = { it }
                    ) { url ->

                        ServerWallpaperItem(
                            url = url,
                            isSelected =
                                config.wallpaperUrl == url,
                            onSelect = {
                                viewModel
                                    .setWallpaperFromServer(url)
                            },
                            colorScheme = colorScheme
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// SERVER WALLPAPER ITEM
// ============================================================

@Composable
private fun ServerWallpaperItem(
    url: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    colorScheme: ColorScheme
) {

    var isLoading by remember {
        mutableStateOf(true)
    }

    var hasError by remember {
        mutableStateOf(false)
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource
        .collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.94f
            isSelected -> 1.01f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "wallpaper_item_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = tween(180),
        label = "wallpaper_border"
    )

    Box(
        modifier = Modifier
            .width(130.dp)
            .height(195.dp)
            .scale(scale)
            .clip(RoundedCornerShape(19.dp))
            .background(colorScheme.surfaceVariant)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = borderWidth,
                        color = colorScheme.primary,
                        shape = RoundedCornerShape(19.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelect
            )
    ) {

        if (isLoading) {

            ShimmerLoading(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(19.dp)
            )
        }

        if (hasError) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    Icons.Rounded.BrokenImage,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(35.dp)
                )
            }
        }

        AsyncImage(
            model = ImageRequest.Builder(
                LocalContext.current
            )
                .data(url)
                .crossfade(300)
                .listener(
                    onStart = {
                        isLoading = true
                        hasError = false
                    },
                    onSuccess = { _, _ ->
                        isLoading = false
                        hasError = false
                    },
                    onError = { _, _ ->
                        isLoading = false
                        hasError = true
                    }
                )
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(
                    if (isLoading) 0f else 1f
                ),
            contentScale = ContentScale.Crop
        )

        // ----------------------------------------------------
        // Gradiente
        // ----------------------------------------------------

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f)
                        )
                    )
                )
        )

        // ----------------------------------------------------
        // Selecionado
        // ----------------------------------------------------

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut(),
            modifier = Modifier.align(
                Alignment.Center
            )
        ) {

            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = colorScheme.primary
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(27.dp)
                    )
                }
            }
        }
    }
}

// ============================================================
// EFEITOS
// ============================================================

@Composable
private fun WallpaperEffects(
    config: CustomThemeConfig,
    viewModel: CustomThemeViewModel,
    colorScheme: ColorScheme
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        HorizontalDivider(
            color = colorScheme.outlineVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = colorScheme.primaryContainer
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(
                Modifier.width(10.dp)
            )

            Text(
                text = stringResource(
                    R.string.wallpaper_effects
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }

        SliderWithLabel(
            label = stringResource(
                R.string.wallpaper_blur
            ),
            value = config.wallpaperBlur,
            onValueChange = {
                viewModel.setWallpaperBlur(it)
            },
            valueRange = 0f..1f,
            colorScheme = colorScheme
        )

        SliderWithLabel(
            label = stringResource(
                R.string.wallpaper_dim
            ),
            value = config.wallpaperDim,
            onValueChange = {
                viewModel.setWallpaperDim(it)
            },
            valueRange = 0f..0.8f,
            colorScheme = colorScheme
        )
    }
}

// ============================================================
// COLOR ITEM
// ============================================================

@Composable
private fun ColorItem(
    color: Int,
    isSelected: Boolean,
    onColorSelected: (Int) -> Unit,
    size: Dp = 36.dp
) {

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource
        .collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "color_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = tween(180),
        label = "color_border"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(color))
            .then(
                if (isSelected) {
                    Modifier.border(
                        borderWidth,
                        Color.White,
                        RoundedCornerShape(10.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onColorSelected(color)
            },
        contentAlignment = Alignment.Center
    ) {

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(tween(150)) +
                    scaleIn(
                        initialScale = 0.5f,
                        animationSpec = spring(
                            dampingRatio =
                                Spring.DampingRatioMediumBouncy
                        )
                    ),
            exit = fadeOut(tween(100))
        ) {

            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.18f)
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color(color)
                            .contrastTextColor(),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// ============================================================
// SLIDER
// ============================================================

@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    colorScheme: ColorScheme
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colorScheme.surfaceVariant
            ) {

                Text(
                    text = "${(value * 100).toInt()}%",
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
            }
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = colorScheme.primary,
                activeTrackColor = colorScheme.primary,
                inactiveTrackColor =
                    colorScheme.surfaceVariant
            )
        )
    }
}

// ============================================================
// SHIMMER
// ============================================================

@Composable
private fun ShimmerLoading(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {

    val transition = rememberInfiniteTransition(
        label = "shimmer_transition"
    )

    val shimmerState by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Gray.copy(alpha = 0.16f),
                        Color.Gray.copy(alpha = 0.38f),
                        Color.Gray.copy(alpha = 0.16f)
                    ),
                    startX = shimmerState * 500f,
                    endX = shimmerState * 500f + 300f
                )
            )
    )
}