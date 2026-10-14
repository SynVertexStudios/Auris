package com.goldensystem.auris.presentation.components.scoped

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Item de navegação no estilo "Aurora Glass":
 * - pílula indicadora em gradiente com anel luminoso
 * - halo radial animado atrás do ícone selecionado
 * - ícone com spring bouncy + elevação
 * - label com fade/slide suave e cor animada
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RowScope.CustomNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    selectedIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compactMode: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    contentDescription: String? = null,
    alwaysShowLabel: Boolean = true,
    selectedIconColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    unselectedIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedTextColor: Color = MaterialTheme.colorScheme.onSurface,
    unselectedTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val scheme = MaterialTheme.colorScheme

    // ── Cores animadas
    val iconColor by animateColorAsState(
        targetValue = if (selected) selectedIconColor else unselectedIconColor,
        animationSpec = tween(durationMillis = 220),
        label = "iconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) selectedTextColor else unselectedTextColor,
        animationSpec = tween(durationMillis = 220),
        label = "textColor"
    )

    // ── Escala com mola + leve "lift"
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.14f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "iconScale"
    )
    val iconLift by animateFloatAsState(
        targetValue = if (selected) -1.5f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconLift"
    )

    // ── Halo pulsante quando selecionado (sutil, respira sozinho)
    val pulse = rememberInfiniteTransition(label = "navPulse")
    val haloPulse by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloPulse"
    )
    val haloAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(280),
        label = "haloAlpha"
    )

    val showLabel = label != null && (alwaysShowLabel || selected)
    val indicatorWidth = 66.dp
    val indicatorHeight = 34.dp
    val iconBoxWidth = 48.dp
    val iconBoxHeight = 26.dp
    val indicatorShape = RoundedCornerShape(18.dp)
    val iconShape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null
            )
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(indicatorWidth, indicatorHeight)
        ) {
            // ── Halo ambiente (radial), aparece apenas quando selecionado
            Box(
                modifier = Modifier
                    .size(indicatorWidth + 16.dp, indicatorHeight + 16.dp)
                    .graphicsLayer { alpha = haloAlpha * 0.85f }
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    scheme.primary.copy(alpha = 0.35f * haloPulse),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.minDimension / 2f
                            ),
                            radius = size.minDimension / 2f
                        )
                    }
            )

            // ── Pílula indicadora em gradiente + anel luminoso
            // Usamos explicitamente a versão do pacote androidx.compose.animation
            // para evitar a resolução para ColumnScope.AnimatedVisibility
            // (que exige receiver implícito e falha aqui dentro de Box).
            androidx.compose.animation.AnimatedVisibility(
                visible = selected,
                enter = fadeIn(animationSpec = tween(140)) +
                        scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialScale = 0.7f
                        ),
                exit = fadeOut(animationSpec = tween(120)) +
                        scaleOut(
                            animationSpec = tween(140, easing = EaseInQuart),
                            targetScale = 0.85f
                        )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp)
                        .clip(indicatorShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    scheme.primary.copy(alpha = 0.28f),
                                    scheme.tertiary.copy(alpha = 0.22f)
                                )
                            ),
                            shape = indicatorShape
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.40f),
                                    Color.Transparent,
                                    scheme.primary.copy(alpha = 0.35f)
                                )
                            ),
                            shape = indicatorShape
                        )
                )
            }

            // ── Ícone com spring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(iconBoxWidth, iconBoxHeight)
                    .clip(iconShape)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        translationY = iconLift
                    }
            ) {
                CompositionLocalProvider(LocalContentColor provides iconColor) {
                    Box(modifier = Modifier.clearAndSetSemantics { }) {
                        if (selected) selectedIcon() else icon()
                    }
                }
            }
        }

        // ── Label com fade + slide — também qualificado explicitamente
        androidx.compose.animation.AnimatedVisibility(
            visible = showLabel,
            enter = fadeIn(animationSpec = tween(220, delayMillis = 40)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(220, delayMillis = 40)
                    ),
            exit = fadeOut(animationSpec = tween(110))
        ) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(modifier = Modifier.padding(top = 2.dp)) {
                ProvideTextStyle(
                    value = MaterialTheme.typography.labelMedium.copy(
                        color = textColor,
                        fontSize = 12.5.sp,
                        letterSpacing = 0.2.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                ) {
                    label?.invoke()
                }
            }
        }
    }
}

private val EaseInQuart = CubicBezierEasing(0.5f, 0f, 0.75f, 0f)
