package com.goldensystem.auris.presentation.components.external

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

    // Movimento principal da energia ao redor da tela
    val flow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 7000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "EnergyFlow"
    )

    // Segunda onda, mais lenta
    val secondaryFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 11500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "SecondaryFlow"
    )

    // Respiração geral
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowBreathing"
    )

    // Pulsação dos hotspots
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HotspotPulse"
    )

    val blue = Color(0xFF3B82F6)
    val cyan = Color(0xFF22D3EE)
    val purple = Color(0xFF8B5CF6)

    Canvas(modifier = modifier) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas

        val w = size.width
        val h = size.height

        val density = this

        val edgeWidth = 3.5.dp.toPx()
        val glowWidth = 18.dp.toPx()
        val cornerRadius = 42.dp.toPx()

        /*
         * Caminho contínuo ao redor da tela.
         *
         * Isso é importante:
         * em vez de quatro linhas independentes,
         * temos um único fluxo de energia percorrendo
         * todo o perímetro.
         */
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
         * ------------------------------------------------
         * 1. ATMOSFERA EXTERNA
         * ------------------------------------------------
         */

        // Glow azul
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(
                    blue.copy(alpha = 0.10f * breathe),
                    cyan.copy(alpha = 0.04f * breathe),
                    purple.copy(alpha = 0.08f * breathe),
                    blue.copy(alpha = 0.05f * breathe)
                ),
                start = Offset(
                    x = w * flow,
                    y = 0f
                ),
                end = Offset(
                    x = w * (1f - flow),
                    y = h
                )
            ),
            style = Stroke(width = glowWidth)
        )

        // Segunda camada de glow
        drawPath(
            path = path,
            brush = Brush.sweepGradient(
                colors = listOf(
                    blue.copy(alpha = 0.10f * breathe),
                    cyan.copy(alpha = 0.18f * breathe),
                    Color.Transparent,
                    purple.copy(alpha = 0.16f * breathe),
                    blue.copy(alpha = 0.08f * breathe),
                    Color.Transparent
                ),
                center = Offset(w / 2f, h / 2f)
            ),
            style = Stroke(width = 10.dp.toPx())
        )

        /*
         * ------------------------------------------------
         * 2. LINHA BASE
         * ------------------------------------------------
         */

        drawPath(
            path = path,
            brush = Brush.sweepGradient(
                colors = listOf(
                    blue.copy(alpha = 0.30f * breathe),
                    cyan.copy(alpha = 0.18f * breathe),
                    purple.copy(alpha = 0.28f * breathe),
                    blue.copy(alpha = 0.14f * breathe),
                    cyan.copy(alpha = 0.25f * breathe),
                    blue.copy(alpha = 0.30f * breathe)
                ),
                center = Offset(w / 2f, h / 2f)
            ),
            style = Stroke(width = edgeWidth)
        )

        /*
         * ------------------------------------------------
         * 3. FEIXE PRINCIPAL DE ENERGIA
         * ------------------------------------------------
         *
         * O gradiente se move pelo perímetro.
         * A região clara funciona como um "feixe".
         */

        val flowPosition = flow

        drawPath(
            path = path,
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    blue.copy(alpha = 0.15f * breathe),
                    cyan.copy(alpha = 0.95f * breathe),
                    Color.White.copy(alpha = 0.85f * breathe),
                    cyan.copy(alpha = 0.60f * breathe),
                    purple.copy(alpha = 0.25f * breathe),
                    Color.Transparent,
                    Color.Transparent
                ),
                center = Offset(
                    x = w / 2f + (w * 0.25f * kotlin.math.cos(flowPosition * Math.PI * 2)).toFloat(),
                    y = h / 2f + (h * 0.25f * kotlin.math.sin(flowPosition * Math.PI * 2)).toFloat()
                )
            ),
            style = Stroke(width = edgeWidth * 1.8f)
        )

        /*
         * ------------------------------------------------
         * 4. SEGUNDO FEIXE
         * ------------------------------------------------
         */

        drawPath(
            path = path,
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    purple.copy(alpha = 0.15f),
                    Color.Transparent,
                    Color.Transparent,
                    blue.copy(alpha = 0.30f),
                    cyan.copy(alpha = 0.65f),
                    Color.Transparent,
                    Color.Transparent
                ),
                center = Offset(
                    x = w / 2f + (
                        w * 0.30f *
                            kotlin.math.cos(
                                secondaryFlow * Math.PI * 2
                            )
                    ).toFloat(),
                    y = h / 2f + (
                        h * 0.30f *
                            kotlin.math.sin(
                                secondaryFlow * Math.PI * 2
                            )
                    ).toFloat()
                )
            ),
            style = Stroke(width = 2.dp.toPx())
        )

        /*
         * ------------------------------------------------
         * 5. HOTSPOTS DOS CANTOS
         * ------------------------------------------------
         */

        val hotspotRadius = 105.dp.toPx()

        // Superior esquerdo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    cyan.copy(alpha = 0.45f * breathe * (0.7f + pulse * 0.3f)),
                    blue.copy(alpha = 0.20f * breathe),
                    Color.Transparent
                ),
                center = Offset.Zero,
                radius = hotspotRadius
            ),
            radius = hotspotRadius,
            center = Offset.Zero
        )

        // Superior direito
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    purple.copy(alpha = 0.42f * breathe),
                    blue.copy(alpha = 0.16f * breathe),
                    Color.Transparent
                ),
                radius = hotspotRadius
            ),
            radius = hotspotRadius,
            center = Offset(w, 0f)
        )

        // Inferior esquerdo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blue.copy(alpha = 0.32f * breathe),
                    cyan.copy(alpha = 0.12f * breathe),
                    Color.Transparent
                ),
                radius = hotspotRadius
            ),
            radius = hotspotRadius,
            center = Offset(0f, h)
        )

        // Inferior direito
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    purple.copy(alpha = 0.48f * breathe * (0.75f + pulse * 0.25f)),
                    blue.copy(alpha = 0.18f * breathe),
                    Color.Transparent
                ),
                radius = hotspotRadius
            ),
            radius = hotspotRadius,
            center = Offset(w, h)
        )

        /*
         * ------------------------------------------------
         * 6. PEQUENOS NÚCLEOS DE LUZ NOS CANTOS
         * ------------------------------------------------
         */

        val coreRadius = 5.dp.toPx()

        drawCircle(
            color = cyan.copy(alpha = 0.8f * breathe),
            radius = coreRadius,
            center = Offset(0f, 0f)
        )

        drawCircle(
            color = purple.copy(alpha = 0.8f * breathe),
            radius = coreRadius,
            center = Offset(w, 0f)
        )

        drawCircle(
            color = blue.copy(alpha = 0.7f * breathe),
            radius = coreRadius,
            center = Offset(0f, h)
        )

        drawCircle(
            color = purple.copy(alpha = 0.9f * breathe),
            radius = coreRadius,
            center = Offset(w, h)
        )

        /*
         * ------------------------------------------------
         * 7. PARTÍCULAS / PONTOS DE ENERGIA
         * ------------------------------------------------
         *
         * Movem-se independentemente pelo perímetro.
         */

        val particles = listOf(
            Triple(0.13f, 0.75f, cyan),
            Triple(0.31f, 0.42f, blue),
            Triple(0.57f, 0.88f, purple),
            Triple(0.76f, 0.30f, cyan),
            Triple(0.91f, 0.64f, blue),
            Triple(0.46f, 0.18f, purple)
        )

        particles.forEachIndexed { index, particle ->
            val base = particle.first
            val speed = particle.second
            val color = particle.third

            val position = (
                base +
                    flow * (0.25f + speed * 0.4f) +
                    index * 0.07f
                ) % 1f

            /*
             * Converte posição 0..1 em posição no perímetro.
             */
            val perimeter = 2f * (w + h)

            val distance = position * perimeter

            val point = when {
                distance < w -> {
                    Offset(distance, 0f)
                }

                distance < w + h -> {
                    Offset(w, distance - w)
                }

                distance < (2f * w) + h -> {
                    Offset(
                        w - (distance - (w + h)),
                        h
                    )
                }

                else -> {
                    Offset(
                        0f,
                        h - (distance - (2f * w + h))
                    )
                }
            }

            val particlePulse =
                0.35f +
                    0.65f *
                    kotlin.math.sin(
                        (
                            flow * Math.PI * 4 +
                                index
                        )
                    ).toFloat().let { (it + 1f) / 2f }

            drawCircle(
                color = color.copy(
                    alpha = particlePulse * 0.9f
                ),
                radius = 2.5.dp.toPx(),
                center = point
            )

            // Pequeno halo da partícula
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = particlePulse * 0.25f),
                        Color.Transparent
                    ),
                    radius = 16.dp.toPx()
                ),
                radius = 16.dp.toPx(),
                center = point
            )
        }

        /*
         * ------------------------------------------------
         * 8. PEQUENOS "TRAILS" NOS FEIXES
         * ------------------------------------------------
         */

        val trailAlpha = 0.20f + breathe * 0.12f

        drawPath(
            path = path,
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    cyan.copy(alpha = trailAlpha),
                    Color.Transparent,
                    Color.Transparent
                ),
                center = Offset(w / 2f, h / 2f)
            ),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}