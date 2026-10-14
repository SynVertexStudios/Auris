package com.goldensystem.auris.presentation.components

import com.goldensystem.auris.presentation.navigation.navigateToTopLevelSafely

import android.os.SystemClock
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.goldensystem.auris.BottomNavItem
import com.goldensystem.auris.data.preferences.NavBarStyle
import com.goldensystem.auris.presentation.components.scoped.CustomNavigationBarItem
import com.goldensystem.auris.presentation.navigation.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal val NavBarContentHeight = 90.dp
internal val NavBarCompactContentHeight = 64.dp
internal val NavBarContentHeightFullWidth = NavBarContentHeight
internal val MaxNavigationBarBottomInset = 96.dp

internal fun sanitizeNavigationBarBottomInset(systemNavBarInset: Dp): Dp {
    if (!systemNavBarInset.value.isFinite()) return 0.dp
    return systemNavBarInset.coerceIn(0.dp, MaxNavigationBarBottomInset)
}

internal fun calculatePlayerSheetCollapsedTargetY(
    containerHeightPx: Float,
    collapsedContentHeightPx: Float,
    bottomMarginPx: Float,
    bottomSpacerPx: Float
): Float {
    val safeContainerHeightPx = containerHeightPx.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val safeCollapsedContentHeightPx = collapsedContentHeightPx.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val safeBottomMarginPx = bottomMarginPx.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val safeBottomSpacerPx = bottomSpacerPx.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val maxTargetY = (safeContainerHeightPx - safeCollapsedContentHeightPx).coerceAtLeast(0f)

    return (safeContainerHeightPx - safeCollapsedContentHeightPx - safeBottomMarginPx - safeBottomSpacerPx)
        .coerceIn(0f, maxTargetY)
}

internal fun resolveNavBarContentHeight(compactMode: Boolean): Dp =
    if (compactMode) NavBarCompactContentHeight else NavBarContentHeight

internal fun resolveNavBarSurfaceHeight(
    navBarStyle: String,
    systemNavBarInset: Dp,
    compactMode: Boolean
): Dp {
    val contentHeight = resolveNavBarContentHeight(compactMode)
    return if (navBarStyle == NavBarStyle.FULL_WIDTH) {
        contentHeight + systemNavBarInset
    } else {
        contentHeight
    }
}

internal fun resolveNavBarOccupiedHeight(
    systemNavBarInset: Dp,
    compactMode: Boolean
): Dp = resolveNavBarContentHeight(compactMode) + systemNavBarInset

@Composable
private fun PlayerInternalNavigationItemsRow(
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

    val scheme = MaterialTheme.colorScheme

    // ── Sheen diagonal animado — varre a cápsula como vidro reflexivo
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

    // ── Glow ambiente atrás da cápsula inteira
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

    val rowPadding = if (navBarStyle == NavBarStyle.FULL_WIDTH) {
        Modifier.padding(top = 0.dp, bottom = innerRowPadding, start = 12.dp, end = 12.dp)
    } else {
        Modifier.padding(start = 10.dp, end = 10.dp, bottom = innerRowPadding)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(rowPadding)
    ) {
        // ── Camada de vidro flutuante atrás dos itens
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    // Glow ambiente em radial por trás da cápsula
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                scheme.primary.copy(alpha = 0.22f * ambientPulse),
                                Color.Transparent,
                                scheme.tertiary.copy(alpha = 0.20f * ambientPulse)
                            )
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx())
                    )
                }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            scheme.surfaceContainerHigh.copy(alpha = 0.75f),
                            scheme.surfaceContainer.copy(alpha = 0.65f)
                        )
                    )
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                )
                .drawBehind {
                    // ── Sheen diagonal (faixa branca muito sutil que varre)
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
                },
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val scope = rememberCoroutineScope()
            var lastSearchTapTimestamp by remember { mutableStateOf(0L) }

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
                CustomNavigationBarItem(
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
}

@Composable
fun PlayerInternalNavigationBar(
    navController: NavHostController,
    navItems: ImmutableList<BottomNavItem>,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    navBarStyle: String,
    compactMode: Boolean,
    bottomBarPadding: Dp = 0.dp,
    onSearchIconDoubleTap: () -> Unit = {}
) {
    PlayerInternalNavigationItemsRow(
        navController = navController,
        navItems = navItems,
        currentRoute = currentRoute,
        navBarStyle = navBarStyle,
        compactMode = compactMode,
        bottomBarPadding = bottomBarPadding,
        onSearchIconDoubleTap = onSearchIconDoubleTap,
        modifier = modifier
    )
}
