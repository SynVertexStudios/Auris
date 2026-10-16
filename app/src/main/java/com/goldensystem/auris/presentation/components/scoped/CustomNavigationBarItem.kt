package com.goldensystem.auris.presentation.components.scoped

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.goldensystem.auris.presentation.components.LocalUseAuroraGlassUi

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
    if (LocalUseAuroraGlassUi.current) {
        CustomNavigationBarItemAurora(
            selected, onClick, icon, selectedIcon, modifier, enabled, compactMode,
            label, contentDescription, alwaysShowLabel, selectedIconColor,
            unselectedIconColor, selectedTextColor, unselectedTextColor,
            indicatorColor, interactionSource
        )
    } else {
        CustomNavigationBarItemLegacy(
            selected, onClick, icon, selectedIcon, modifier, enabled, compactMode,
            label, contentDescription, alwaysShowLabel, selectedIconColor,
            unselectedIconColor, selectedTextColor, unselectedTextColor,
            indicatorColor, interactionSource
        )
    }
}
