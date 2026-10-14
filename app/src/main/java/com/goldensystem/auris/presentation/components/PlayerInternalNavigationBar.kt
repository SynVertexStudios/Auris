package com.goldensystem.auris.presentation.components.scoped

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.shadow
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

    // ── Microinterações spring (mais vivas que o original)
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.14f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )
    val indicatorScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "indicatorScale"
    )
    // ✨ Novo: glow do item selecionado
    val glowAlpha by animateFloatAsState(
        targetValue = if (selected) 0.42f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "glowAlpha"
    )

    val showLabel = label != null && (alwaysShowLabel || selected)

    // ── Dimensões
    val indicatorWidth = 68.dp
    val indicatorHeight = 34.dp
    val iconWidth = 48.dp
    val iconHeight = 26.dp
    val indicatorPadding = 4.dp
    val indicatorShape = RoundedCornerShape(18.dp)
    val iconShape = RoundedCornerShape(12.dp)

    // ── Gradiente do indicador (aurora) — primária → terciária
    val indicatorGradient = remember(indicatorColor, scheme.primary, scheme.tertiary) {
        Brush.linearGradient(
            colors = listOf(
                indicatorColor,
                scheme.primary.copy(alpha = 0.35f).let { indicatorColor.copy(alpha = 0.85f) },
                scheme.tertiary.copy(alpha = 0.28f).let { indicatorColor.copy(alpha = 0.65f) },
            )
        )
    }

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
        // ── Container do ícone + indicador aurora
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(indicatorWidth, indicatorHeight)
        ) {
            // ✨ Glow ambiente atrás do indicador (só quando selecionado)
            if (glowAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        scheme.primary.copy(alpha = glowAlpha),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = size.minDimension * 1.1f
                                ),
                                radius = size.minDimension * 1.1f
                            )
                        }
                )
            }

            // ── Indicador com gradiente aurora + borda de vidro
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(animationSpec = tween(180)) +
                        scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                                initialVelocity = 0f
                            ),
                            initialScale = 0.72f
                        ),
                exit = fadeOut(animationSpec = tween(160)) +
                        scaleOut(
                            animationSpec = tween(180, easing = EaseInQuart),
                            targetScale = 0.85f
                        )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = indicatorPadding)
                        .graphicsLayer {
                            scaleX = indicatorScale
                            scaleY = indicatorScale
                        }
                        .shadow(
                            elevation = 12.dp,
                            shape = indicatorShape,
                            clip = false,
                            ambientColor = scheme.primary,
                            spotColor = scheme.primary
                        )
                        .clip(indicatorShape)
                        .background(brush = indicatorGradient, shape = indicatorShape)
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.42f),
                                    Color.Transparent,
                                    scheme.tertiary.copy(alpha = 0.30f),
                                )
                            ),
                            shape = indicatorShape
                        )
                )
            }

            // ── Ícone
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(iconWidth, iconHeight)
                    .clip(iconShape)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            ) {
                CompositionLocalProvider(LocalContentColor provides iconColor) {
                    Box(
                        modifier = Modifier.clearAndSetSemantics {
                            if (showLabel) {
                                // semântica gerenciada acima
                            }
                        }
                    ) {
                        if (selected) selectedIcon() else icon()
                    }
                }
            }
        }

        // ── Label com fade/slide refinado
        AnimatedVisibility(
            visible = showLabel,
            enter = fadeIn(animationSpec = tween(220, delayMillis = 60)),
            exit = fadeOut(animationSpec = tween(120))
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.padding(top = 4.dp)) {
                ProvideTextStyle(
                    value = MaterialTheme.typography.labelMedium.copy(
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    )
                ) {
                    label?.invoke()
                }
            }
        }
    }
}

// Easing curves
private val EaseInQuart = CubicBezierEasing(0.5f, 0f, 0.75f, 0f)
