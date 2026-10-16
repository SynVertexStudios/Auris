package com.goldensystem.auris.presentation.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.goldensystem.auris.data.model.Song
import com.goldensystem.auris.presentation.viewmodel.PlayerViewModel
import com.goldensystem.auris.utils.shapes.RoundedStarShape
import kotlinx.collections.immutable.ImmutableList
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

@Composable
fun threeShapeSwitch(index: Int, thirdShapeCornerRadius: Dp = 16.dp): Shape {
    return when (index) {
        0 -> RoundedStarShape(sides = 6, rotation = 10f)
        1 -> CircleShape
        2 -> AbsoluteSmoothCornerShape(
            cornerRadiusBL = thirdShapeCornerRadius, cornerRadiusTR = thirdShapeCornerRadius,
            smoothnessAsPercentBL = 60, smoothnessAsPercentTR = 60,
            cornerRadiusTL = thirdShapeCornerRadius, cornerRadiusBR = thirdShapeCornerRadius,
            smoothnessAsPercentTL = 60, smoothnessAsPercentBR = 60
        )
        else -> CircleShape
    }
}

@Composable
fun DailyMixSection(
    songs: ImmutableList<Song>,
    playerViewModel: PlayerViewModel,
    onClickOpen: () -> Unit = {},
    onNavigateToAlbum: (Song) -> Unit = {},
    onNavigateToArtist: (Song) -> Unit = {},
    onNavigateToGenre: (Song) -> Unit = {},
) {
    if (LocalUseAuroraGlassUi.current) {
        DailyMixSectionAurora(songs, playerViewModel, onClickOpen, onNavigateToAlbum, onNavigateToArtist, onNavigateToGenre)
    } else {
        DailyMixSectionLegacy(songs, playerViewModel, onClickOpen, onNavigateToAlbum, onNavigateToArtist, onNavigateToGenre)
    }
}
