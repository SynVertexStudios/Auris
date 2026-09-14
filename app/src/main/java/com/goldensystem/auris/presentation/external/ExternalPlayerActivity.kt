package com.goldensystem.auris.presentation.external

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goldensystem.auris.ui.theme.AurisTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExternalPlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val audioUri = extractAudioUri(intent)
        
        setContent {
            AurisTheme(darkTheme = true) {
                ExternalPlayerScreen(
                    audioUri = audioUri,
                    onClose = { finish() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Aqui você pode atualizar o ViewModel com o novo Uri
        // Ex: viewModel.loadAudio(extractAudioUri(intent))
    }

    private fun extractAudioUri(intent: Intent?): Uri? {
        if (intent == null) return null
        
        return when {
            intent.action == Intent.ACTION_VIEW -> intent.data
            intent.action == Intent.ACTION_SEND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
            }
            else -> null
        }
    }
}

@Composable
fun ExternalPlayerScreen(
    audioUri: Uri?,
    onClose: () -> Unit,
    viewModel: ExternalPlayerViewModel = viewModel()
) {
    // Carregar o áudio quando a tela é criada
    LaunchedEffect(audioUri) {
        audioUri?.let { viewModel.loadAudio(it) }
    }

    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent) // Fundo totalmente transparente
    ) {
        // 1. Brilho nas bordas (Edge Glow)
        EdgeGlow(
            isPlaying = uiState.isPlaying,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Controles centrais
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Nome da música
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Artista / Nome do arquivo
            Text(
                text = uiState.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Controles de reprodução
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Botão Anterior
                IconButton(
                    onClick = { viewModel.previous() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // Botão Play/Pause
                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) 
                            androidx.compose.material.icons.Icons.Default.Pause 
                        else 
                            androidx.compose.material.icons.Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pausar" else "Reproduzir",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // Botão Próximo
                IconButton(
                    onClick = { viewModel.next() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.SkipNext,
                        contentDescription = "Próximo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Botão Fechar (opcional, mas útil)
            TextButton(onClick = onClose) {
                Text(
                    text = "Fechar",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun EdgeGlow(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EdgeGlow")
    
    // Animação de pulso
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )
    
    // Animação de rotação das cores
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val glowAlpha = if (isPlaying) pulseAlpha else 0.2f
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val borderWidth = 60.dp.toPx()
        
        // Brilho nas bordas com gradiente
        // Topo
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF00B4FF).copy(alpha = glowAlpha),
                    Color.Transparent
                ),
                startY = 0f,
                endY = borderWidth
            ),
            size = androidx.compose.ui.geometry.Size(width, borderWidth),
            topLeft = Offset(0f, 0f)
        )
        
        // Base
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF00B4FF).copy(alpha = glowAlpha)
                ),
                startY = height - borderWidth,
                endY = height
            ),
            size = androidx.compose.ui.geometry.Size(width, borderWidth),
            topLeft = Offset(0f, height - borderWidth)
        )
        
        // Esquerda
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF00B4FF).copy(alpha = glowAlpha),
                    Color.Transparent
                ),
                startX = 0f,
                endX = borderWidth
            ),
            size = androidx.compose.ui.geometry.Size(borderWidth, height),
            topLeft = Offset(0f, 0f)
        )
        
        // Direita
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF00B4FF).copy(alpha = glowAlpha)
                ),
                startX = width - borderWidth,
                endX = width
            ),
            size = androidx.compose.ui.geometry.Size(borderWidth, height),
            topLeft = Offset(width - borderWidth, 0f)
        )
    }
}