package com.goldensystem.auris.presentation.components.external

import androidx.compose.ui.geometry.CornerRadius
import android.os.Build
import android.graphics.RuntimeShader
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ShaderBrush
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

private val EDGE_GLOW_SHADER = """
    uniform float2 resolution;
    uniform float time;

    const float PI = 3.14159265359;

    /*
     * Distância assinada de um retângulo arredondado.
     *
     * 0 = exatamente na borda
     * negativo = dentro
     * positivo = fora
     */
    float roundedBoxSdf(float2 p, float2 halfSize, float radius) {
        float2 q = abs(p) - halfSize + radius;

        return length(max(q, 0.0))
            + min(max(q.x, q.y), 0.0)
            - radius;
    }

    /*
     * Converte a posição do pixel em uma posição 0..1
     * ao longo do perímetro do retângulo.
     *
     * Começa no topo, no canto superior esquerdo,
     * e percorre no sentido horário.
     */
    float perimeterPosition(
        float2 p,
        float width,
        float height,
        float radius
    ) {
        float straightW = width - radius * 2.0;
        float straightH = height - radius * 2.0;

        float arc = radius * PI * 0.5;

        float topStart = 0.0;
        float topEnd = straightW;

        float topRightEnd = topEnd + arc;

        float rightEnd =
            topRightEnd + straightH;

        float bottomRightEnd =
            rightEnd + arc;

        float bottomEnd =
            bottomRightEnd + straightW;

        float bottomLeftEnd =
            bottomEnd + arc;

        float leftEnd =
            bottomLeftEnd + straightH;

        float topLeftEnd =
            leftEnd + arc;

        float perimeter = topLeftEnd;

        float x = p.x;
        float y = p.y;

        /*
         * TOPO
         */
        if (y <= radius && x >= radius && x <= width - radius) {
            return clamp(
                (x - radius) / perimeter,
                0.0,
                1.0
            );
        }

        /*
         * CANTO SUPERIOR DIREITO
         */
        if (x > width - radius && y < radius) {
            float2 c = float2(
                width - radius,
                radius
            );

            float angle = atan(
                y - c.y,
                x - c.x
            );

            float localArc =
                (angle + PI * 0.5) * radius;

            return (
                topEnd + localArc
            ) / perimeter;
        }

        /*
         * DIREITA
         */
        if (x >= width - radius &&
            y >= radius &&
            y <= height - radius) {

            return (
                topRightEnd +
                (y - radius)
            ) / perimeter;
        }

        /*
         * CANTO INFERIOR DIREITO
         */
        if (x > width - radius &&
            y > height - radius) {

            float2 c = float2(
                width - radius,
                height - radius
            );

            float angle = atan(
                y - c.y,
                x - c.x
            );

            float localArc =
                angle * radius;

            return (
                rightEnd +
                localArc
            ) / perimeter;
        }

        /*
         * INFERIOR
         */
        if (y >= height - radius &&
            x >= radius &&
            x <= width - radius) {

            return (
                bottomRightEnd +
                (width - radius - x)
            ) / perimeter;
        }

        /*
         * CANTO INFERIOR ESQUERDO
         */
        if (x < radius &&
            y > height - radius) {

            float2 c = float2(
                radius,
                height - radius
            );

            float angle = atan(
                y - c.y,
                x - c.x
            );

            float localArc =
                (PI * 0.5 - angle) * radius;

            return (
                bottomEnd +
                localArc
            ) / perimeter;
        }

        /*
         * ESQUERDA
         */
        if (x <= radius &&
            y >= radius &&
            y <= height - radius) {

            return (
                bottomLeftEnd +
                (height - radius - y)
            ) / perimeter;
        }

        /*
         * CANTO SUPERIOR ESQUERDO
         */
        float2 c = float2(
            radius,
            radius
        );

        float angle = atan(
            y - c.y,
            x - c.x
        );

        if (angle < 0.0) {
            angle += PI * 2.0;
        }

        float localArc =
            (angle - PI) * radius;

        return (
            leftEnd +
            localArc
        ) / perimeter;
    }

    /*
     * Distância circular entre dois pontos 0..1.
     */
    float circularDistance(float a, float b) {
        float d = abs(a - b);
        return min(d, 1.0 - d);
    }

    half4 main(float2 fragCoord) {

        float width = resolution.x;
        float height = resolution.y;

        /*
         * Mesmo tamanho visual da versão anterior.
         */
        float radius = 42.0;

        /*
         * A espessura total da borda.
         */
        float halfBorder = 44.0;

        float2 center = resolution * 0.5;

        float2 p = fragCoord - center;

        float2 halfSize =
            resolution * 0.5;

        /*
         * Limita o raio para telas pequenas.
         */
        radius = min(
            radius,
            min(halfSize.x, halfSize.y) - 2.0
        );

        /*
         * ---------------------------------------------------------
         * DISTÂNCIA DA BORDA
         * ---------------------------------------------------------
         */

        float sdf = roundedBoxSdf(
            p,
            halfSize,
            radius
        );

        float distanceToEdge = abs(sdf);

        /*
         * Fade matemático da espessura.
         *
         * O centro é mais forte.
         * Conforme chega na extremidade, desaparece
         * progressivamente.
         *
         * Não existe uma faixa sólida.
         */
        float edgeMask =
            1.0 -
            smoothstep(
                halfBorder * 0.15,
                halfBorder,
                distanceToEdge
            );

        /*
         * Anti-aliasing extra.
         */
        edgeMask *=
            1.0 -
            smoothstep(
                halfBorder,
                halfBorder + 1.5,
                distanceToEdge
            );

        /*
         * Fora da região da borda não desenha nada.
         */
        if (edgeMask <= 0.001) {
            return half4(0.0);
        }

        /*
         * ---------------------------------------------------------
         * POSIÇÃO NO PERÍMETRO
         * ---------------------------------------------------------
         */

        float2 pixel = fragCoord;

        float perimeterT = perimeterPosition(
            pixel,
            width,
            height,
            radius
        );

        /*
         * ---------------------------------------------------------
         * ENERGIA SE MOVENDO PELA BORDA
         * ---------------------------------------------------------
         *
         * Não é uma bolinha.
         *
         * É uma região enorme e suave de intensidade.
         */
        float movingPosition =
            fract(time * 0.00010);

        float distanceFromEnergy =
            circularDistance(
                perimeterT,
                movingPosition
            );

        /*
         * O 0.17 deixa a região bastante larga.
         */
        float energy =
            exp(
                -pow(
                    distanceFromEnergy / 0.17,
                    2.0
                )
            );

        /*
         * Segunda região, mais fraca e mais lenta.
         *
         * Isso evita que pareça simplesmente uma única
         * mancha viajando.
         */
        float secondPosition =
            fract(
                time * 0.000055 + 0.43
            );

        float secondDistance =
            circularDistance(
                perimeterT,
                secondPosition
            );

        float secondaryEnergy =
            exp(
                -pow(
                    secondDistance / 0.23,
                    2.0
                )
            ) * 0.28;

        /*
         * Respiração geral extremamente leve.
         */
        float breathing =
            0.82 +
            0.18 *
            (0.5 + 0.5 * sin(time * 0.0017));

        /*
         * Intensidade final.
         */
        float intensity =
            0.34 +
            energy * 0.62 +
            secondaryEnergy;

        intensity *= breathing;

        /*
         * ---------------------------------------------------------
         * CORES
         * ---------------------------------------------------------
         *
         * A cor também muda suavemente ao longo do perímetro.
         * Não há blocos separados de azul/ciano/roxo.
         */

        float colorPhase =
            perimeterT * PI * 2.0
            - time * 0.00016;

        float cyanWeight =
            0.5 +
            0.5 * cos(colorPhase);

        float purpleWeight =
            0.5 +
            0.5 * cos(
                colorPhase - PI * 0.90
            );

        float blueWeight =
            0.5 +
            0.5 * cos(
                colorPhase + PI * 0.90
            );

        float total =
            cyanWeight +
            purpleWeight +
            blueWeight;

        cyanWeight /= total;
        purpleWeight /= total;
        blueWeight /= total;

        float3 cyan =
            float3(
                0.133,
                0.827,
                0.933
            );

        float3 blue =
            float3(
                0.231,
                0.510,
                0.965
            );

        float3 purple =
            float3(
                0.545,
                0.361,
                0.965
            );

        float3 color =
            cyan * cyanWeight +
            blue * blueWeight +
            purple * purpleWeight;

        /*
         * ---------------------------------------------------------
         * PEQUENO NÚCLEO DE LUZ
         * ---------------------------------------------------------
         *
         * Só aparece onde a energia está passando.
         */
        float core =
            pow(
                energy,
                2.8
            );

        color = mix(
            color,
            float3(0.82, 0.97, 1.0),
            core * 0.38
        );

        /*
         * Alpha final.
         */
        float alpha =
            edgeMask *
            intensity *
            0.72;

        /*
         * AGSL exige saída premultiplicada
         * quando usamos transparência.
         */
        return half4(
            color * alpha,
            alpha
        );
    }
""".trimIndent()

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

   var hasEverHadSong by remember { mutableStateOf(false) }

LaunchedEffect(currentSong) {
    if (currentSong != null) {
        hasEverHadSong = true
    } else if (hasEverHadSong) {
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // Borda animada
        EdgeGlowBorder(
            modifier = Modifier.fillMaxSize()
        )

        // Área externa → fecha
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
                rawPosition.coerceIn(
                    0L,
                    totalDuration
                )

            val progressFraction =
                if (totalDuration > 0L) {
                    position.toFloat() / totalDuration
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
             * Fundo translúcido do player.
             *
             * Não é um card sólido:
             * deixa o conteúdo atrás aparecer,
             * mas cria contraste suficiente para o player.
             */
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(
                        bottom = bottomPadding + 8.dp
                    )
                    .clip(
                        RoundedCornerShape(24.dp)
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF071923).copy(alpha = 0.72f),
                                Color(0xFF0A1024).copy(alpha = 0.78f)
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null
                    ) {
                        // Consome o clique.
                    }
                    .padding(
                        horizontal = 16.dp,
                        vertical = 14.dp
                    )
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    /*
                     * Capa + informações
                     */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        OptimizedAlbumArt(
                            uri = currentSong.albumArtUriString,
                            title = currentSong.title,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = currentSong.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFFE8FAFF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(
                                modifier = Modifier.height(2.dp)
                            )

                            Text(
                                text = currentSong.displayArtist,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9DD9E8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    /*
                     * Barra de progresso
                     */
                    WavyMusicSlider(
                        value = sliderPosition,
                        onValueChange = { newValue ->
                            isUserScrubbing = true
                            sliderPosition =
                                newValue.coerceIn(0f, 1f)
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
                        isPlaying = stablePlayerState.isPlaying,
                        isWaveEligible = true,
                        semanticsLabel = "Playback position"
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    /*
                     * Tempos
                     */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = formatDuration(position),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8CCBD9)
                        )

                        Text(
                            text = formatDuration(totalDuration),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8CCBD9)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    /*
                     * Controles.
                     *
                     * O fundo dos botões agora usa a mesma
                     * família de cores da borda.
                     */
                    AnimatedPlaybackControls(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),

                        isPlayingProvider = {
                            stablePlayerState.isPlaying
                        },

                        onPrevious =
                            playerViewModel::previousSong,

                        onPlayPause =
                            playerViewModel::playPause,

                        onNext =
                            playerViewModel::nextSong,

                        height = 60.dp,

                        pressAnimationSpec =
                            controlAnimationSpec,

                        colorOtherButtons =
                            Color(0xFF1D4E67)
                                .copy(alpha = 0.38f),

                        colorPlayPause =
                            Color(0xFF16BFD4)
                                .copy(alpha = 0.30f),

                        tintPlayPauseIcon =
                            Color(0xFFD9FAFF),

                        tintOtherIcons =
                            Color(0xFF9DE8F5)
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )
                }
            }

        } else {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = bottomPadding + 40.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF35D5E8)
                )
            }
        }
    }
}

@Composable
private fun EdgeGlowBorder(
    modifier: Modifier = Modifier
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        EdgeGlowBorderShader(modifier)
    } else {
        EdgeGlowBorderFallback(modifier)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun EdgeGlowBorderShader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition =
        rememberInfiniteTransition(
            label = "EdgeGlowShader"
        )

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 60000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShaderTime"
    )

    val shader = remember {
        RuntimeShader(
            EDGE_GLOW_SHADER
        )
    }

    val shaderBrush = remember(shader) {
        ShaderBrush(shader)
    }

    Canvas(
        modifier = modifier
    ) {
        shader.setFloatUniform(
            "resolution",
            size.width,
            size.height
        )

        shader.setFloatUniform(
            "time",
            time
        )

        drawRect(
            brush = shaderBrush
        )
    }
}

@Composable
private fun EdgeGlowBorderFallback(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        drawRoundRect(
            color = Color(0xFF3B82F6).copy(alpha = 0.45f),
            style = Stroke(
                width = 3.5.dp.toPx()
            ),
            cornerRadius = CornerRadius(
                42.dp.toPx(),
                42.dp.toPx()
            )
        )
    }
}