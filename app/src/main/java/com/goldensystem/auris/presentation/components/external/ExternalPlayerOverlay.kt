package com.goldensystem.auris.presentation.components.external

import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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

    val flow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 6500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "EdgeFlow"
    )

    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "EdgeBreathing"
    )

    val blue = Color(0xFF3B82F6)
    val cyan = Color(0xFF22D3EE)
    val purple = Color(0xFF8B5CF6)

    Canvas(modifier = modifier) {
        if (size.width <= 0f || size.height <= 0f) {
            return@Canvas
        }

        val w = size.width
        val h = size.height

        /*
         * A borda continua exatamente no mesmo espaço.
         *
         * Não existe uma camada gigante de glow invadindo
         * o centro da tela.
         */
        val edgeWidth = 3.5.dp.toPx()
        val glowWidth = 18.dp.toPx()
        val cornerRadius = 42.dp.toPx()

        val path = Path().apply {
            moveTo(cornerRadius, 0f)

            lineTo(w - cornerRadius, 0f)

            quadraticTo(
                w,
                0f,
                w,
                cornerRadius
            )

            lineTo(w, h - cornerRadius)

            quadraticTo(
                w,
                h,
                w - cornerRadius,
                h
            )

            lineTo(cornerRadius, h)

            quadraticTo(
                0f,
                h,
                0f,
                h - cornerRadius
            )

            lineTo(0f, cornerRadius)

            quadraticTo(
                0f,
                0f,
                cornerRadius,
                0f
            )

            close()
        }

        /*
         * =========================================================
         * MOVIMENTO DA BORDA
         * =========================================================
         *
         * Em vez de partículas, criamos regiões de energia.
         *
         * O gradiente percorre a borda lentamente:
         *
         * apagado -> suave -> forte -> suave -> apagado
         *
         * Isso faz parecer que a própria borda está "respirando"
         * e se deslocando pelo perímetro.
         */

        val movement = flow * Math.PI.toFloat() * 2f

        val offsetX =
            kotlin.math.cos(movement) * w * 0.45f

        val offsetY =
            kotlin.math.sin(movement * 0.73f) * h * 0.45f

        /*
         * =========================================================
         * 1. GLOW EXTERNO SUAVE
         * =========================================================
         *
         * Não é uma faixa colorida.
         *
         * A intensidade nasce da borda e desaparece suavemente.
         */

        val outerGradient = Brush.radialGradient(
            colorStops = arrayOf(
                0.00f to Color.White.copy(
                    alpha = 0.04f * breathe
                ),
                0.20f to cyan.copy(
                    alpha = 0.10f * breathe
                ),
                0.42f to blue.copy(
                    alpha = 0.07f * breathe
                ),
                0.68f to purple.copy(
                    alpha = 0.035f * breathe
                ),
                1.00f to Color.Transparent
            ),
            center = Offset(
                w / 2f + offsetX,
                h / 2f + offsetY
            ),
            radius = maxOf(w, h) * 0.72f
        )

        /*
         * Esse glow é bem fraco.
         * A borda continua sendo o elemento principal.
         */
        drawPath(
            path = path,
            brush = outerGradient,
            style = Stroke(width = glowWidth)
        )

        /*
         * =========================================================
         * 2. BORDA PRINCIPAL
         * =========================================================
         *
         * Muitos pontos próximos entre si evitam aquelas
         * transições duras de "faixa azul -> faixa roxa".
         *
         * A mudança de cor acontece de maneira contínua.
         */

        val mainGradient = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to cyan.copy(alpha = 0.22f * breathe),
                0.08f to cyan.copy(alpha = 0.28f * breathe),
                0.18f to blue.copy(alpha = 0.34f * breathe),
                0.30f to blue.copy(alpha = 0.20f * breathe),
                0.42f to purple.copy(alpha = 0.27f * breathe),
                0.54f to purple.copy(alpha = 0.17f * breathe),
                0.66f to blue.copy(alpha = 0.30f * breathe),
                0.78f to cyan.copy(alpha = 0.25f * breathe),
                0.90f to cyan.copy(alpha = 0.34f * breathe),
                1.00f to blue.copy(alpha = 0.18f * breathe)
            ),
            start = Offset(
                x = -w * 0.35f + offsetX,
                y = -h * 0.15f + offsetY
            ),
            end = Offset(
                x = w * 1.35f + offsetX,
                y = h * 1.15f + offsetY
            )
        )

        /*
         * Glow intermediário.
         *
         * Mais largo, porém extremamente transparente.
         */
        drawPath(
            path = path,
            brush = mainGradient,
            style = Stroke(
                width = glowWidth
            )
        )

        /*
         * =========================================================
         * 3. NÚCLEO DA BORDA
         * =========================================================
         *
         * É aqui que fica a linha realmente visível.
         * Bem fina para não parecer uma faixa.
         */

        drawPath(
            path = path,
            brush = mainGradient,
            style = Stroke(
                width = edgeWidth
            )
        )

        /*
         * =========================================================
         * 4. "ONDA" DE INTENSIDADE
         * =========================================================
         *
         * Não é uma bolinha nem uma partícula.
         *
         * É uma região grande da própria borda que fica
         * gradualmente mais forte e depois desaparece.
         */

        val wavePosition = flow

        val waveGradient = Brush.sweepGradient(
            colorStops = arrayOf(
                0.00f to Color.Transparent,
                0.16f to Color.Transparent,
                0.28f to cyan.copy(
                    alpha = 0.04f * breathe
                ),
                0.38f to blue.copy(
                    alpha = 0.10f * breathe
                ),
                0.46f to cyan.copy(
                    alpha = 0.24f * breathe
                ),
                0.50f to Color.White.copy(
                    alpha = 0.34f * breathe
                ),
                0.54f to cyan.copy(
                    alpha = 0.20f * breathe
                ),
                0.63f to blue.copy(
                    alpha = 0.08f * breathe
                ),
                0.74f to Color.Transparent,
                1.00f to Color.Transparent
            ),
            center = Offset(
                x = w / 2f +
                    kotlin.math.cos(
                        wavePosition * Math.PI.toFloat() * 2f
                    ) * w * 0.28f,

                y = h / 2f +
                    kotlin.math.sin(
                        wavePosition * Math.PI.toFloat() * 2f
                    ) * h * 0.28f
            )
        )

        drawPath(
            path = path,
            brush = waveGradient,
            style = Stroke(
                width = 4.5.dp.toPx()
            )
        )

        /*
         * =========================================================
         * 5. MICRO-BRILHO
         * =========================================================
         *
         * Uma linha quase branca muito transparente.
         * Dá aquele aspecto de material luminoso em vez
         * de uma simples linha neon.
         */

        val highlightGradient = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color.Transparent,
                0.20f to cyan.copy(alpha = 0.10f * breathe),
                0.40f to Color.White.copy(alpha = 0.16f * breathe),
                0.55f to cyan.copy(alpha = 0.08f * breathe),
                0.72f to purple.copy(alpha = 0.12f * breathe),
                1.00f to Color.Transparent
            ),
            start = Offset(
                x = -w * 0.20f + offsetX,
                y = 0f
            ),
            end = Offset(
                x = w * 1.20f + offsetX,
                y = h
            )
        )

        drawPath(
            path = path,
            brush = highlightGradient,
            style = Stroke(
                width = 1.dp.toPx()
            )
        )
    }
}