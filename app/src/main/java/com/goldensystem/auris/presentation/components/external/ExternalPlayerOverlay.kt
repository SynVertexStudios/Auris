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
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
    onDismiss: () -> Unit
) {
    val stablePlayerState by
        playerViewModel.stablePlayerState.collectAsStateWithLifecycle()

    val playbackPosition by
        playerViewModel.currentPlaybackPosition.collectAsStateWithLifecycle()

    val remotePosition by
        playerViewModel.remotePosition.collectAsStateWithLifecycle()

    val isRemotePlaybackActive by
        playerViewModel.isRemotePlaybackActive.collectAsStateWithLifecycle()

    val currentSong = stablePlayerState.currentSong

    /*
     * Se a música desaparecer do estado do player,
     * o overlay desaparece automaticamente.
     */
    LaunchedEffect(currentSong) {
        if (currentSong == null) {
            onDismiss()
        }
    }

    /*
     * Back continua funcionando normalmente.
     * Não existe botão visual de fechar.
     */
    BackHandler {
        onDismiss()
    }

    val safePadding = WindowInsets.safeDrawing.asPaddingValues()
    val bottomPadding = safePadding.calculateBottomPadding()

    val controlAnimationSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        /*
         * Glow de tela inteira.
         *
         * Não possui fundo.
         * O que existe são apenas linhas/glows desenhados
         * sobre a interface que já estiver atrás.
         */
        EdgeGlowBorder(
            modifier = Modifier.fillMaxSize(),
            isPlaying = stablePlayerState.isPlaying
        )

        /*
         * Área transparente responsável por fechar.
         *
         * Fica atrás do conteúdo, então clicar no player
         * não dispara o dismiss.
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

        if (currentSong != null) {

            val totalDuration =
                stablePlayerState.totalDuration.coerceAtLeast(0L)

            val rawPosition =
                if (isRemotePlaybackActive) {
                    remotePosition
                } else {
                    playbackPosition
                }

            val position =
                rawPosition.coerceIn(0L, totalDuration)

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

            /*
             * Animação de entrada.
             */
            AnimatedVisibility(
                visible = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = FastOutSlowInEasing
                    )
                ) + scaleIn(
                    initialScale = 0.97f,
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(
                            bottom = bottomPadding + 18.dp
                        )
                        /*
                         * Esse drawBehind adiciona uma pequena
                         * sombra/glow somente atrás do conteúdo.
                         *
                         * Não cria card.
                         * Não cria fundo.
                         */
                        .drawBehind {

                            val centerY = size.height * 0.55f

                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF2563EB)
                                            .copy(alpha = 0.10f),
                                        Color.Transparent
                                    ),
                                    center = Offset(
                                        size.width * 0.5f,
                                        centerY
                                    ),
                                    radius = size.width * 0.72f
                                ),
                                blendMode = BlendMode.Screen
                            )
                        }
                        /*
                         * Impede o clique do conteúdo de chegar
                         * no detector transparente de fora.
                         */
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null
                        ) {},
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    /*
                     * ─────────────────────────────
                     * CAPA + INFORMAÇÕES
                     * ─────────────────────────────
                     */

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        /*
                         * Pequeno glow atrás da capa.
                         */
                        Box(
                            modifier = Modifier.size(76.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            AlbumArtGlow()

                            OptimizedAlbumArt(
                                uri = currentSong.albumArtUriString,
                                title = currentSong.title,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(
                                        RoundedCornerShape(16.dp)
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
                                style = MaterialTheme
                                    .typography
                                    .titleMedium
                                    .copy(
                                        fontSize = 18.sp,
                                        fontWeight =
                                            FontWeight.SemiBold
                                    ),
                                color = Color.White,
                                maxLines = 1,
                                overflow =
                                    TextOverflow.Ellipsis
                            )

                            Spacer(
                                modifier = Modifier.height(3.dp)
                            )

                            Text(
                                text = currentSong.displayArtist,
                                style = MaterialTheme
                                    .typography
                                    .bodySmall
                                    .copy(
                                        fontSize = 13.sp
                                    ),
                                color = Color.White.copy(
                                    alpha = 0.62f
                                ),
                                maxLines = 1,
                                overflow =
                                    TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(19.dp)
                    )

                    /*
                     * ─────────────────────────────
                     * SLIDER
                     * ─────────────────────────────
                     */

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

                        waveLength = 30.dp,
                        isPlaying =
                            stablePlayerState.isPlaying,
                        isWaveEligible = true,
                        semanticsLabel =
                            "Playback position"
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    /*
                     * ─────────────────────────────
                     * TEMPOS
                     * ─────────────────────────────
                     */

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = formatDuration(position),
                            style = MaterialTheme
                                .typography
                                .labelSmall
                                .copy(
                                    fontSize = 11.sp,
                                    fontWeight =
                                        FontWeight.Medium
                                ),
                            color = Color.White.copy(
                                alpha = 0.58f
                            )
                        )

                        Text(
                            text =
                                formatDuration(totalDuration),
                            style = MaterialTheme
                                .typography
                                .labelSmall
                                .copy(
                                    fontSize = 11.sp,
                                    fontWeight =
                                        FontWeight.Medium
                                ),
                            color = Color.White.copy(
                                alpha = 0.58f
                            )
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    /*
                     * ─────────────────────────────
                     * CONTROLES
                     *
                     * A ideia aqui é deixar os botões
                     * visíveis sem aquela aparência de
                     * três manchas brancas.
                     * ─────────────────────────────
                     */

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {

                        ControlGlow(
                            isPlaying =
                                stablePlayerState.isPlaying
                        )

                        AnimatedPlaybackControls(
                            modifier = Modifier.fillMaxWidth(),

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

                            pressAnimationSpec =
                                controlAnimationSpec,

                            /*
                             * Antes os valores eram muito
                             * claros e os controles acabavam
                             * parecendo uma interface branca.
                             *
                             * Agora os fundos são quase
                             * transparentes e os ícones
                             * continuam claramente visíveis.
                             */
                            colorOtherButtons =
                                Color.White.copy(
                                    alpha = 0.09f
                                ),

                            colorPlayPause =
                                Color.White.copy(
                                    alpha = 0.16f
                                ),

                            tintPlayPauseIcon =
                                Color.White.copy(
                                    alpha = 0.96f
                                ),

                            tintOtherIcons =
                                Color.White.copy(
                                    alpha = 0.82f
                                )
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )
                }
            }
        } else {

            /*
             * Loading totalmente transparente.
             */
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = bottomPadding + 42.dp
                    ),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    color = Color.White.copy(
                        alpha = 0.78f
                    ),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}


/* ═══════════════════════════════════════════════
 *
 * GLOW DA CAPA
 *
 * ═══════════════════════════════════════════════ */

@Composable
private fun AlbumArtGlow() {

    val transition =
        rememberInfiniteTransition(
            label = "AlbumArtGlow"
        )

    val alpha by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.34f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlbumGlowAlpha"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6366F1)
                        .copy(alpha = alpha),
                    Color(0xFF2563EB)
                        .copy(alpha = alpha * 0.35f),
                    Color.Transparent
                )
            ),
            radius = size.minDimension * 0.62f
        )
    }
}


/* ═══════════════════════════════════════════════
 *
 * GLOW ATRÁS DOS CONTROLES
 *
 * ═══════════════════════════════════════════════ */

@Composable
private fun ControlGlow(
    isPlaying: Boolean
) {

    val transition =
        rememberInfiniteTransition(
            label = "ControlGlow"
        )

    val alpha by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = if (isPlaying) 0.20f else 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ControlGlowAlpha"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6366F1)
                        .copy(alpha = alpha),
                    Color(0xFF3B82F6)
                        .copy(alpha = alpha * 0.35f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width / 2f,
                    size.height / 2f
                ),
                radius = size.width * 0.55f
            ),
            size = Size(
                size.width * 0.9f,
                size.height * 0.9f
            ),
            topLeft = Offset(
                size.width * 0.05f,
                size.height * 0.05f
            )
        )
    }
}


/* ═══════════════════════════════════════════════
 *
 * BORDA GLOW
 *
 * A versão anterior usava vários drawRect().
 * Isso fazia os cantos parecerem quadrados.
 *
 * Aqui o glow é desenhado seguindo uma trajetória
 * arredondada próxima da borda da tela.
 *
 * ═══════════════════════════════════════════════ */

@Composable
private fun EdgeGlowBorder(
    modifier: Modifier = Modifier,
    isPlaying: Boolean
) {

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "EdgeGlow"
        )

    /*
     * Respiração geral.
     */
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.52f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Breathe"
    )

    /*
     * Movimento contínuo do ponto de luz.
     */
    val flow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 9000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Flow"
    )

    /*
     * Pequena variação quando existe reprodução.
     * Não é um "visualizer" exagerado.
     */
    val playbackEnergy by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.72f,
        animationSpec = tween(
            durationMillis = 700,
            easing = FastOutSlowInEasing
        ),
        label = "PlaybackEnergy"
    )

    val blue = Color(0xFF3B82F6)
    val cyan = Color(0xFF22D3EE)
    val purple = Color(0xFF8B5CF6)

    Canvas(
        modifier = modifier
    ) {

        val w = size.width
        val h = size.height

        /*
         * Margem da linha em relação à borda.
         */
        val inset = 9.dp.toPx()

        /*
         * Raio suficientemente grande para acompanhar
         * os cantos arredondados da tela.
         */
        val radius = 34.dp.toPx()

        /*
         * Caminho arredondado.
         *
         * Não é um retângulo preenchido.
         * É literalmente uma linha seguindo a borda.
         */
        val path = Path().apply {

            moveTo(
                inset + radius,
                inset
            )

            lineTo(
                w - inset - radius,
                inset
            )

            arcTo(
                rect = Rect(
                    left = w - inset - radius * 2f,
                    top = inset,
                    right = w - inset,
                    bottom = inset + radius * 2f
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(
                w - inset,
                h - inset - radius
            )

            arcTo(
                rect = Rect(
                    left = w - inset - radius * 2f,
                    top = h - inset - radius * 2f,
                    right = w - inset,
                    bottom = h - inset
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(
                inset + radius,
                h - inset
            )

            arcTo(
                rect = Rect(
                    left = inset,
                    top = h - inset - radius * 2f,
                    right = inset + radius * 2f,
                    bottom = h - inset
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(
                inset,
                inset + radius
            )

            arcTo(
                rect = Rect(
                    left = inset,
                    top = inset,
                    right = inset + radius * 2f,
                    bottom = inset + radius * 2f
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            close()
        }

        /*
         * Gradiente que percorre a borda.
         *
         * O flow altera a posição das cores,
         * criando movimento sem parecer uma animação
         * chamativa demais.
         */
        val gradientOffset =
            w * (flow - 0.5f)

        val edgeBrush = Brush.linearGradient(
            colors = listOf(
                blue.copy(
                    alpha =
                        breathe *
                            playbackEnergy *
                            0.90f
                ),
                cyan.copy(
                    alpha =
                        breathe *
                            playbackEnergy *
                            0.74f
                ),
                purple.copy(
                    alpha =
                        breathe *
                            playbackEnergy *
                            0.90f
                ),
                blue.copy(
                    alpha =
                        breathe *
                            playbackEnergy *
                            0.74f
                )
            ),
            start = Offset(
                -w + gradientOffset,
                h
            ),
            end = Offset(
                w + gradientOffset,
                -h
            )
        )

        /*
         * Halo grande.
         */
        drawPath(
            path = path,
            brush = edgeBrush,
            style = Stroke(
                width = 18.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = 0.16f
        )

        /*
         * Halo intermediário.
         */
        drawPath(
            path = path,
            brush = edgeBrush,
            style = Stroke(
                width = 9.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = 0.25f
        )

        /*
         * Linha principal extremamente fina.
         * É ela que deixa a borda definida sem virar
         * uma moldura grossa.
         */
        drawPath(
            path = path,
            brush = edgeBrush,
            style = Stroke(
                width = 2.2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = 0.72f
        )

        /*
         * Pontos de luz nos quatro cantos.
         *
         * Eles são círculos/gradientes, não quadrados.
         */
        val cornerRadius = 85.dp.toPx()

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    cyan.copy(
                        alpha =
                            breathe *
                                playbackEnergy *
                                0.24f
                    ),
                    Color.Transparent
                ),
                radius = cornerRadius
            ),
            radius = cornerRadius,
            center = Offset(
                inset + radius * 0.25f,
                inset + radius * 0.25f
            ),
            alpha = 0.75f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    purple.copy(
                        alpha =
                            breathe *
                                playbackEnergy *
                                0.25f
                    ),
                    Color.Transparent
                ),
                radius = cornerRadius
            ),
            radius = cornerRadius,
            center = Offset(
                w - inset - radius * 0.25f,
                inset + radius * 0.25f
            ),
            alpha = 0.75f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blue.copy(
                        alpha =
                            breathe *
                                playbackEnergy *
                                0.22f
                    ),
                    Color.Transparent
                ),
                radius = cornerRadius
            ),
            radius = cornerRadius,
            center = Offset(
                inset + radius * 0.25f,
                h - inset - radius * 0.25f
            ),
            alpha = 0.75f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    purple.copy(
                        alpha =
                            breathe *
                                playbackEnergy *
                                0.22f
                    ),
                    Color.Transparent
                ),
                radius = cornerRadius
            ),
            radius = cornerRadius,
            center = Offset(
                w - inset - radius * 0.25f,
                h - inset - radius * 0.25f
            ),
            alpha = 0.75f
        )
    }
}