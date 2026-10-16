package com.goldensystem.auris.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.goldensystem.auris.R
import com.goldensystem.auris.data.model.Song
import com.goldensystem.auris.presentation.model.RecentlyPlayedSongUiModel

private val HomeRecentlyPlayedPillHeight = 62.dp
private val HomeRecentlyPlayedPillSpacing = 10.dp
private const val HomeRecentlyPlayedPillsLimit = 10
private const val HomeRecentlyPlayedPillsPerColumn = 3
private val HomeRecentlyPlayedPillArtSize = 40.dp
private val HomeRecentlyPlayedWidthSteps = listOf(152.dp, 170.dp, 188.dp, 208.dp, 226.dp)

private data class RecentlyPlayedPillCell(val item: RecentlyPlayedSongUiModel, val width: Dp)
private data class RecentlyPlayedPillRow(val pills: List<RecentlyPlayedPillCell>, val contentWidth: Dp)

@Composable
fun RecentlyPlayedSectionAurora(
    songs: List<RecentlyPlayedSongUiModel>,
    onSongClick: (Song) -> Unit,
    onOpenAllClick: () -> Unit,
    currentSongId: String? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp),
    modifier: Modifier = Modifier
) {
    val visibleSongs = remember(songs) { songs.take(HomeRecentlyPlayedPillsLimit) }
    if (visibleSongs.size < RecentlyPlayedSectionMinSongsToShow) return

    val layoutDirection = LocalLayoutDirection.current
    val startContentPadding = remember(contentPadding, layoutDirection) {
        contentPadding.calculateLeftPadding(layoutDirection)
    }
    val endContentPadding = remember(contentPadding, layoutDirection) {
        contentPadding.calculateRightPadding(layoutDirection)
    }
    val songRows = remember(visibleSongs, startContentPadding, endContentPadding) {
        buildRecentlyPlayedPillRowsAurora(visibleSongs, startContentPadding, endContentPadding)
    }
    val maxRowContentWidth = remember(songRows) { songRows.maxOfOrNull { it.contentWidth } ?: 0.dp }
    val sharedScrollState = rememberScrollState()

    val sectionHeight = HomeRecentlyPlayedPillHeight * HomeRecentlyPlayedPillsPerColumn +
            HomeRecentlyPlayedPillSpacing * (HomeRecentlyPlayedPillsPerColumn - 1)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(start = 6.dp),
                text = stringResource(R.string.presentation_batch_g_recently_played_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            FilledIconButton(
                modifier = Modifier.height(42.dp).width(70.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                onClick = onOpenAllClick,
                enabled = visibleSongs.size >= RecentlyPlayedSectionMinSongsToShow
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = sectionHeight)
                .horizontalScroll(state = sharedScrollState)
        ) {
            Column(
                modifier = Modifier.width(maxRowContentWidth).height(sectionHeight),
                verticalArrangement = Arrangement.spacedBy(HomeRecentlyPlayedPillSpacing),
                horizontalAlignment = Alignment.Start
            ) {
                songRows.forEach { row ->
                    if (row.pills.isEmpty()) {
                        Spacer(Modifier.height(HomeRecentlyPlayedPillHeight))
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(HomeRecentlyPlayedPillHeight),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(HomeRecentlyPlayedPillSpacing)
                        ) {
                            if (startContentPadding > 0.dp) Spacer(Modifier.width(startContentPadding))
                            row.pills.forEach { cell ->
                                key(cell.item.song.id) {
                                    RecentlyPlayedPillAurora(
                                        item = cell.item,
                                        isCurrentSong = currentSongId == cell.item.song.id,
                                        modifier = Modifier.width(cell.width),
                                        onClick = { onSongClick(cell.item.song) }
                                    )
                                }
                            }
                            if (endContentPadding > 0.dp) Spacer(Modifier.width(endContentPadding))
                            val trailingGap = (maxRowContentWidth - row.contentWidth).coerceAtLeast(0.dp)
                            if (trailingGap > 0.dp) Spacer(Modifier.width(trailingGap))
                        }
                    }
                }
            }
        }
    }
}

private fun buildRecentlyPlayedPillRowsAurora(
    visibleSongs: List<RecentlyPlayedSongUiModel>,
    startContentPadding: Dp,
    endContentPadding: Dp
): List<RecentlyPlayedPillRow> {
    val rows = MutableList(HomeRecentlyPlayedPillsPerColumn) { mutableListOf<RecentlyPlayedPillCell>() }
    val rowPillsWidths = MutableList(HomeRecentlyPlayedPillsPerColumn) { 0.dp }
    val rowTargets = resolveRecentlyPlayedRowTargetsAurora(visibleSongs.size)

    var itemIndex = 0
    for (columnIndex in 0 until rowTargets[0]) {
        for (rowIndex in 0 until HomeRecentlyPlayedPillsPerColumn) {
            if (itemIndex >= visibleSongs.size) break
            if (columnIndex >= rowTargets[rowIndex]) continue
            val item = visibleSongs[itemIndex++]
            val cellWidth = resolveRecentlyPlayedPillWidthAurora(item)
            val spacingBefore = if (rows[rowIndex].isNotEmpty()) HomeRecentlyPlayedPillSpacing else 0.dp
            rows[rowIndex] += RecentlyPlayedPillCell(item, cellWidth)
            rowPillsWidths[rowIndex] = rowPillsWidths[rowIndex] + spacingBefore + cellWidth
        }
    }

    return rows.mapIndexed { rowIndex, pills ->
        RecentlyPlayedPillRow(
            pills = pills,
            contentWidth = rowPillsWidths[rowIndex] + startContentPadding + endContentPadding
        )
    }
}

private fun resolveRecentlyPlayedRowTargetsAurora(totalItems: Int): IntArray {
    val base = totalItems / HomeRecentlyPlayedPillsPerColumn
    val remainder = totalItems % HomeRecentlyPlayedPillsPerColumn
    return intArrayOf(
        base + if (remainder > 0) 1 else 0,
        base + if (remainder > 1) 1 else 0,
        base
    )
}

@Composable
private fun RecentlyPlayedPillAurora(
    item: RecentlyPlayedSongUiModel,
    isCurrentSong: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val animatedCorner by animateDpAsState(
        targetValue = if (isCurrentSong) 18.dp else (HomeRecentlyPlayedPillHeight / 2),
        animationSpec = tween(320),
        label = "pillCorner"
    )
    val animatedContainer by animateColorAsState(
        targetValue = if (isCurrentSong) scheme.primaryContainer else scheme.surfaceContainerHigh,
        animationSpec = tween(320),
        label = "pillContainer"
    )
    val titleColor by animateColorAsState(
        targetValue = if (isCurrentSong) scheme.onPrimaryContainer else scheme.onSurface,
        animationSpec = tween(320),
        label = "pillTitleColor"
    )
    val artistColor by animateColorAsState(
        targetValue = if (isCurrentSong) scheme.onPrimaryContainer.copy(alpha = 0.85f)
        else scheme.onSurfaceVariant,
        animationSpec = tween(320),
        label = "pillArtistColor"
    )

    val shape = RoundedCornerShape(animatedCorner)
    val artStartPadding = (HomeRecentlyPlayedPillHeight - HomeRecentlyPlayedPillArtSize) / 2

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = animatedContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .height(HomeRecentlyPlayedPillHeight)
            .clip(shape)
            .border(
                width = 1.dp,
                brush = if (isCurrentSong)
                    Brush.linearGradient(listOf(scheme.primary, scheme.tertiary))
                else Brush.linearGradient(
                    listOf(
                        scheme.outlineVariant.copy(alpha = 0.45f),
                        scheme.outlineVariant.copy(alpha = 0.15f)
                    )
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HomeRecentlyPlayedPillHeight)
                .padding(start = artStartPadding, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(HomeRecentlyPlayedPillArtSize)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(scheme.primary, scheme.tertiary))
                    )
                    .padding(2.dp)
                    .clip(CircleShape)
            ) {
                SmartImage(
                    model = item.song.albumArtUriString,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    targetSize = SmartImageCompactListTargetSize,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.song.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = titleColor
                )
                Text(
                    text = item.song.displayArtist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = artistColor
                )
            }
        }
    }
}

private fun resolveRecentlyPlayedPillWidthAurora(item: RecentlyPlayedSongUiModel): Dp {
    val titleLength = item.song.title.trim().length
    val artistLength = item.song.displayArtist.trim().length
    val weightedTextLength = titleLength + (artistLength * 0.55f)
    val baseStep = when {
        weightedTextLength < 18f -> 0
        weightedTextLength < 28f -> 1
        weightedTextLength < 40f -> 2
        weightedTextLength < 54f -> 3
        else -> 4
    }
    return HomeRecentlyPlayedWidthSteps[baseStep.coerceIn(0, HomeRecentlyPlayedWidthSteps.lastIndex)]
}
