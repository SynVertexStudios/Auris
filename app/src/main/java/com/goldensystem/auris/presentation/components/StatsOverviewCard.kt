package com.goldensystem.auris.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.goldensystem.auris.data.stats.PlaybackStatsRepository

@Composable
fun StatsOverviewCard(
    modifier: Modifier = Modifier,
    summary: PlaybackStatsRepository.PlaybackStatsSummary?,
    onClick: () -> Unit
) {
    if (LocalUseAuroraGlassUi.current) {
        StatsOverviewCardAurora(modifier, summary, onClick)
    } else {
        StatsOverviewCardLegacy(modifier, summary, onClick)
    }
}
