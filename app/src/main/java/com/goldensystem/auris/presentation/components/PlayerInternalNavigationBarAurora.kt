package com.goldensystem.auris.presentation.components

import com.goldensystem.auris.presentation.navigation.navigateToTopLevelSafely

import android.os.SystemClock
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.goldensystem.auris.BottomNavItem
import com.goldensystem.auris.data.preferences.NavBarStyle
import com.goldensystem.auris.presentation.components.scoped.CustomNavigationBarItemAurora
import com.goldensystem.auris.presentation.navigation.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

@Composable
private fun PlayerInternalNavigationItemsRowAurora(
    navController: NavHostController,
    navItems: ImmutableList<BottomNavItem>,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    navBarStyle: String,
    compactMode: Boolean,
    bottomBarPadding: Dp,
    onSearchIconDoubleTap: () -> Unit
) {
    val navBarInsetPadding = sanitizeNavigationBarBottomInset(
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    )
    val innerRowPadding = (navBarInsetPadding - bottomBarPadding).coerceAtLeast(0.dp)
    val latestCurrentRoute by rememberUpdatedState(currentRoute)
    val latestOnSearchIconDoubleTap by rememberUpdatedState(onSearchIconDoubleTap)
    val latestNavigationEnabled by rememberUpdatedState(currentRoute != null)

    val scope = rememberCoroutineScope()
    var lastSearchTapTimestamp by remember { mutableStateOf(0L) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp)
            .padding(bottom = innerRowPadding),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        navItems.forEach { item ->
            val isSelected = currentRoute != null && currentRoute == item.screen.route
            val selectedColor = MaterialTheme.colorScheme.primary
            val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            val indicatorColorFromTheme = MaterialTheme.colorScheme.secondaryContainer

            val iconPainterResId = if (isSelected && item.selectedIconResId != null && item.selectedIconResId != 0) {
                item.selectedIconResId
            } else {
                item.iconResId
            }
            val iconLambda: @Composable () -> Unit = remember(iconPainterResId, item.label) {
                {
                    Icon(
                        painter = painterResource(id = iconPainterResId),
                        contentDescription = item.label
                    )
                }
            }
            val selectedIconLambda: @Composable () -> Unit = remember(iconPainterResId, item.label) {
                {
                    Icon(
                        painter = painterResource(id = iconPainterResId),
                        contentDescription = item.label
                    )
                }
            }
            val labelLambda: (@Composable () -> Unit)? = if (compactMode) {
                null
            } else {
                remember(item.label) { { Text(item.label) } }
            }
            val onClickLambda: () -> Unit = remember(item.screen.route, navController, scope) {
                click@{
                    if (!latestNavigationEnabled) {
                        lastSearchTapTimestamp = 0L
                        return@click
                    }

                    val itemRoute = item.screen.route
                    val isSearchTab = itemRoute == Screen.Search.route
                    val isAlreadySelected = latestCurrentRoute == itemRoute

                    if (isSearchTab) {
                        val now = SystemClock.elapsedRealtime()
                        val isDoubleTap = now - lastSearchTapTimestamp <= 350L
                        lastSearchTapTimestamp = now

                        if (!isAlreadySelected) {
                            if (!navController.navigateToTopLevelSafely(itemRoute)) {
                                lastSearchTapTimestamp = 0L
                                return@click
                            }
                        }

                        if (isDoubleTap) {
                            lastSearchTapTimestamp = 0L
                            if (isAlreadySelected) {
                                latestOnSearchIconDoubleTap()
                            } else {
                                scope.launch {
                                    delay(160L)
                                    latestOnSearchIconDoubleTap()
                                }
                            }
                        }
                    } else if (!isAlreadySelected) {
                        lastSearchTapTimestamp = 0L
                        navController.navigateToTopLevelSafely(itemRoute)
                    } else {
                        lastSearchTapTimestamp = 0L
                    }
                }
            }
            CustomNavigationBarItemAurora(
                modifier = Modifier.weight(1f),
                selected = isSelected,
                onClick = onClickLambda,
                enabled = currentRoute != null,
                compactMode = compactMode,
                icon = iconLambda,
                selectedIcon = selectedIconLambda,
                label = labelLambda,
                contentDescription = item.label,
                alwaysShowLabel = true,
                selectedIconColor = selectedColor,
                unselectedIconColor = unselectedColor,
                selectedTextColor = selectedColor,
                unselectedTextColor = unselectedColor,
                indicatorColor = indicatorColorFromTheme
            )
        }
    }
}

@Composable
fun PlayerInternalNavigationBarAurora(
    navController: NavHostController,
    navItems: ImmutableList<BottomNavItem>,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    navBarStyle: String,
    compactMode: Boolean,
    bottomBarPadding: Dp = 0.dp,
    topCornerRadius: Dp = 28.dp,
    bottomCornerRadius: Dp = 28.dp,
    onSearchIconDoubleTap: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme

    val capsuleShape: Shape = remember(topCornerRadius, bottomCornerRadius) {
        AbsoluteSmoothCornerShape(
            cornerRadiusTL = topCornerRadius,
            smoothnessAsPercentTL = 60,
            cornerRadiusTR = topCornerRadius,
            smoothnessAsPercentTR = 60,
            cornerRadiusBL = bottomCornerRadius,
            smoothnessAsPercentBL = 60,
            cornerRadiusBR = bottomCornerRadius,
            smoothnessAsPercentBR = 60,
        )
    }

    val sheen = rememberInfiniteTransition(label = "navSheen")
    val sheenPhase by sheen.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenPhase"
    )

    val ambient = rememberInfiniteTransition(label = "navAmbient")
    val ambientPulse by ambient.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientPulse"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { translationY = 4.dp.toPx() }
                .background(
                    color = Color.Black.copy(alpha = 0.22f),
                    shape = capsuleShape
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(capsuleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            scheme.surfaceContainerHigh.copy(alpha = 0.82f),
                            scheme.surfaceContainer.copy(alpha = 0.68f)
                        )
                    ),
                    shape = capsuleShape
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.30f),
                            Color.Transparent,
                            scheme.primary.copy(alpha = 0.28f)
                        )
                    ),
                    shape = capsuleShape
                )
                .drawBehind {
                    val w = size.width
                    val bandWidth = w * 0.35f
                    val cx = sheenPhase * (w + bandWidth) - bandWidth
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            start = Offset(cx, 0f),
                            end = Offset(cx + bandWidth, size.height)
                        )
                    )
                    val avgRadiusPx = ((topCornerRadius + bottomCornerRadius) / 2).toPx()
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                scheme.primary.copy(alpha = 0.18f * ambientPulse),
                                Color.Transparent,
                                scheme.tertiary.copy(alpha = 0.16f * ambientPulse)
                            )
                        ),
                        cornerRadius = CornerRadius(avgRadiusPx)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            PlayerInternalNavigationItemsRowAurora(
                navController = navController,
                navItems = navItems,
                currentRoute = currentRoute,
                navBarStyle = navBarStyle,
                compactMode = compactMode,
                bottomBarPadding = bottomBarPadding,
                onSearchIconDoubleTap = onSearchIconDoubleTap,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}