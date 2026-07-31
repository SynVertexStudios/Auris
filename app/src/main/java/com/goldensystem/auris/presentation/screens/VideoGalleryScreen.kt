@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.goldensystem.auris.presentation.screens

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.SizeTransform
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavController
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import com.goldensystem.auris.presentation.components.LibrarySortBottomSheet
import com.goldensystem.auris.presentation.components.ExpressiveScrollBar
import com.goldensystem.auris.data.model.SortOption
import androidx.compose.foundation.Image
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.goldensystem.auris.R
import com.goldensystem.auris.data.model.QueueContext
import com.goldensystem.auris.data.model.VideoItem
import com.goldensystem.auris.data.model.VideoQueue
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel
import com.goldensystem.auris.presentation.viewmodel.GalleryUiState
import com.goldensystem.auris.presentation.viewmodel.SortMode
import com.goldensystem.auris.presentation.viewmodel.VideoGalleryViewModel
import com.goldensystem.auris.ui.theme.WallpaperBackground
import com.goldensystem.auris.utils.VideoQueueHolder
import com.goldensystem.auris.utils.VideoUtils
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

enum class NavigationDirection { FORWARD, BACK }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VideoGalleryScreen(
    onOpenPlayerWithQueue: (VideoQueue) -> Unit,
    onBack: () -> Unit,
    viewModel: VideoGalleryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val transitionKey = "${state.currentContext.name}_${state.showFoldersOnly}_${state.currentFolder ?: "root"}"
    
    var navDirection by remember { mutableStateOf(NavigationDirection.FORWARD) }
    
    val customThemeViewModel: CustomThemeViewModel = hiltViewModel()
    val config by customThemeViewModel.customThemeConfig.collectAsStateWithLifecycle()

    val permission = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.loadVideos()
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(permission)
        }
    }

    WallpaperBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!hasPermission) {
            PermissionScreen(onRequest = { permissionLauncher.launch(permission) })
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = if (config.isEnabled) Color.Transparent else MaterialTheme.colorScheme.background,
                topBar = {
                    GalleryTopBar(
                        state = state,
                        onSearchChange = viewModel::setSearchQuery,
                        onSortChange = viewModel::setSortMode,
                        onContextChange = viewModel::setContext,
                        onToggleShowFolders = viewModel::setShowFoldersOnly,
                        onBack = {
                            if (state.currentContext == QueueContext.FOLDER) {
                                navDirection = NavigationDirection.BACK
                                viewModel.exitFolder()
                            } else onBack()
                        }
                    )
                }
            ) { padding ->
                AnimatedContent(
                    targetState = transitionKey,
                    transitionSpec = {
                        when (navDirection) {
                            NavigationDirection.FORWARD -> {
                                slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                            }
                            NavigationDirection.BACK -> {
                                slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                            }
                        }.using(SizeTransform(clip = false))
                    }
                ) { _ ->
                    Box(modifier = Modifier.padding(padding)) {
                        when {
                            state.isLoading -> LoadingState()
                            state.errorMessage != null -> ErrorState(state.errorMessage!!) { viewModel.loadVideos() }
                            state.showFoldersOnly && state.folders.isEmpty() -> EmptyState()
                            state.displayVideos.isEmpty() && !state.showFoldersOnly && state.searchQuery.isNotBlank() -> EmptySearchState()
                            state.displayVideos.isEmpty() && !state.showFoldersOnly && state.searchQuery.isBlank() -> EmptyState()
                            else -> {
                                val showFeatured = !state.showFoldersOnly && state.searchQuery.isBlank() && state.currentContext != QueueContext.FOLDER
                                val listState = rememberLazyListState()
                                val isRefreshing by remember { mutableStateOf(false) }
                                val pullToRefreshState = rememberPullToRefreshState()
                                
                                val density = LocalDensity.current
                                val bottomPadding = with(density) { 16.dp }

                                PullToRefreshBox(
                                    isRefreshing = isRefreshing,
                                    onRefresh = { viewModel.loadVideos() },
                                    state = pullToRefreshState,
                                    modifier = Modifier.fillMaxSize(),
                                    indicator = {
    PullToRefreshDefaults.LoadingIndicator(
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        modifier = Modifier.align(Alignment.TopCenter)
    )
}
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        LazyColumn(
                                            state = listState,
                                            contentPadding = PaddingValues(
                                                start = 12.dp,
                                                end = if (listState.canScrollForward || listState.canScrollBackward) 22.dp else 12.dp,
                                                top = 12.dp,
                                                bottom = 12.dp
                                            ),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            if (state.showFoldersOnly) {
                                                val foldersChunked = state.folders.chunked(2)
                                                items(foldersChunked, key = { it.hashCode() }) { rowFolders ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        rowFolders.forEach { folder ->
                                                            Box(modifier = Modifier.weight(1f)) {
                                                                FolderItem(folder) { 
                                                                    navDirection = NavigationDirection.FORWARD
                                                                    viewModel.enterFolder(folder.path) 
                                                                }
                                                            }
                                                        }
                                                        repeat(2 - rowFolders.size) {
                                                            Spacer(modifier = Modifier.weight(1f))
                                                        }
                                                    }
                                                }
                                            } else {
                                                val videos = state.displayVideos
                                                
                                                if (showFeatured && videos.isNotEmpty()) {
                                                    val featured = viewModel.getFeaturedVideo()
                                                    if (featured != null) {
                                                        item {
                                                            FeaturedVideoItem(
                                                                video = featured,
                                                                viewModel = viewModel,
                                                                onClick = { queue ->
                                                                    viewModel.incrementViewCount(featured.id)
                                                                    VideoQueueHolder.setQueue(queue)
                                                                    onOpenPlayerWithQueue(queue)
                                                                }
                                                            )
                                                        }
                                                        val remainingVideos = videos.filter { it.id != featured.id }
                                                        val remainingChunked = remainingVideos.chunked(2)
                                                        items(remainingChunked, key = { it.hashCode() }) { rowVideos ->
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                            ) {
                                                                rowVideos.forEach { video ->
                                                                    Box(modifier = Modifier.weight(1f)) {
                                                                        VideoGridItem(video, viewModel) { queue ->
                                                                            viewModel.incrementViewCount(video.id)
                                                                            VideoQueueHolder.setQueue(queue)
                                                                            onOpenPlayerWithQueue(queue)
                                                                        }
                                                                    }
                                                                }
                                                                repeat(2 - rowVideos.size) {
                                                                    Spacer(modifier = Modifier.weight(1f))
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        val videosChunked = videos.chunked(2)
                                                        items(videosChunked, key = { it.hashCode() }) { rowVideos ->
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                            ) {
                                                                rowVideos.forEach { video ->
                                                                    Box(modifier = Modifier.weight(1f)) {
                                                                        VideoGridItem(video, viewModel) { queue ->
                                                                            viewModel.incrementViewCount(video.id)
                                                                            VideoQueueHolder.setQueue(queue)
                                                                            onOpenPlayerWithQueue(queue)
                                                                        }
                                                                    }
                                                                }
                                                                repeat(2 - rowVideos.size) {
                                                                    Spacer(modifier = Modifier.weight(1f))
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    val videosChunked = videos.chunked(2)
                                                    items(videosChunked, key = { it.hashCode() }) { rowVideos ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                        ) {
                                                            rowVideos.forEach { video ->
                                                                Box(modifier = Modifier.weight(1f)) {
                                                                    VideoGridItem(video, viewModel) { queue ->
                                                                        viewModel.incrementViewCount(video.id)
                                                                        VideoQueueHolder.setQueue(queue)
                                                                        onOpenPlayerWithQueue(queue)
                                                                    }
                                                                }
                                                            }
                                                            repeat(2 - rowVideos.size) {
                                                                Spacer(modifier = Modifier.weight(1f))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        ExpressiveScrollBar(
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .padding(end = 4.dp, top = 16.dp, bottom = bottomPadding),
                                            listState = listState
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionScreen(onRequest: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.padding(24.dp).fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.gallery_permission_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.gallery_permission_message), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRequest,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.gallery_permission_button), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryTopBar(
    state: GalleryUiState,
    onSearchChange: (String) -> Unit,
    onSortChange: (SortMode) -> Unit,
    onContextChange: (QueueContext) -> Unit,
    onToggleShowFolders: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var showSortSheet by remember { mutableStateOf(false) }
    val isInFolder = state.currentContext == QueueContext.FOLDER

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            CenterAlignedTopAppBar(
                title = {
                    if (isInFolder) {
                        Text(text = state.currentFolder?.substringAfterLast("/") ?: stringResource(R.string.gallery_folder_fallback), fontWeight = FontWeight.SemiBold)
                    } else {
                        Text(stringResource(R.string.gallery_title), fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    if (isInFolder || state.currentContext == QueueContext.FOLDER) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.gallery_back))
                        }
                    }
                },
                actions = {
                    if (state.currentContext != QueueContext.RECENT) {
                        IconButton(onClick = { showSortSheet = true }) {
                            Icon(Icons.Filled.Sort, null)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text(stringResource(R.string.gallery_search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                )
            )

            if (state.currentContext != QueueContext.FOLDER && state.searchQuery.isBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isAllSelected = !state.showFoldersOnly && state.currentContext == QueueContext.ALL
                    val isRecentSelected = !state.showFoldersOnly && state.currentContext == QueueContext.RECENT
                    val isFoldersSelected = state.showFoldersOnly

                    FilterButton(
                        selected = isAllSelected,
                        onClick = {
                            onContextChange(QueueContext.ALL)
                            onToggleShowFolders(false)
                        },
                        text = stringResource(R.string.gallery_tab_all)
                    )

                    FilterButton(
                        selected = isRecentSelected,
                        onClick = {
                            onContextChange(QueueContext.RECENT)
                            onToggleShowFolders(false)
                        },
                        text = stringResource(R.string.gallery_tab_recent)
                    )

                    FilterButton(
                        selected = isFoldersSelected,
                        onClick = {
                            if (!state.showFoldersOnly) onToggleShowFolders(true)
                        },
                        text = stringResource(R.string.gallery_tab_folders)
                    )
                }
            }
        }
    }

    if (showSortSheet) {
        LibrarySortBottomSheet(
            title = stringResource(R.string.gallery_sort),
            options = VIDEO_SORT_OPTIONS,
            selectedOption = when (state.sortMode) {
                SortMode.NAME_ASC -> SortOption.SongTitleAZ
                SortMode.NAME_DESC -> SortOption.SongTitleZA
                SortMode.DATE_DESC -> SortOption.SongDateAdded
                SortMode.DATE_ASC -> SortOption.SongDateAddedAsc
                SortMode.DURATION_DESC -> SortOption.SongDuration
                SortMode.DURATION_ASC -> SortOption.SongDurationAsc
                SortMode.SIZE_ASC,
                SortMode.SIZE_DESC -> SortOption.SongDefaultOrder
            },
            onDismiss = { showSortSheet = false },
            onOptionSelected = { option ->
                showSortSheet = false
                when (option) {
                    SortOption.SongTitleAZ -> onSortChange(SortMode.NAME_ASC)
                    SortOption.SongTitleZA -> onSortChange(SortMode.NAME_DESC)
                    SortOption.SongDateAdded -> onSortChange(SortMode.DATE_DESC)
                    SortOption.SongDateAddedAsc -> onSortChange(SortMode.DATE_ASC)
                    SortOption.SongDuration -> onSortChange(SortMode.DURATION_DESC)
                    SortOption.SongDurationAsc -> onSortChange(SortMode.DURATION_ASC)
                    else -> {}
                }
            },
            onDirectionToggle = { option ->
                when (option) {
                    SortOption.SongTitleAZ -> onSortChange(SortMode.NAME_ASC)
                    SortOption.SongTitleZA -> onSortChange(SortMode.NAME_DESC)
                    SortOption.SongDateAdded -> onSortChange(SortMode.DATE_DESC)
                    SortOption.SongDateAddedAsc -> onSortChange(SortMode.DATE_ASC)
                    SortOption.SongDuration -> onSortChange(SortMode.DURATION_DESC)
                    SortOption.SongDurationAsc -> onSortChange(SortMode.DURATION_ASC)
                    else -> {}
                }
            }
        )
    }
}

@Composable
private fun RowScope.FilterButton(
    selected: Boolean,
    onClick: () -> Unit,
    text: String
) {
    val shape = RoundedCornerShape(50.dp)
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
    modifier = Modifier
    .weight(1f)
    .fillMaxWidth(),
        shape = shape,
        color = containerColor,
        tonalElevation = if (selected) 6.dp else 2.dp,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun FeaturedVideoItem(
    video: VideoItem,
    viewModel: VideoGalleryViewModel,
    onClick: (VideoQueue) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, animationSpec = spring(dampingRatio = 0.5f))
    val glowAlpha by animateFloatAsState(targetValue = if (isPressed) 0.8f else 0.2f, animationSpec = tween(200))

    var thumbnail by remember { mutableStateOf<ImageBitmap?>(null) }
    
    LaunchedEffect(video.id) {
        withContext(Dispatchers.IO) {
            val bitmap = viewModel.getVideoThumbnail(video.id)
            thumbnail = bitmap?.asImageBitmap()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick(viewModel.buildQueue(video))
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 12.dp else 4.dp)
    ) {
        Box {
            Box(
                Modifier.matchParentSize().background(
                    Brush.radialGradient(
                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha), Color.Transparent),
                        radius = 0.9f
                    )
                )
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)), startY = 0.7f)
                )
            )

            Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                    if (thumbnail != null) {
                        Image(
                            bitmap = thumbnail!!,
                            contentDescription = video.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.VideoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)))))
                    CustomPlayIcon(modifier = Modifier.align(Alignment.Center).size(36.dp), alpha = if (isPressed) 0.9f else 0.7f)
                }
                Column(modifier = Modifier.weight(0.4f).fillMaxHeight().padding(12.dp), verticalArrangement = Arrangement.Center) {
                    Text(
                        stringResource(R.string.gallery_featured),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(video.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(video.durationFormatted, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoGridItem(
    video: VideoItem,
    viewModel: VideoGalleryViewModel,
    onClick: (VideoQueue) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, animationSpec = spring(dampingRatio = 0.5f))
    val elevation by animateDpAsState(targetValue = if (isPressed) 8.dp else 2.dp, animationSpec = tween(100))
    val glowAlpha by animateFloatAsState(targetValue = if (isPressed) 0.5f else 0f, animationSpec = tween(150))

    var thumbnail by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingThumbnail by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(video.id) {
        isLoadingThumbnail = true
        withContext(Dispatchers.IO) {
            val bitmap = viewModel.getVideoThumbnail(video.id)
            thumbnail = bitmap?.asImageBitmap()
        }
        isLoadingThumbnail = false
    }

    val isRecent = remember(video.dateAddedMs) {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)
        video.dateAddedMs > sevenDaysAgo
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick(viewModel.buildQueue(video))
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box {
            Box(
                Modifier.matchParentSize().background(
                    Brush.radialGradient(
                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha), Color.Transparent),
                        radius = 0.8f
                    )
                )
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)), startY = 0.6f)
                )
            )

            Box(modifier = Modifier.aspectRatio(16f / 9f)) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!,
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.VideoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)))))
                
                Text(
                    video.durationFormatted,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                
                CustomPlayIcon(modifier = Modifier.align(Alignment.Center).size(32.dp), alpha = if (isPressed) 0.9f else 0.7f)
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    video.title, 
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis, 
                    modifier = Modifier.weight(1f)
                )
                if (isRecent) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), contentColor = MaterialTheme.colorScheme.onPrimary) {
                        Text(stringResource(R.string.gallery_new), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomPlayIcon(modifier: Modifier = Modifier, alpha: Float = 0.8f) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val triangleSize = width * 0.6f
        val offsetX = (width - triangleSize) / 2
        val offsetY = (height - triangleSize) / 2

        val path = Path().apply {
            moveTo(offsetX, offsetY)
            lineTo(offsetX + triangleSize, offsetY + triangleSize / 2)
            lineTo(offsetX, offsetY + triangleSize)
            close()
        }
        drawPath(path, color = Color.White.copy(alpha = alpha))
        drawCircle(
            color = primaryColor.copy(alpha = alpha * 0.5f),
            radius = width * 0.7f,
            center = Offset(width / 2, height / 2)
        )
    }
}

@Composable
private fun FolderItem(folder: VideoUtils.FolderInfo, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, animationSpec = spring(dampingRatio = 0.5f))
    val elevation by animateDpAsState(targetValue = if (isPressed) 6.dp else 1.dp, animationSpec = tween(100))

    Card(
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }.clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).background(
                Brush.radialGradient(
                    listOf(Color.Transparent, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)),
                    radius = 0.8f
                )
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Folder, null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(folder.name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${folder.videoCount} ${stringResource(R.string.gallery_video_count_suffix)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LoadingState() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.gallery_retry)) }
    }
}

@Composable
private fun EmptyState() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(stringResource(R.string.gallery_empty), style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun EmptySearchState() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(stringResource(R.string.gallery_empty_search), style = MaterialTheme.typography.titleMedium)
}

val VIDEO_SORT_OPTIONS = listOf(
    SortOption.SongTitleAZ,
    SortOption.SongTitleZA,
    SortOption.SongDateAdded,
    SortOption.SongDateAddedAsc,
    SortOption.SongDuration,
    SortOption.SongDurationAsc
)