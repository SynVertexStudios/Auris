package com.goldensystem.auris.presentation.components

import androidx.compose.runtime.Composable

@Composable
fun HomeGradientTopBar(
    onNavigationIconClick: () -> Unit,
    onBetaClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onMenuClick: () -> Unit = {},
    onVideoGalleryClick: () -> Unit = {},
    onCustomThemeClick: () -> Unit = {},
) {
    if (LocalUseAuroraGlassUi.current) {
        HomeGradientTopBarAurora(onNavigationIconClick, onBetaClick, onTelegramClick, onMenuClick, onVideoGalleryClick, onCustomThemeClick)
    } else {
        HomeGradientTopBarLegacy(onNavigationIconClick, onBetaClick, onTelegramClick, onMenuClick, onVideoGalleryClick, onCustomThemeClick)
    }
}