package com.goldensystem.auris.presentation.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.goldensystem.auris.presentation.viewmodel.PlayerState
import com.goldensystem.auris.presentation.viewmodel.VideoPlayerViewModel
import com.goldensystem.auris.utils.VideoUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    onBack: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showControls by remember { mutableStateOf(true) }
    var doubleTapFeedback by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var adjustmentType by remember { mutableStateOf<AdjustmentType?>(null) }
    var adjustmentValue by remember { mutableFloatStateOf(0f) }

    var feedbackAlpha by remember { mutableFloatStateOf(1f) }
    var feedbackScale by remember { mutableFloatStateOf(1f) }
    val feedbackAlphaAnim by animateFloatAsState(feedbackAlpha, spring(dampingRatio = 0.5f, stiffness = 600f), label = "fbAlpha")
    val feedbackScaleAnim by animateFloatAsState(feedbackScale, spring(dampingRatio = 0.5f, stiffness = 600f), label = "fbScale")

    val scope = rememberCoroutineScope()
    var autoHideJob by remember { mutableStateOf<Job?>(null) }

    fun resetAutoHide() {
        autoHideJob?.cancel()
        autoHideJob = scope.launch {
            delay(if (state.isPlaying) 2500L else 4000L)
            showControls = false
        }
    }

    LaunchedEffect(showControls, state.isPlaying) {
        if (showControls) resetAutoHide()
    }

    DisposableEffect(Unit) { onDispose { autoHideJob?.cancel() } }

    // Detecta resolução do vídeo para orientação automática
    LaunchedEffect(state.currentVideo) {
        try {
            val videoPath = state.currentVideo.path
            val mediaPlayer = android.media.MediaPlayer()
            mediaPlayer.setDataSource(videoPath)
            mediaPlayer.prepare()
            val videoWidth = mediaPlayer.videoWidth
            val videoHeight = mediaPlayer.videoHeight
            mediaPlayer.release()

            val activity = context as? Activity
            activity?.let {
                if (videoWidth > videoHeight) {
                    it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
            }
        } catch (_: Exception) {
            // Fallback: mantém orientação atual
        }
    }

    // Fullscreen
    LaunchedEffect(Unit) {
        val w = (context as? Activity)?.window
        w?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            val c = WindowInsetsControllerCompat(it, it.decorView)
            c.hide(WindowInsetsCompat.Type.systemBars())
            c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            try { (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED } catch (_: Exception) {}
            val w = (context as? Activity)?.window
            w?.let {
                val c = WindowInsetsControllerCompat(it, it.decorView)
                c.show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(it, true)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_PAUSE -> viewModel.onPause()
                Lifecycle.Event.ON_RESUME -> viewModel.onResume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    BackHandler {
        try { (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED } catch (_: Exception) {}
        onBack()
    }

    val overlayBrush = remember { Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.42f), Color.Transparent, Color.Black.copy(alpha = 0.42f))) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        // Player
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = viewModel.exoPlayer
                    useController = false
                    controllerAutoShow = false
                    controllerHideOnTouch = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    keepScreenOn = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Thumbnail
        if (state.playerState != PlayerState.READY) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(state.currentVideo.path)
                    .videoFrameMillis(1000)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gradiente
        Box(Modifier.fillMaxSize().background(overlayBrush))

        // Buffering
        if (state.playerState == PlayerState.BUFFERING) {
            LinearProgressIndicator(
                Modifier.align(Alignment.TopCenter).fillMaxWidth().height(2.dp),
                color = Color.White.copy(alpha = 0.6f),
                trackColor = Color.Transparent
            )
        }

        // Double-tap
        if (doubleTapFeedback != null) {
            val (offsetX, _) = doubleTapFeedback!!
            var pulseAlpha by remember { mutableFloatStateOf(1f) }
            val pulseScale by animateFloatAsState(if (pulseAlpha > 0f) 1.6f else 0.8f, tween(400), label = "pulseS")
            LaunchedEffect(doubleTapFeedback) {
                pulseAlpha = 0f
                delay(600)
                doubleTapFeedback = null
            }
            Box(Modifier.align(Alignment.Center).offset(x = if (offsetX < 0.5f) (-60).dp else 60.dp).size(96.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f * pulseAlpha),
                        radius = size.minDimension / 2 * pulseScale,
                        center = center
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (offsetX < 0.5f) Icons.Filled.Replay10 else Icons.Filled.Forward10, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(if (offsetX < 0.5f) "-10s" else "+10s", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Indicador lateral
        adjustmentType?.let { type ->
            AnimatedVisibility(visible = true, enter = fadeIn(tween(200)), exit = fadeOut(tween(250)), label = "adjust") {
                Box(
                    Modifier.align(if (type == AdjustmentType.BRIGHTNESS) Alignment.CenterStart else Alignment.CenterEnd)
                        .padding(horizontal = 32.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(12.dp), contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(if (type == AdjustmentType.BRIGHTNESS) Icons.Outlined.BrightnessHigh else Icons.Outlined.VolumeUp, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("${(adjustmentValue * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.height(60.dp).width(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.3f)), contentAlignment = Alignment.BottomCenter) {
                            Box(Modifier.fillMaxHeight(adjustmentValue).width(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White))
                        }
                    }
                }
            }
            LaunchedEffect(adjustmentType, adjustmentValue) { if (adjustmentType != null) { delay(800); adjustmentType = null } }
        }

        // Gestos
        var dragStartX by remember { mutableFloatStateOf(0f) }
        Box(
            Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls; if (showControls) resetAutoHide() },
                        onDoubleTap = { off ->
                            val d = if (off.x < size.width / 2f) -10000L else 10000L
                            viewModel.seekBy(d)
                            doubleTapFeedback = off.x / size.width to off.y / size.height
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { dragStartX = it.x; resetAutoHide() },
                        onVerticalDrag = { _, da ->
                            val d = -da / size.height.toFloat()
                            if (dragStartX < size.width / 2) {
                                val win = (context as? Activity)?.window
                                val cur = win?.attributes?.screenBrightness?.takeIf { it >= 0f } ?: 0.5f
                                val n = (cur + d).coerceIn(0.01f, 1.0f)
                                if (abs(n - cur) > 0.02f) win?.attributes = win?.attributes?.apply { screenBrightness = n }
                                adjustmentType = AdjustmentType.BRIGHTNESS
                                adjustmentValue = n
                            } else {
                                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val n = (cur + (d * max).toInt()).coerceIn(0, max)
                                if (n != cur) am.setStreamVolume(AudioManager.STREAM_MUSIC, n, 0)
                                adjustmentType = AdjustmentType.VOLUME
                                adjustmentValue = n.toFloat() / max
                            }
                        },
                        onDragEnd = {},
                        onDragCancel = {}
                    )
                }
        )

        // 🔥 CONTROLES - TOPO (animação separada)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { -it / 8 }, // Entra de CIMA
            exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 8 },  // Sai pra CIMA
            label = "controlsTop"
        ) {
            Row(
                Modifier.align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .statusBarsPadding()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.45f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        try { (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED } catch (_: Exception) {}
                        onBack()
                    },
                    Modifier.size(36.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.currentVideo.title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(state.currentVideo.folderName, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }

        // 🔥 CONTROLES - INFERIOR (animação separada e CORRETA)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 8 }, // Entra de BAIXO
            exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { -it / 8 }, // Sai pra BAIXO
            label = "controlsBottom"
        ) {
            Column(
                Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                var isDragging by remember { mutableStateOf(false) }
                var dragPosition by remember { mutableFloatStateOf(0f) }

                // Barra de progresso
                Canvas(
                    Modifier.fillMaxWidth()
                        .height(20.dp)
                        .offset(y = 4.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    isDragging = true
                                    dragPosition = (it.x / size.width).coerceIn(0f, 1f)
                                    resetAutoHide()
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    dragPosition = (dragPosition + dragAmount / size.width).coerceIn(0f, 1f)
                                },
                                onDragEnd = {
                                    viewModel.seekTo((dragPosition * state.durationMs).toLong())
                                    isDragging = false
                                },
                                onDragCancel = { isDragging = false }
                            )
                        }
                ) {
                    val eff = if (isDragging) dragPosition else if (state.durationMs > 0) state.currentPositionMs.toFloat() / state.durationMs else 0f
                    val buf = (viewModel.exoPlayer?.bufferedPercentage?.toFloat() ?: 0f) / 100f
                    val th = if (isDragging) 5.dp.toPx() else 3.dp.toPx()
                    val tr = if (isDragging) 8.dp.toPx() else 3.dp.toPx()

                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.15f),
                        topLeft = Offset.Zero,
                        size = Size(size.width, th),
                        cornerRadius = CornerRadius(th / 2)
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.25f),
                        topLeft = Offset.Zero,
                        size = Size(size.width * buf, th),
                        cornerRadius = CornerRadius(th / 2)
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset.Zero,
                        size = Size(size.width * eff, th),
                        cornerRadius = CornerRadius(th / 2)
                    )
                    if (tr > 0f) {
                        drawCircle(
                            color = Color.White,
                            radius = tr,
                            center = Offset(size.width * eff, th / 2)
                        )
                        if (isDragging) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.5f),
                                radius = tr + 2.dp.toPx(),
                                center = Offset(size.width * eff, th / 2),
                                style = Stroke(1.dp.toPx())
                            )
                        }
                    }
                }

                // Tempo
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val displayedPos = if (isDragging) (dragPosition * state.durationMs).toLong() else state.currentPositionMs
                    Text(
                        VideoUtils.formatDuration(displayedPos),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    Text(
                        VideoUtils.formatDuration(state.durationMs),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Botões
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.goToPrevious() },
                        modifier = Modifier.size(36.dp),
                        enabled = state.queue.hasPrevious
                    ) {
                        Icon(Icons.Filled.SkipPrevious, "Anterior", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    IconButton(
                        { viewModel.seekBy(-10000) },
                        Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Replay10, "-10s", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    
                    Spacer(Modifier.width(12.dp))
                    
                    IconButton(
                        { viewModel.playPause() },
                        Modifier.size(44.dp)
                    ) {
                        AnimatedContent(
                            state.isPlaying,
                            transitionSpec = {
                                scaleIn(spring(dampingRatio = 0.55f, stiffness = 350f)) togetherWith
                                fadeOut(spring(dampingRatio = 0.55f, stiffness = 350f))
                            },
                            label = "playPauseSm"
                        ) { playing ->
                            Icon(
                                if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.width(12.dp))
                    
                    IconButton(
                        { viewModel.seekBy(10000) },
                        Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Forward10, "+10s", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = { viewModel.advanceToNext() },
                        modifier = Modifier.size(36.dp),
                        enabled = state.queue.hasNext
                    ) {
                        Icon(Icons.Filled.SkipNext, "Próximo", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(4.dp))
                
                Text(
                    state.queue.positionDescription,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

private enum class AdjustmentType { BRIGHTNESS, VOLUME }