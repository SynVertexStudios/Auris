package com.goldensystem.auris.presentation.components.external

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.goldensystem.auris.presentation.components.OptimizedAlbumArt
import com.goldensystem.auris.presentation.components.WavyMusicSlider
import com.goldensystem.auris.presentation.components.player.AnimatedPlaybackControls
import com.goldensystem.auris.presentation.viewmodel.PlayerViewModel
import com.goldensystem.auris.utils.formatDuration
import kotlin.math.roundToLong

@OptIn(UnstableApi::class)
@Composable
fun ExternalPlayerOverlay(
    playerViewModel: PlayerViewModel,
    onDismiss: () -> Unit,
    onOpenFullPlayer: () -> Unit
) {
    val stablePlayerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()
    val playbackPosition by playerViewModel.currentPlaybackPosition.collectAsStateWithLifecycle()
    val remotePosition by playerViewModel.remotePosition.collectAsStateWithLifecycle()
    val isRemotePlaybackActive by playerViewModel.isRemotePlaybackActive.collectAsStateWithLifecycle()

    val currentSong = stablePlayerState.currentSong

    val safePadding = WindowInsets.safeDrawing.asPaddingValues()
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()

    val bottomPadding = maxOf(
        safePadding.calculateBottomPadding(),
        navigationBottom
    )

    LaunchedEffect(currentSong) {
        if (currentSong == null) {
            onDismiss()
        }
    }

    BackHandler {
        onDismiss()
    }

    val infiniteTransition = rememberInfiniteTransition(
        label = "ExternalPlayerAnimation"
    )

    val ambientProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 7000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AmbientProgress"
    )

    val cardScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (currentSong != null) 1f else 0.96f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 380f
        ),
        label = "CardScale"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        /*
         * BACKGROUND
         */

        ExternalPlayerBackdrop(
            modifier = Modifier.fillMaxSize(),
            animationProgress = ambientProgress
        )

        /*
         * FECHA AO TOCAR FORA DO PLAYER
         */

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    },
                    indication = null
                ) {
                    onDismiss()
                }
        )

        /*
         * BORDA LUMINOSA
         */

        ExternalPlayerEdgeGlow(
            modifier = Modifier.fillMaxSize(),
            animationProgress = ambientProgress
        )

        /*
         * PLAYER
         */

        if (currentSong != null) {

            val totalDuration = stablePlayerState.totalDuration
                .coerceAtLeast(0L)

            val rawPosition = if (isRemotePlaybackActive) {
                remotePosition
            } else {
                playbackPosition
            }

            val position = rawPosition.coerceIn(
                0L,
                totalDuration
            )

            val progressFraction =
                if (totalDuration > 0L) {
                    position.toFloat() / totalDuration.toFloat()
                } else {
                    0f
                }

            var sliderPosition by remember(currentSong.id) {
                mutableStateOf(progressFraction)
            }

            var isUserScrubbing by remember {
                mutableStateOf(false)
            }

            LaunchedEffect(progressFraction) {
                if (!isUserScrubbing) {
                    sliderPosition = progressFraction
                }
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(280)
                ) + slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = 350f
                    )
                ) + scaleIn(
                    initialScale = 0.96f,
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = 350f
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(180)
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = bottomPadding + 12.dp
                    )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(cardScale)
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null
                        ) {
                            // Consome o toque.
                        }
                ) {

                    /*
                     * GLOW ATRÁS DO CARD
                     */

                    PlayerCardAmbientGlow(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(
                                horizontal = 18.dp,
                                vertical = 10.dp
                            ),
                        progress = ambientProgress
                    )

                    /*
                     * CARD PRINCIPAL
                     */

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(30.dp),
                        color = Color(0xFF080A12).copy(alpha = 0.88f),
                        tonalElevation = 0.dp,
                        shadowElevation = 20.dp
                    ) {

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            /*
                             * TEXTURA INTERNA
                             */

                            Canvas(
                                modifier = Modifier.matchParentSize()
                            ) {
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.055f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.08f)
                                        )
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                        30.dp.toPx()
                                    )
                                )
                            }

                            /*
                             * CONTEÚDO
                             */

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 18.dp,
                                        end = 18.dp,
                                        top = 14.dp,
                                        bottom = 15.dp
                                    )
                            ) {

                                /*
                                 * TOPO
                                 */

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 2.dp)
                                    ) {

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (stablePlayerState.isPlaying) {
                                                            Color(0xFF60A5FA)
                                                        } else {
                                                            Color.White.copy(alpha = 0.35f)
                                                        }
                                                    )
                                            )

                                            Spacer(
                                                modifier = Modifier.width(8.dp)
                                            )

                                            Text(
                                                text = if (
                                                    stablePlayerState.isPlaying
                                                ) {
                                                    "TOCANDO AGORA"
                                                } else {
                                                    "PAUSADO"
                                                },
                                                color = Color.White.copy(
                                                    alpha = 0.52f
                                                ),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.4.sp
                                            )
                                        }
                                    }

                                    ExternalPlayerTopButton(
                                        icon = Icons.Default.ExpandLess,
                                        contentDescription = "Abrir player completo",
                                        onClick = onOpenFullPlayer
                                    )

                                    Spacer(
                                        modifier = Modifier.width(7.dp)
                                    )

                                    ExternalPlayerTopButton(
                                        icon = Icons.Default.Close,
                                        contentDescription = "Fechar",
                                        onClick = onDismiss
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(14.dp)
                                )

                                /*
                                 * CAPA + INFORMAÇÕES
                                 */

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Box(
                                        modifier = Modifier.size(82.dp),
                                        contentAlignment = Alignment.Center
                                    ) {

                                        AlbumArtGlow(
                                            modifier = Modifier.fillMaxSize(),
                                            isPlaying = stablePlayerState.isPlaying
                                        )

                                        OptimizedAlbumArt(
                                            uri = currentSong.albumArtUriString,
                                            title = currentSong.title,
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(
                                                    RoundedCornerShape(19.dp)
                                                )
                                        )
                                    }

                                    Spacer(
                                        modifier = Modifier.width(15.dp)
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text = currentSong.title,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(
                                            modifier = Modifier.height(4.dp)
                                        )

                                        Text(
                                            text = currentSong.displayArtist,
                                            color = Color.White.copy(
                                                alpha = 0.58f
                                            ),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(
                                            modifier = Modifier.height(9.dp)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Icon(
                                                imageVector =
                                                    if (isRemotePlaybackActive) {
                                                        Icons.Default.GraphicEq
                                                    } else {
                                                        Icons.Default.MusicNote
                                                    },
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = Color.White.copy(
                                                    alpha = 0.4f
                                                )
                                            )

                                            Spacer(
                                                modifier = Modifier.width(5.dp)
                                            )

                                            Text(
                                                text = if (
                                                    isRemotePlaybackActive
                                                ) {
                                                    "REPRODUÇÃO REMOTA"
                                                } else {
                                                    "AURIS PLAYER"
                                                },
                                                color = Color.White.copy(
                                                    alpha = 0.4f
                                                ),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(
                                    modifier = Modifier.height(19.dp)
                                )

                                /*
                                 * SLIDER
                                 */

                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    WavyMusicSlider(
                                        value = sliderPosition,
                                        onValueChange = { newValue ->
                                            isUserScrubbing = true

                                            sliderPosition =
                                                newValue.coerceIn(
                                                    0f,
                                                    1f
                                                )
                                        },
                                        onValueChangeFinished = {
                                            val targetPosition =
                                                (
                                                    sliderPosition *
                                                        totalDuration
                                                ).roundToLong()

                                            playerViewModel.seekTo(
                                                targetPosition
                                            )

                                            isUserScrubbing = false
                                        },
                                        waveLength = 26.dp,
                                        isPlaying =
                                            stablePlayerState.isPlaying,
                                        isWaveEligible = true,
                                        semanticsLabel =
                                            "Posição da reprodução"
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )

                                /*
                                 * TEMPOS
                                 */

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {

                                    Text(
                                        text = formatDuration(
                                            if (isUserScrubbing) {
                                                (
                                                    sliderPosition *
                                                        totalDuration
                                                ).roundToLong()
                                            } else {
                                                position
                                            }
                                        ),
                                        color = Color.White.copy(
                                            alpha = 0.48f
                                        ),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = formatDuration(
                                            totalDuration
                                        ),
                                        color = Color.White.copy(
                                            alpha = 0.48f
                                        ),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(11.dp)
                                )

                                /*
                                 * CONTROLES
                                 */

                                AnimatedPlaybackControls(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 8.dp
                                        ),
                                    isPlayingProvider = {
                                        stablePlayerState.isPlaying
                                    },
                                    onPrevious =
                                        playerViewModel::previousSong,
                                    onPlayPause =
                                        playerViewModel::playPause,
                                    onNext =
                                        playerViewModel::nextSong,
                                    height = 66.dp,
                                    pressAnimationSpec = remember {
                                        spring<Float>(
                                            dampingRatio =
                                                Spring.DampingRatioNoBouncy,
                                            stiffness =
                                                Spring.StiffnessMedium
                                        )
                                    },
                                    colorOtherButtons =
                                        Color.White.copy(
                                            alpha = 0.11f
                                        ),
                                    colorPlayPause =
                                        Color.White.copy(
                                            alpha = 0.18f
                                        ),
                                    tintPlayPauseIcon =
                                        Color.White,
                                    tintOtherIcons =
                                        Color.White
                                )
                            }

                            /*
                             * LINHA DE BRILHO NO TOPO
                             */

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .align(Alignment.TopCenter)
                            ) {

                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            lerp(
                                                Color(0xFF60A5FA),
                                                Color(0xFFA78BFA),
                                                ambientProgress
                                            ).copy(
                                                alpha = 0.85f
                                            ),
                                            Color.Transparent
                                        )
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }

        } else {

            /*
             * LOADING
             */

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = bottomPadding + 40.dp
                    )
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFF0A0C14).copy(alpha = 0.85f)
                    ),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator(
                    modifier = Modifier.size(25.dp),
                    strokeWidth = 2.dp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}


/*
 * ============================================================
 * BACKGROUND
 * ============================================================
 */

@Composable
private fun ExternalPlayerBackdrop(
    modifier: Modifier = Modifier,
    animationProgress: Float
) {
    Canvas(
        modifier = modifier
    ) {

        val w = size.width
        val h = size.height

        val shift = animationProgress * w * 0.22f

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF263B70).copy(alpha = 0.20f),
                    Color(0xFF11162A).copy(alpha = 0.13f),
                    Color.Transparent
                ),
                center = Offset(
                    w * 0.12f + shift,
                    h * 0.82f
                ),
                radius = w * 0.85f
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF693A9E).copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(
                    w * 0.88f - shift,
                    h * 0.18f
                ),
                radius = w * 0.7f
            )
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.14f),
                    Color.Black.copy(alpha = 0.42f),
                    Color.Black.copy(alpha = 0.58f)
                )
            )
        )
    }
}


/*
 * ============================================================
 * EDGE GLOW
 * ============================================================
 */

@Composable
private fun ExternalPlayerEdgeGlow(
    modifier: Modifier = Modifier,
    animationProgress: Float
) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "EdgeGlow"
    )

    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "EdgeBreathe"
    )

    Canvas(
        modifier = modifier
    ) {

        val w = size.width
        val h = size.height

        val edgeWidth = 42.dp.toPx()
        val corner = 150.dp.toPx()

        val blue = Color(0xFF3B82F6)
        val purple = Color(0xFF8B5CF6)

        /*
         * Esquerda
         */

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    blue.copy(alpha = breathe * 0.65f),
                    blue.copy(alpha = breathe * 0.25f),
                    Color.Transparent
                ),
                startX = 0f,
                endX = edgeWidth
            ),
            size = Size(edgeWidth, h)
        )

        /*
         * Direita
         */

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    purple.copy(alpha = breathe * 0.25f),
                    purple.copy(alpha = breathe * 0.68f)
                ),
                startX = w - edgeWidth,
                endX = w
            ),
            topLeft = Offset(w - edgeWidth, 0f),
            size = Size(edgeWidth, h)
        )

        /*
         * Topo
         */

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    purple.copy(alpha = breathe * 0.65f),
                    purple.copy(alpha = breathe * 0.18f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = edgeWidth
            ),
            size = Size(w, edgeWidth)
        )

        /*
         * Base
         */

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    blue.copy(alpha = breathe * 0.18f),
                    blue.copy(alpha = breathe * 0.68f)
                ),
                startY = h - edgeWidth,
                endY = h
            ),
            topLeft = Offset(0f, h - edgeWidth),
            size = Size(w, edgeWidth)
        )

        /*
         * Cantos
         */

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blue.copy(
                        alpha = breathe *
                            (0.32f + animationProgress * 0.12f)
                    ),
                    Color.Transparent
                )
            ),
            radius = corner,
            center = Offset(0f, 0f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    purple.copy(
                        alpha = breathe *
                            (0.34f + animationProgress * 0.12f)
                    ),
                    Color.Transparent
                )
            ),
            radius = corner,
            center = Offset(w, h)
        )
    }
}


/*
 * ============================================================
 * CARD GLOW
 * ============================================================
 */

@Composable
private fun PlayerCardAmbientGlow(
    modifier: Modifier = Modifier,
    progress: Float
) {
    Canvas(
        modifier = modifier
    ) {

        val w = size.width
        val h = size.height

        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF4F8CFF).copy(
                        alpha = 0.13f + progress * 0.07f
                    ),
                    Color(0xFF8B5CF6).copy(
                        alpha = 0.08f
                    ),
                    Color.Transparent
                ),
                center = Offset(
                    w * (0.25f + progress * 0.5f),
                    h * 0.5f
                ),
                radius = w * 0.9f
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                35.dp.toPx()
            )
        )
    }
}


/*
 * ============================================================
 * ALBUM ART GLOW
 * ============================================================
 */

@Composable
private fun AlbumArtGlow(
    modifier: Modifier = Modifier,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "AlbumGlow"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (isPlaying) 0.72f else 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2100,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlbumGlowPulse"
    )

    Canvas(
        modifier = modifier
    ) {

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF60A5FA).copy(
                        alpha = pulse * 0.38f
                    ),
                    Color(0xFF8B5CF6).copy(
                        alpha = pulse * 0.18f
                    ),
                    Color.Transparent
                )
            ),
            radius = size.minDimension * 0.68f,
            center = Offset(
                size.width / 2f,
                size.height / 2f
            )
        )
    }
}


/*
 * ============================================================
 * TOP BUTTON
 * ============================================================
 */

@Composable
private fun ExternalPlayerTopButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(
                Color.White.copy(alpha = 0.075f)
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
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = Color.White.copy(alpha = 0.72f)
        )
    }
}