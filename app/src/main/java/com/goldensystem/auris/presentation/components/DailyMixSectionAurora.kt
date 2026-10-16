package com.goldensystem.auris.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.goldensystem.auris.R
import com.goldensystem.auris.data.model.Song
import com.goldensystem.auris.presentation.components.subcomps.EnhancedSongListItem
import com.goldensystem.auris.presentation.viewmodel.PlayerViewModel
import com.goldensystem.auris.presentation.viewmodel.PlaylistViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun DailyMixSectionAurora(
    songs: ImmutableList<Song>,
    playerViewModel: PlayerViewModel,
    onClickOpen: () -> Unit = {},
    onNavigateToAlbum: (Song) -> Unit = {},
    onNavigateToArtist: (Song) -> Unit = {},
    onNavigateToGenre: (Song) -> Unit = {},
) {
    val playlistViewModel: PlaylistViewModel = hiltViewModel()
    val favoriteSongIds by playerViewModel.favoriteSongIds.collectAsStateWithLifecycle()
    val selectedSongForInfo by playerViewModel.selectedSongForInfo.collectAsStateWithLifecycle()
    val playlistUiState by playlistViewModel.uiState.collectAsStateWithLifecycle()
    val navBarCompactMode by playerViewModel.navBarCompactMode.collectAsStateWithLifecycle()
    val systemNavBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarHeightDp = resolveNavBarOccupiedHeight(systemNavBarInset, navBarCompactMode)
    var showSongInfoSheet by remember { mutableStateOf(false) }
    var showPlaylistBottomSheet by remember { mutableStateOf(false) }
    val dailyMixQueueName = stringResource(R.string.presentation_batch_g_daily_mix_queue_name)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        DailyMixCardAurora(
            songs = songs,
            playerViewModel = playerViewModel,
            onClickOpen = onClickOpen,
            onMoreOptionsClick = { song ->
                playerViewModel.selectSongForInfo(song)
                showSongInfoSheet = true
            }
        )
    }

    if (showSongInfoSheet && selectedSongForInfo != null) {
        val song = selectedSongForInfo!!
        SongInfoBottomSheet(
            song = song,
            isFavorite = favoriteSongIds.contains(song.id),
            onToggleFavorite = { playerViewModel.toggleFavoriteSpecificSong(song) },
            onDismiss = {
                showSongInfoSheet = false
                showPlaylistBottomSheet = false
            },
            onPlaySong = {
                playerViewModel.showAndPlaySong(
                    song = song,
                    contextSongs = songs,
                    queueName = dailyMixQueueName,
                    isVoluntaryPlay = false
                )
                showSongInfoSheet = false
            },
            onAddToQueue = {
                playerViewModel.addSongToQueue(song)
                showSongInfoSheet = false
            },
            onAddNextToQueue = {
                playerViewModel.addSongNextToQueue(song)
                showSongInfoSheet = false
            },
            onAddToPlayList = { showPlaylistBottomSheet = true },
            onDeleteFromDevice = playerViewModel::deleteFromDevice,
            onNavigateToAlbum = { onNavigateToAlbum(song); showSongInfoSheet = false },
            onNavigateToArtist = { onNavigateToArtist(song); showSongInfoSheet = false },
            onNavigateToGenre = { onNavigateToGenre(song); showSongInfoSheet = false },
            onEditSong = { newTitle, newArtist, newAlbum, newGenre, newLyrics, newTrackNumber, newDiscNumber, rgTrack, rgAlbum, coverArtUpdate ->
                playerViewModel.editSongMetadata(
                    song, newTitle, newArtist, newAlbum, newGenre, newLyrics,
                    newTrackNumber, newDiscNumber, rgTrack, rgAlbum, coverArtUpdate
                )
            },
            generateAiMetadata = { fields -> playerViewModel.generateAiMetadata(song, fields) },
            removeFromListTrigger = {}
        )

        if (showPlaylistBottomSheet) {
            PlaylistBottomSheet(
                playlistUiState = playlistUiState,
                songs = listOf(song),
                onDismiss = { showPlaylistBottomSheet = false },
                bottomBarHeight = bottomBarHeightDp,
                playerViewModel = playerViewModel,
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun DailyMixCardAurora(
    songs: ImmutableList<Song>,
    onClickOpen: () -> Unit,
    playerViewModel: PlayerViewModel,
    onMoreOptionsClick: (Song) -> Unit
) {
    val headerSongs = songs.take(3).toImmutableList()
    val visibleSongs = songs.take(4).toImmutableList()
    val cornerRadius = 30.dp

    val shape = AbsoluteSmoothCornerShape(
        cornerRadiusBR = cornerRadius,
        smoothnessAsPercentTL = 70,
        cornerRadiusTR = cornerRadius,
        smoothnessAsPercentTR = 70,
        cornerRadiusBL = cornerRadius,
        smoothnessAsPercentBL = 70,
        cornerRadiusTL = cornerRadius,
        smoothnessAsPercentBR = 70
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = shape,
                clip = false,
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.Transparent,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.20f)
                    )
                ),
                shape = shape
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            DailyMixHeaderAurora(thumbnails = headerSongs)
            Spacer(Modifier.height(10.dp))
            DailyMixSongListAurora(
                songs = visibleSongs,
                playbackQueue = songs,
                playerViewModel = playerViewModel,
                onMoreOptionsClick = onMoreOptionsClick
            )
            ViewAllDailyMixButtonAurora(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 10.dp),
                onClickOpen = onClickOpen,
            )
        }
    }
}

@Composable
fun DailyMixHeaderAurora(thumbnails: ImmutableList<Song>) {
    val titleStyle = rememberDailyMixTitleStyleAurora()
    val scheme = MaterialTheme.colorScheme

    val aurora = rememberInfiniteTransition(label = "dailyMixHeader")
    val phase by aurora.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(
                AbsoluteSmoothCornerShape(
                    cornerRadiusTL = 30.dp, smoothnessAsPercentTL = 70,
                    cornerRadiusTR = 30.dp, smoothnessAsPercentTR = 70,
                    cornerRadiusBL = 0.dp, smoothnessAsPercentBL = 0,
                    cornerRadiusBR = 0.dp, smoothnessAsPercentBR = 0,
                )
            )
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            scheme.primary,
                            scheme.tertiary
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        start = Offset(phase * size.width * 1.4f - size.width * 0.4f, 0f),
                        end = Offset(phase * size.width * 1.4f, size.height)
                    )
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.presentation_batch_g_daily_mix_heading),
                    style = titleStyle,
                    color = scheme.onPrimary
                )
                Text(
                    modifier = Modifier.padding(start = 1.dp, top = 2.dp),
                    text = stringResource(R.string.presentation_batch_g_daily_mix_based_on_history),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = scheme.onPrimary.copy(alpha = 0.88f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy((-22).dp)) {
                thumbnails.forEachIndexed { index, song ->
                    val size = when (index) {
                        0 -> 62.dp
                        1 -> 56.dp
                        else -> 60.dp
                    }
                    Box(
                        modifier = Modifier
                            .size(size)
                            .graphicsLayer {
                                rotationZ = when (index) {
                                    0 -> -8f
                                    1 -> 5f
                                    else -> -3f
                                }
                            }
                            .shadow(elevation = 12.dp, shape = CircleShape, clip = false)
                            .clip(CircleShape)
                            .border(
                                width = 2.5.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        Color.White.copy(alpha = 0.35f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        SmartImage(
                            model = song.albumArtUriString,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun DailyMixSongListAurora(
    songs: ImmutableList<Song>,
    playbackQueue: ImmutableList<Song>,
    playerViewModel: PlayerViewModel,
    onMoreOptionsClick: (Song) -> Unit
) {
    val dailyMixQueueName = stringResource(R.string.presentation_batch_g_daily_mix_queue_name)
    val stablePlayerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()
    val itemContainerColor = MaterialTheme.colorScheme.surfaceContainer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(24.dp)),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        songs.forEach { song ->
            EnhancedSongListItem(
                song = song,
                isCurrentSong = stablePlayerState.currentSong?.id == song.id,
                isPlaying = stablePlayerState.isPlaying && stablePlayerState.currentSong?.id == song.id,
                containerColorOverride = itemContainerColor,
                onMoreOptionsClick = onMoreOptionsClick,
                customShape = RoundedCornerShape(14.dp),
                showAlbumArt = false,
                onClick = {
                    playerViewModel.showAndPlaySong(
                        song = song,
                        contextSongs = playbackQueue,
                        queueName = dailyMixQueueName,
                        isVoluntaryPlay = false
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ViewAllDailyMixButtonAurora(
    modifier: Modifier = Modifier,
    onClickOpen: () -> Unit
) {
    FilledTonalButton(
        modifier = modifier,
        onClick = onClickOpen,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        shape = AbsoluteSmoothCornerShape(
            cornerRadiusTL = 14.dp,
            cornerRadiusTR = 14.dp,
            smoothnessAsPercentTL = 70,
            smoothnessAsPercentTR = 70,
            cornerRadiusBL = 60.dp,
            cornerRadiusBR = 60.dp,
            smoothnessAsPercentBL = 70,
            smoothnessAsPercentBR = 70
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.presentation_batch_g_daily_mix_see_all),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                painter = painterResource(R.drawable.rounded_arrow_forward_24),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun rememberDailyMixTitleStyleAurora(): TextStyle {
    return remember {
        TextStyle(
            fontFamily = FontFamily(
                Font(
                    resId = R.font.gflex_variable,
                    variationSettings = FontVariation.Settings(
                        FontVariation.weight(630),
                        FontVariation.width(136f),
                        FontVariation.grade(40),
                        FontVariation.Setting("ROND", 100f),
                        FontVariation.Setting("XTRA", 520f),
                        FontVariation.Setting("YOPQ", 90f),
                        FontVariation.Setting("YTLC", 505f)
                    )
                )
            ),
            fontWeight = FontWeight(630),
            fontSize = 22.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.35).sp
        )
    }
}
