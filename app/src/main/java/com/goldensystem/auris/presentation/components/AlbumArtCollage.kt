package com.goldensystem.auris.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.goldensystem.auris.data.model.Song
import com.goldensystem.auris.data.preferences.CollagePattern
import kotlinx.collections.immutable.ImmutableList

@Stable
data class Config(
    val size: Dp, val width: Dp, val height: Dp, val align: Alignment,
    val rot: Float, val shape: Shape, val offsetX: Dp, val offsetY: Dp
)

@Composable
fun AlbumArtCollage(
    songs: ImmutableList<Song>,
    modifier: Modifier = Modifier,
    height: Dp = 400.dp,
    padding: Dp = 0.dp,
    pattern: CollagePattern = CollagePattern.default,
    onSongClick: (Song) -> Unit,
) {
    if (LocalUseAuroraGlassUi.current) {
        AlbumArtCollageAurora(songs, modifier, height, padding, pattern, onSongClick)
    } else {
        AlbumArtCollageLegacy(songs, modifier, height, padding, pattern, onSongClick)
    }
}
