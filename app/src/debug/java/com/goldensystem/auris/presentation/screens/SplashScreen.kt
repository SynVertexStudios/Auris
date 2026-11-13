// presentation/screens/SplashScreenBeta.kt
package com.goldensystem.auris.presentation.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goldensystem.auris.R
import kotlinx.coroutines.delay

private val BetaAccent = Color(0xFFFFB74D)   // âmbar suave
private val BetaAccentGlow = Color(0xFFFFA726) // âmbar mais saturado

@Composable
fun SplashScreenBeta(
    onAnimationComplete: () -> Unit
) {
    // Timer de 0.80 segundos (igual à release)
    LaunchedEffect(Unit) {
        delay(800)
        onAnimationComplete()
    }

    // Gradiente com leve tom âmbar na base para diferenciar a build BETA
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0A12),
            Color(0xFF12121F),
            Color(0xFF1A1508) // base com toque dourado/âmbar
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        // Partículas flutuantes (agora com leve tint âmbar em algumas)
        FloatingParticlesBeta()

        // Logo centralizado
        Image(
            painter = painterResource(R.drawable.ic_auris_logo_transparent),
            contentDescription = "Auris Logo",
            modifier = Modifier.size(170.dp),
            contentScale = ContentScale.Fit
        )

        // Texto "Auris" abaixo do logo
        Text(
            text = "Auris",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier
                .padding(top = 240.dp)
                .alpha(0.9f)
        )

        // Badge "BETA" — logo abaixo do nome do app
        BetaBadge(
            modifier = Modifier
                .padding(top = 300.dp)
                .align(Alignment.Center)
        )

        // Aviso de build de teste no rodapé
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "by Golden System  •  BETA",
                color = BetaAccent.copy(alpha = 0.55f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(0.85f)
            )
        }
    }
}

@Composable
private fun BetaBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "beta_badge")
    val pulse: Float by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beta_pulse"
    )

    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = BetaAccent.copy(alpha = 0.35f + 0.35f * pulse),
                shape = RoundedCornerShape(50)
            )
            .background(
                color = BetaAccent.copy(alpha = 0.08f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 14.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Bolinha pulsante indicando "build viva"
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(BetaAccentGlow.copy(alpha = 0.4f + 0.6f * pulse), CircleShape)
        )
        Text(
            text = "BETA",
            color = BetaAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun FloatingParticlesBeta() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles_beta")

    val particles = remember {
        List(25) { _ ->
            ParticleData(
                x = (0..1000).random() / 1000f,
                y = (0..1000).random() / 1000f,
                delayMs = (200 + (0..300).random()).toLong(),
                size = (1.5f + (0..3).random()).dp,
                alphaBase = 0.08f + (0..200).random() / 1000f * 0.25f,
                // ~30% das partículas ganham um toque âmbar na versão beta
                isAmber = (0..100).random() < 30
            )
        }
    }

    particles.forEach { particle ->
        val floatValue: Float by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (1500 + (0..1000).random()).toInt(),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "particle_${particle.hashCode()}"
        )

        val particleColor = if (particle.isAmber) BetaAccentGlow else Color.White

        Box(
            modifier = Modifier
                .offset(
                    x = (particle.x * 300 - 150).dp,
                    y = (particle.y * 500 - 250).dp + (floatValue * 35).dp
                )
                .size(particle.size)
                .alpha(particle.alphaBase + (0.15f * (1f - floatValue.coerceIn(0f, 1f))))
                .background(particleColor, CircleShape)
        )
    }
}

private data class ParticleData(
    val x: Float,
    val y: Float,
    val delayMs: Long,
    val size: androidx.compose.ui.unit.Dp,
    val alphaBase: Float,
    val isAmber: Boolean = false
)