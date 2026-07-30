// presentation/screens/SplashScreen.kt
package com.goldensystem.auris.presentation.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun SplashScreen(
    onAnimationComplete: () -> Unit
) {
    // Timer de 0.80 segundos
    LaunchedEffect(Unit) {
        delay(800) // 0.80 segundos
        onAnimationComplete()
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0A12),
            Color(0xFF12121F),
            Color(0xFF0D0D17)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        // Partículas flutuantes
        FloatingParticles()

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

        // Texto "by Golden System" no rodapé
        Text(
            text = "by Golden System",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.5.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(0.7f)
        )
    }
}

@Composable
private fun FloatingParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val particles = remember {
        List(25) { idx ->
            ParticleData(
                x = (0..1000).random() / 1000f,
                y = (0..1000).random() / 1000f,
                delayMs = (200 + (0..300).random()).toLong(),
                size = (1.5f + (0..3).random()).dp,
                alphaBase = 0.08f + (0..200).random() / 1000f * 0.25f
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

        Box(
            modifier = Modifier
                .offset(
                    x = (particle.x * 300 - 150).dp,
                    y = (particle.y * 500 - 250).dp + (floatValue * 35).dp
                )
                .size(particle.size)
                .alpha(particle.alphaBase + (0.15f * (1f - floatValue.coerceIn(0f, 1f))))
                .background(Color.White, CircleShape)
        )
    }
}

private data class ParticleData(
    val x: Float,
    val y: Float,
    val delayMs: Long,
    val size: androidx.compose.ui.unit.Dp,
    val alphaBase: Float
)