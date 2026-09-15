package com.goldensystem.auris.presentation.components.external

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    // Sem card visível: só fecha quando não há mais música
    LaunchedEffect(currentSong) {
        if (currentSong == null) {
            onDismiss()
        }
    }

    BackHandler {
        onDismiss()
    }

    val controlAnimationSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    }

    val safePadding = WindowInsets.safeDrawing.asPaddingValues()
    val bottomPadding = safePadding.calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. Brilho gradiente nas bordas
        EdgeGlowBorder(modifier = Modifier.fillMaxSize())

        // 2. Área clicável no centro vazio → fecha o player
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        )

        // 3. Conteúdo do player, tudo embaixo
        if (currentSong != null) {
            val totalDuration = stablePlayerState.totalDuration.coerceAtLeast(0L)
            val rawPosition = if (isRemotePlaybackActive) remotePosition else playbackPosition
            val position = rawPosition.coerceIn(0L, totalDuration)
            val progressFraction = if (totalDuration > 0) position.toFloat() / totalDuration else 0f

            var sliderPosition by remember(currentSong.id) { mutableStateOf(progressFraction) }
            var isUserScrubbing by remember { mutableStateOf(false) }

            LaunchedEffect(progressFraction) {
                if (!isUserScrubbing) {
                    sliderPosition = progressFraction
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = bottomPadding + 16.dp)
                    .clickable(
                        // evita que o clique no card feche o player
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* consume */ },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Capa + texto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OptimizedAlbumArt(
                        uri = currentSong.albumArtUriString,
                        title = currentSong.title,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentSong.displayArtist,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Barra de progresso
                WavyMusicSlider(
                    value = sliderPosition,
                    onValueChange = { newValue ->
                        isUserScrubbing = true
                        sliderPosition = newValue.coerceIn(0f, 1f)
                    },
                    onValueChangeFinished = {
                        val targetPosition = (sliderPosition * totalDuration).roundToLong()
                        playerViewModel.seekTo(targetPosition)
                        isUserScrubbing = false
                    },
                    waveLength = 30.dp,
                    isPlaying = stablePlayerState.isPlaying,
                    isWaveEligible = true,
                    semanticsLabel = "Playback position"
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Tempos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(position),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatDuration(totalDuration),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Controles
                AnimatedPlaybackControls(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    isPlayingProvider = { stablePlayerState.isPlaying },
                    onPrevious = playerViewModel::previousSong,
                    onPlayPause = playerViewModel::playPause,
                    onNext = playerViewModel::nextSong,
                    height = 68.dp,
                    pressAnimationSpec = controlAnimationSpec,
                    colorOtherButtons = Color.White.copy(alpha = 0.15f),
                    colorPlayPause = Color.White.copy(alpha = 0.25f),
                    tintPlayPauseIcon = Color.White,
                    tintOtherIcons = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            // Aguardando carregar a música
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomPadding + 40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

/**
 * Brilho gradiente nas bordas: azul → roxo, mais forte na borda,
 * desvanecendo em direção ao centro. Animação lenta de "respiração".
 */
@Composable
private fun EdgeGlowBorder(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "EdgeGlow")

    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreatheAlpha"
    )

    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GradientShift"
    )

    val blue = Color(0xFF3B82F6)
    val purple = Color(0xFF8B5CF6)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Deslocamento interno pra não cortar o stroke
        val inset = 2.dp.toPx()
        val corner = 48.dp.toPx()   // quina do glow (bem arredondada)

        // Gradiente diagonal azul→roxo, mudando suavemente com o tempo
        val brush = Brush.linearGradient(
            colors = listOf(
                blue.copy(alpha = breathe * (1f - gradientShift * 0.25f)),
                purple.copy(alpha = breathe * (0.75f + gradientShift * 0.25f))
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )

        // 3 strokes concêntricos: de dentro pra fora, alpha decrescente.
        // Isso cria o efeito "vai desvanecendo em direção ao centro".
        val strokes = listOf(
            6.dp to 1.00f,   // linha mais grossa, mais opaca
            14.dp to 0.55f,  // média
            24.dp to 0.25f   // fina, quase transparente
        )

        strokes.forEach { (thickness, alpha) ->
            val strokePx = thickness.toPx() / 2f   // metade pra dentro, metade pra fora
            drawRoundRect(
                brush = brush,
                topLeft = Offset(
                    inset + strokePx,
                    inset + strokePx
                ),
                size = Size(
                    w - (inset + strokePx) * 2,
                    h - (inset + strokePx) * 2
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner),
                style = Stroke(
                    width = thickness.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                ),
                alpha = alpha
            )
        }
    }
}