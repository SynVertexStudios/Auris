package com.goldensystem.auris.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.goldensystem.auris.BottomNavItem
import com.goldensystem.auris.data.preferences.NavBarStyle
import kotlinx.collections.immutable.ImmutableList

internal val NavBarContentHeight = 90.dp
internal val NavBarCompactContentHeight = 64.dp
internal val NavBarContentHeightFullWidth = NavBarContentHeight
internal val MaxNavigationBarBottomInset = 96.dp

internal fun sanitizeNavigationBarBottomInset(systemNavBarInset: Dp): Dp {
    if (!systemNavBarInset.value.isFinite()) return 0.dp
    return systemNavBarInset.coerceIn(0.dp, MaxNavigationBarBottomInset)
}

internal fun calculatePlayerSheetCollapsedTargetY(
    containerHeightPx: Float, collapsedContentHeightPx: Float,
    bottomMarginPx: Float, bottomSpacerPx: Float
): Float {
    val h = containerHeightPx.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val c = collapsedContentHeightPx.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val m = bottomMarginPx.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val s = bottomSpacerPx.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    return (h - c - m - s).coerceIn(0f, (h - c).coerceAtLeast(0f))
}

internal fun resolveNavBarContentHeight(compactMode: Boolean): Dp =
    if (compactMode) NavBarCompactContentHeight else NavBarContentHeight

internal fun resolveNavBarSurfaceHeight(navBarStyle: String, systemNavBarInset: Dp, compactMode: Boolean): Dp {
    val ch = resolveNavBarContentHeight(compactMode)
    return if (navBarStyle == NavBarStyle.FULL_WIDTH) ch + systemNavBarInset else ch
}

internal fun resolveNavBarOccupiedHeight(systemNavBarInset: Dp, compactMode: Boolean): Dp =
    resolveNavBarContentHeight(compactMode) + systemNavBarInset

@Composable
fun PlayerInternalNavigationBar(
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
    if (LocalUseAuroraGlassUi.current) {
        PlayerInternalNavigationBarAurora(
            navController, navItems, currentRoute, modifier, navBarStyle,
            compactMode, bottomBarPadding, topCornerRadius, bottomCornerRadius, onSearchIconDoubleTap
        )
    } else {
        PlayerInternalNavigationBarLegacy(
            navController, navItems, currentRoute, modifier, navBarStyle,
            compactMode, bottomBarPadding, onSearchIconDoubleTap
        )
    }
}
