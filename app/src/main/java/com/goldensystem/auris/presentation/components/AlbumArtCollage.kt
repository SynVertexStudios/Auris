package com.goldensystem.auris.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import com.goldensystem.auris.R
import com.goldensystem.auris.data.model.Song
import com.goldensystem.auris.data.preferences.CollagePattern
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlin.math.cos
import kotlin.math.sin

@Stable
data class Config(
    val size: Dp,
    val width: Dp,
    val height: Dp,
    val align: Alignment,
    val rot: Float,
    val shape: Shape,
    val offsetX: Dp,
    val offsetY: Dp
)

@Composable
fun AlbumArtCollage(
    songs: ImmutableList<Song>,
    modifier: Modifier = Modifier,
    height: Dp = 400.dp,
    padding: Dp = 0.dp,
    pattern: CollagePattern = CollagePattern.default,
    onSongClick: (Song) -> Unit,
) {
    val context = LocalContext.current

    val songsToShow = remember(songs) {
        (songs.take(6) + List((6 - songs.size).coerceAtLeast(0)) { null }).toImmutableList()
    }

    // context nas keys para não segurar Activity recriada
    val requests = remember(songsToShow, context) {
        songsToShow.map { song ->
            song?.albumArtUriString?.let {
                ImageRequest.Builder(context)
                    .data(it)
                    .dispatcher(Dispatchers.IO)
                    .crossfade(true)
                    .error(R.drawable.ic_music_placeholder)
                    .build()
            }
        }.toImmutableList()
    }

    val aurora = rememberInfiniteTransition(label = "collageAurora")
    val auroraAngle by aurora.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(28_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auroraAngle"
    )
    val floatPhase by aurora.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(9_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floatPhase"
    )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(padding)
    ) {
        val boxMaxHeight = maxHeight

        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val a = Math.toRadians(auroraAngle.toDouble())
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.minDimension * 1.05f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primary.copy(alpha = 0.30f), Color.Transparent),
                            center = Offset(
                                cx + (cos(a) * size.width * 0.30f).toFloat(),
                                cy + (sin(a) * size.height * 0.30f).toFloat()
                            ),
                            radius = r
                        ),
                        radius = r
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(tertiary.copy(alpha = 0.24f), Color.Transparent),
                            center = Offset(
                                cx - (cos(a) * size.width * 0.35f).toFloat(),
                                cy - (sin(a) * size.height * 0.25f).toFloat()
                            ),
                            radius = r * 0.85f
                        ),
                        radius = r * 0.85f
                    )
                }
        )

        // Cálculo puro e síncrono — buildCollageConfigs é O(1), não precisa de coroutine.
        // Antes isso era um produceState rodando em Dispatchers.Default, o que causava
        // coroutines empilhadas a cada troca de pattern + ida/volta de tela, saturando
        // o pool compartilhado e travando a UI.
        val shapeConfigs = remember(pattern, boxMaxHeight, height) {
            val min = minOf(300.dp, height)
            buildCollageConfigs(pattern, min, boxMaxHeight)
        }

        if (shapeConfigs.isNotEmpty()) {
            val topConfigs = remember(shapeConfigs) { shapeConfigs.take(3) }
            val bottomConfigs = remember(shapeConfigs) { shapeConfigs.drop(3) }

            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(boxMaxHeight * 0.6f)
                ) {
                    topConfigs.forEachIndexed { idx, cfg ->
                        songsToShow.getOrNull(idx)?.let { song ->
                            CollageCard(
                                modifier = Modifier.align(cfg.align),
                                request = requests.getOrNull(idx),
                                cfg = cfg,
                                floatPhase = floatPhase + idx * 0.55f,
                                index = idx,
                                song = song,
                                onSongClick = onSongClick
                            )
                        }
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(boxMaxHeight * 0.4f)
                ) {
                    bottomConfigs.forEachIndexed { j, cfg ->
                        songsToShow.getOrNull(j + 3)?.let { song ->
                            CollageCard(
                                modifier = Modifier.align(cfg.align),
                                request = requests.getOrNull(j + 3),
                                cfg = cfg,
                                floatPhase = floatPhase + (j + 3) * 0.55f,
                                index = j + 3,
                                song = song,
                                onSongClick = onSongClick
                            )
                        }
                    }
                }
            }
        }

        if (songs.isEmpty()) {
            Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    primary.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_music_note_24),
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CollageCard(
    modifier: Modifier,
    request: Any?,
    cfg: Config,
    floatPhase: Float,
    index: Int,
    song: Song,
    onSongClick: (Song) -> Unit,
) {
    val bob = sin(floatPhase) * 8f
    val sway = cos(floatPhase * 0.7f) * 3f

    val surface = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .offset(cfg.offsetX, cfg.offsetY)
            .graphicsLayer {
                translationY = bob
                translationX = sway
                rotationZ = cfg.rot + sway * 0.08f
                shadowElevation = 24f
                shape = cfg.shape
                clip = false
                val base = if (index == 0) 1.03f else 1f
                scaleX = base
                scaleY = base
            }
            .size(cfg.width, cfg.height)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onSongClick(song) }
            .drawBehind {
                val r = size.maxDimension * 0.9f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primary.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = r
                    ),
                    radius = r
                )
            }
            .clip(cfg.shape)
            .background(
                shape = cfg.shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                shape = cfg.shape
            )
    ) {
        SmartImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            surface.copy(alpha = 0.10f)
                        )
                    )
                )
        )
    }
}
