package com.goldensystem.auris.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.goldensystem.auris.data.model.Song
import com.goldensystem.auris.presentation.viewmodel.StablePlayerState
import com.goldensystem.auris.ui.theme.LocalAurisDarkTheme

/*
 * ============================================================
 * PREMIUM PLAYER
 * ============================================================
 *
 * Esta camada é propositalmente visual.
 *
 * A lógica de:
 * - drag
 * - predictive back
 * - queue
 * - cast
 * - Media3
 * - ViewModel
 * - sheet state
 *
 * continua no UnifiedPlayerSheetV2.
 */

@Composable
fun PremiumPlayerSurface(
    modifier: Modifier = Modifier,
    currentSong: Song?,
    playerState: StablePlayerState,
    expansionFraction: Float,
    albumColor: Color,
    backgroundColor: Color,
    isFavorite: Boolean,
    isPreparing: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onFavorite: () -> Unit,
    onQueue: () -> Unit,
    onMore: () -> Unit
) {
    val isDark = LocalAurisDarkTheme.current

    val expansion = expansionFraction.coerceIn(0f, 1f)

    val miniAlpha by animateFloatAsState(
        targetValue = 1f - expansion,
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing
        ),
        label = "miniAlpha"
    )

    val fullAlpha by animateFloatAsState(
        targetValue = expansion,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "fullAlpha"
    )

    val fullScale by animateFloatAsState(
        targetValue = 0.94f + (0.06f * expansion),
        animationSpec = tween(
            durationMillis = 280,
            easing = FastOutSlowInEasing
        ),
        label = "fullScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(
                            alpha = if (isDark) 0.96f else 0.98f
                        ),
                        albumColor.copy(
                            alpha = if (isDark) 0.24f else 0.12f
                        )
                    )
                )
            )
    ) {

        /*
         * ======================================================
         * BACKGROUND GLOW
         * ======================================================
         */

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .graphicsLayer {
                    alpha = fullAlpha * 0.75f
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            albumColor.copy(alpha = 0.30f),
                            albumColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        /*
         * ======================================================
         * MINI PLAYER
         * ======================================================
         */

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = miniAlpha
                }
        ) {
            PremiumMiniPlayer(
                currentSong = currentSong,
                playerState = playerState,
                albumColor = albumColor,
                isPreparing = isPreparing,
                onPlayPause = onPlayPause,
                onQueue = onQueue,
                onMore = onMore
            )
        }

        /*
         * ======================================================
         * FULL PLAYER
         * ======================================================
         */

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = fullAlpha
                    scaleX = fullScale
                    scaleY = fullScale
                }
        ) {
            PremiumFullPlayer(
                currentSong = currentSong,
                playerState = playerState,
                albumColor = albumColor,
                isFavorite = isFavorite,
                isPreparing = isPreparing,
                currentPosition = currentPosition,
                duration = duration,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onFavorite = onFavorite,
                onQueue = onQueue,
                onMore = onMore
            )
        }
    }
}

/*
 * ============================================================
 * MINI PLAYER
 * ============================================================
 */

@Composable
private fun PremiumMiniPlayer(
    currentSong: Song?,
    playerState: StablePlayerState,
    albumColor: Color,
    isPreparing: Boolean,
    onPlayPause: () -> Unit,
    onQueue: () -> Unit,
    onMore: () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .shadow(
                elevation = 14.dp,
                shape = shape,
                clip = false
            ),
        shape = shape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            albumColor.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.98f
                            )
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = shape
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 8.dp,
                        end = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                PremiumAlbumArt(
                    song = currentSong,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(17.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentSong?.title ?: "Nenhuma música",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = currentSong?.artist ?: "Auris",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.62f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                MiniPlayerIconButton(
                    icon = Icons.Default.QueueMusic,
                    onClick = onQueue
                )

                MiniPlayerPlayButton(
                    isPlaying = playerState.isPlaying,
                    isPreparing = isPreparing,
                    onClick = onPlayPause
                )

                MiniPlayerIconButton(
                    icon = Icons.Default.MoreVert,
                    onClick = onMore
                )
            }
        }
    }
}

/*
 * ============================================================
 * FULL PLAYER
 * ============================================================
 */

@Composable
private fun PremiumFullPlayer(
    currentSong: Song?,
    playerState: StablePlayerState,
    albumColor: Color,
    isFavorite: Boolean,
    isPreparing: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onFavorite: () -> Unit,
    onQueue: () -> Unit,
    onMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 22.dp,
                vertical = 18.dp
            )
    ) {

        /*
         * HEADER
         */

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOCANDO AGORA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.6.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                            .copy(alpha = 0.55f)
                    )
                }
            }

            PremiumHeaderButton(
                icon = Icons.Default.MoreVert,
                onClick = onMore
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        /*
         * ALBUM
         */

        PremiumAlbumArt(
            song = currentSong,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(30.dp))
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(30.dp)
                )
        )

        Spacer(modifier = Modifier.height(28.dp))

        /*
         * SONG INFO
         */

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currentSong?.title ?: "Nenhuma música",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentSong?.artist ?: "Auris",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.60f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            PremiumFavoriteButton(
                selected = isFavorite,
                onClick = onFavorite
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        /*
         * PROGRESS
         */

        PremiumProgressBar(
            position = currentPosition,
            duration = duration,
            accent = albumColor
        )

        Spacer(modifier = Modifier.height(26.dp))

        /*
         * MAIN CONTROLS
         */

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            PremiumControlButton(
                icon = Icons.Default.Shuffle,
                onClick = {}
            )

            PremiumControlButton(
                icon = Icons.Default.SkipPrevious,
                size = 48.dp,
                onClick = onPrevious
            )

            PremiumMainPlayButton(
                isPlaying = playerState.isPlaying,
                isPreparing = isPreparing,
                accent = albumColor,
                onClick = onPlayPause
            )

            PremiumControlButton(
                icon = Icons.Default.SkipNext,
                size = 48.dp,
                onClick = onNext
            )

            PremiumControlButton(
                icon = Icons.Default.Repeat,
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        /*
         * BOTTOM ACTIONS
         */

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            PremiumSecondaryAction(
                icon = Icons.Default.QueueMusic,
                text = "Fila",
                onClick = onQueue
            )

            Spacer(modifier = Modifier.width(16.dp))

            PremiumSecondaryAction(
                icon = Icons.Default.MoreVert,
                text = "Mais",
                onClick = onMore
            )
        }
    }
}

/*
 * ============================================================
 * ALBUM ART
 * ============================================================
 */

@Composable
private fun PremiumAlbumArt(
    song: Song?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        /*
         * Ajuste esta propriedade caso seu Song use outro nome
         * para URI/capa.
         */
        val artwork = remember(song) {
            song?.albumArtUri
        }

        if (artwork != null) {
            AsyncImage(
                model = artwork,
                contentDescription = song?.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary
                                    .copy(alpha = 0.65f),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
            )
        }

        /*
         * Overlay premium
         */

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f)
                        )
                    )
                )
        )
    }
}

/*
 * ============================================================
 * PROGRESS
 * ============================================================
 */

@Composable
private fun PremiumProgressBar(
    position: Long,
    duration: Long,
    accent: Color
) {
    val progress = if (duration > 0L) {
        (position.toFloat() / duration.toFloat())
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.12f)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                accent,
                                accent.copy(alpha = 0.72f)
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatPlayerTime(position),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
                    .copy(alpha = 0.52f)
            )

            Text(
                text = formatPlayerTime(duration),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
                    .copy(alpha = 0.52f)
            )
        }
    }
}

/*
 * ============================================================
 * MINI PLAY
 * ============================================================
 */

@Composable
private fun MiniPlayerPlayButton(
    isPlaying: Boolean,
    isPreparing: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(42.dp)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onSurface
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = if (isPlaying) {
                    "Pausar"
                } else {
                    "Reproduzir"
                },
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/*
 * ============================================================
 * MAIN PLAY
 * ============================================================
 */

@Composable
private fun PremiumMainPlayButton(
    isPlaying: Boolean,
    isPreparing: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .shadow(
                elevation = 14.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accent,
                        accent.copy(alpha = 0.72f)
                    )
                )
            )
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) {
                Icons.Default.Pause
            } else {
                Icons.Default.PlayArrow
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(38.dp)
        )
    }
}

/*
 * ============================================================
 * HEADER BUTTON
 * ============================================================
 */

@Composable
private fun PremiumHeaderButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(42.dp)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onSurface
            .copy(alpha = 0.07f)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/*
 * ============================================================
 * MINI ICON
 * ============================================================
 */

@Composable
private fun MiniPlayerIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(21.dp),
            tint = MaterialTheme.colorScheme.onSurface
                .copy(alpha = 0.78f)
        )
    }
}

/*
 * ============================================================
 * CONTROL
 * ============================================================
 */

@Composable
private fun PremiumControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(
                if (size >= 48.dp) 29.dp else 21.dp
            ),
            tint = MaterialTheme.colorScheme.onSurface
                .copy(alpha = 0.82f)
        )
    }
}

/*
 * ============================================================
 * FAVORITE
 * ============================================================
 */

@Composable
private fun PremiumFavoriteButton(
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
    }

    Surface(
        modifier = Modifier
            .size(46.dp)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        color = background
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorito",
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.7f)
                },
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/*
 * ============================================================
 * SECONDARY ACTION
 * ============================================================
 */

@Composable
private fun PremiumSecondaryAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                MaterialTheme.colorScheme.onSurface
                    .copy(alpha = 0.06f)
            )
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = 18.dp,
                vertical = 11.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            tint = MaterialTheme.colorScheme.onSurface
                .copy(alpha = 0.78f)
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/*
 * ============================================================
 * TIME FORMAT
 * ============================================================
 */

private fun formatPlayerTime(
    milliseconds: Long
): String {
    if (milliseconds <= 0L) return "0:00"

    val totalSeconds = milliseconds / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L

    return "%d:%02d".format(
        minutes,
        seconds
    )
}