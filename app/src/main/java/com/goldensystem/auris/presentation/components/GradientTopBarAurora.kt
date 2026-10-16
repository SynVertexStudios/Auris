package com.goldensystem.auris.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goldensystem.auris.R
import com.goldensystem.auris.ui.theme.AurisStatusBarStyle
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGradientTopBarAurora(
    onNavigationIconClick: () -> Unit,
    onBetaClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onMenuClick: () -> Unit = {},
    onVideoGalleryClick: () -> Unit = {},
    onCustomThemeClick: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val surface = scheme.surface
    val surfaceHigh = scheme.surfaceContainerHigh
    val primary = scheme.primary

    AurisStatusBarStyle(color = Color.Transparent)

    val pillShape = remember {
        AbsoluteSmoothCornerShape(
            cornerRadiusTL = 34.dp,
            smoothnessAsPercentTL = 70,
            cornerRadiusTR = 34.dp,
            smoothnessAsPercentTR = 70,
            cornerRadiusBL = 34.dp,
            smoothnessAsPercentBL = 70,
            cornerRadiusBR = 34.dp,
            smoothnessAsPercentBR = 70,
        )
    }

    val glassGradient = remember(surface, surfaceHigh) {
        Brush.linearGradient(
            colors = listOf(
                surface.copy(alpha = 0.72f),
                surfaceHigh.copy(alpha = 0.55f)
            )
        )
    }

    val borderBrush = remember(primary) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.35f),
                Color.Transparent,
                primary.copy(alpha = 0.30f)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(elevation = 18.dp, shape = pillShape, clip = false)
                .clip(pillShape)
                .background(brush = glassGradient, shape = pillShape)
                .border(width = 1.dp, brush = borderBrush, shape = pillShape)
                .padding(start = 6.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledTonalButton(
                modifier = Modifier.padding(start = 4.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHigh,
                    contentColor = scheme.onSurface
                ),
                onClick = onBetaClick
            ) {
                Text(
                    text = "Auris?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlassIconAurora(onClick = onTelegramClick) {
                    Icon(
                        imageVector = Icons.Rounded.Cloud,
                        contentDescription = stringResource(R.string.presentation_batch_g_topbar_cd_telegram)
                    )
                }
                GlassIconAurora(onClick = onVideoGalleryClick) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_play_arrow_24),
                        contentDescription = "Galeria de Vídeos"
                    )
                }
                GlassIconAurora(onClick = onCustomThemeClick) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_palette_24),
                        contentDescription = stringResource(R.string.custom_theme_settings_title)
                    )
                }
                GlassIconAurora(onClick = onNavigationIconClick, emphasized = true) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_settings_24),
                        contentDescription = stringResource(R.string.settings_top_bar_title)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassIconAurora(
    onClick: () -> Unit,
    emphasized: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (emphasized) scheme.primaryContainer else scheme.surfaceContainer
    val contentColor = if (emphasized) scheme.onPrimaryContainer else scheme.onSurface

    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(42.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = container,
            contentColor = contentColor
        ),
        content = content
    )
}