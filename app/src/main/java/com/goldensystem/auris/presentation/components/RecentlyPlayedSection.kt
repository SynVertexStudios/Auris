package com.goldensystem.auris.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.goldensystem.auris.data.model.Song
import com.goldensystem.auris.presentation.model.RecentlyPlayedSongUiModel

internal const val RecentlyPlayedSectionMinSongsToShow = 4
private val DefaultContentPadding = PaddingValues(horizontal = 8.dp)

internal data class RecentlyPlayedPillCell(
    val item: RecentlyPlayedSongUiModel,
    val width: Dp
)

internal data class RecentlyPlayedPillRow(
    val pills: List<RecentlyPlayedPillCell>,
    val contentWidth: Dp
)

@Composable
fun RecentlyPlayedSection(
    songs: List<RecentlyPlayedSongUiModel>,
    onSongClick: (Song) -> Unit,
    onOpenAllClick: () -> Unit,
    currentSongId: String? = null,
    contentPadding: PaddingValues = DefaultContentPadding,
    modifier: Modifier = Modifier
) {
    if (LocalUseAuroraGlassUi.current) {
        RecentlyPlayedSectionAurora(songs, onSongClick, onOpenAllClick, currentSongId, contentPadding, modifier)
    } else {
        RecentlyPlayedSectionLegacy(songs, onSongClick, onOpenAllClick, currentSongId, contentPadding, modifier)
    }
}
