package com.goldensystem.auris.presentation.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.goldensystem.auris.R
import com.goldensystem.auris.presentation.components.CollapsibleCommonTopBar
import com.goldensystem.auris.presentation.components.MiniPlayerHeight
import com.goldensystem.auris.presentation.components.SmartImage
import com.goldensystem.auris.presentation.navigation.Screen
import com.goldensystem.auris.presentation.navigation.navigateSafely
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel
import com.goldensystem.auris.presentation.viewmodel.PlayerViewModel
import com.goldensystem.auris.ui.theme.WallpaperBackground
import kotlinx.coroutines.launch
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import kotlin.math.roundToInt

// ============================================================
// MODELOS
// ============================================================

private data class Contributor(
    val id: String,
    val displayName: String,
    val role: String,
    val detail: String? = null,
    val badge: String? = null,
    val avatarUrl: String? = null,
    val iconRes: Int? = null,
    val instagramUrl: String? = null,
    val tiktokUrl: String? = null,
    val githubUrl: String? = null,
    val telegramUrl: String? = null,
)

// ============================================================
// TELA PRINCIPAL
// ============================================================

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun AboutScreen(
    navController: NavController,
    viewModel: PlayerViewModel,
    onNavigationIconClick: () -> Unit,
) {
    val context = LocalContext.current
    val customThemeViewModel: CustomThemeViewModel = hiltViewModel()
    val config by customThemeViewModel.customThemeConfig.collectAsStateWithLifecycle()

    val versionName = remember(context) {
        try {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
                ?: "N/A"
        } catch (_: Exception) {
            "N/A"
        }
    }

    val officialWebsite =
        "https://synvertexstudios.github.io/Auris-website/data/home.html"

    val youtubeUrl =
        "https://www.youtube.com/@AurisMusicPlayer"

    val instagramUrl =
        "https://www.instagram.com/aurismp"

    val tiktokUrl =
        "https://www.tiktok.com/@auris_music_player"

    // --------------------------------------------------------
    // MANTENEDORES
    // --------------------------------------------------------

    val goldenSystem = Contributor(
        id = "goldensystem",
        displayName = "Golden System",
        role = stringResource(R.string.contributor_golden_role),
        detail = stringResource(R.string.contributor_golden_detail),
        avatarUrl =
            "https://raw.githubusercontent.com/synvertexstudios/Auris/main/app/src/main/res/drawable/goldensystem_icon.png",
        iconRes = R.drawable.ic_music_placeholder,
        instagramUrl = "https://www.instagram.com/goldensystem.enterprise",
        tiktokUrl = "https://www.tiktok.com/@goldensystem.enterprise",
    )

    val aurisMaintainer = Contributor(
        id = "synvertexstudios",
        displayName = stringResource(R.string.contributor_auris_display_name),
        role = stringResource(R.string.contributor_auris_role),
        detail = stringResource(R.string.contributor_auris_detail),
        avatarUrl =
            "https://raw.githubusercontent.com/synvertexstudios/Auris/refs/heads/main/app/src/main/res/drawable/ic_guarafox_ft.png",
        iconRes = R.drawable.ic_music_placeholder,
        githubUrl = "https://github.com/synvertexstudios",
    )
    
    val aurisMaintainer2 = Contributor(
        id = "synvertexstudiosr",
        displayName = stringResource(R.string.contributor_regiane_display_name),
        role = stringResource(R.string.contributor_regiane_role),
        detail = stringResource(R.string.contributor_regiane_detail),
        avatarUrl =
            "https://raw.githubusercontent.com/synvertexstudios/Auris/assets/avatar/avatar24.png",
        iconRes = R.drawable.ic_music_placeholder,
        githubUrl = "https://github.com/synvertexstudios",
    )

    // --------------------------------------------------------
    // ANIMAÇÃO DE ENTRADA
    // --------------------------------------------------------

    val transitionState = remember {
        MutableTransitionState(false)
    }

    LaunchedEffect(Unit) {
        transitionState.targetState = true
    }

    val transition = rememberTransition(
        transitionState,
        label = "AboutScreenTransition",
    )

    val contentAlpha by transition.animateFloat(
        label = "AboutContentAlpha",
        transitionSpec = {
            tween(
                durationMillis = 450,
                easing = FastOutSlowInEasing,
            )
        },
    ) {
        if (it) 1f else 0f
    }

    val contentOffset by transition.animateDp(
        label = "AboutContentOffset",
        transitionSpec = {
            tween(
                durationMillis = 450,
                easing = FastOutSlowInEasing,
            )
        },
    ) {
        if (it) 0.dp else 28.dp
    }

    // --------------------------------------------------------
    // COLLAPSING TOP BAR
    // --------------------------------------------------------

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    val minTopBarHeight = 64.dp + statusBarHeight
    val maxTopBarHeight = 170.dp

    val minTopBarHeightPx = with(density) {
        minTopBarHeight.toPx()
    }

    val maxTopBarHeightPx = with(density) {
        maxTopBarHeight.toPx()
    }

    val topBarHeight = remember {
        androidx.compose.animation.core.Animatable(
            maxTopBarHeightPx,
        )
    }

    var collapseFraction by remember {
        mutableStateOf(0f)
    }

    LaunchedEffect(topBarHeight.value) {
        collapseFraction =
            1f - (
                (topBarHeight.value - minTopBarHeightPx) /
                    (maxTopBarHeightPx - minTopBarHeightPx)
                ).coerceIn(0f, 1f)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val delta = available.y

                val scrollingUp = delta < 0

                if (
                    !scrollingUp &&
                    (
                        listState.firstVisibleItemIndex > 0 ||
                            listState.firstVisibleItemScrollOffset > 0
                        )
                ) {
                    return Offset.Zero
                }

                val previousHeight = topBarHeight.value

                val newHeight = (
                    previousHeight + delta
                    ).coerceIn(
                    minTopBarHeightPx,
                    maxTopBarHeightPx,
                )

                val consumed = newHeight - previousHeight

                if (consumed.roundToInt() != 0) {
                    coroutineScope.launch {
                        topBarHeight.snapTo(newHeight)
                    }
                }

                val shouldConsume =
                    !(scrollingUp && newHeight == minTopBarHeightPx)

                return if (shouldConsume) {
                    Offset(0f, consumed)
                } else {
                    Offset.Zero
                }
            }
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val middle =
                (minTopBarHeightPx + maxTopBarHeightPx) / 2f

            val shouldExpand =
                topBarHeight.value > middle

            val canExpand =
                listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0

            val target =
                if (shouldExpand && canExpand) {
                    maxTopBarHeightPx
                } else {
                    minTopBarHeightPx
                }

            if (topBarHeight.value != target) {
                coroutineScope.launch {
                    topBarHeight.animateTo(
                        target,
                        spring(
                            stiffness = Spring.StiffnessMedium,
                            dampingRatio = 0.88f,
                        ),
                    )
                }
            }
        }
    }

    // ========================================================
    // UI
    // ========================================================

    WallpaperBackground(
        modifier = Modifier.fillMaxSize(),
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = contentOffset.toPx()
                },
        ) {

            val currentTopBarHeight =
                with(density) {
                    topBarHeight.value.toDp()
                }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(
                    top = currentTopBarHeight + 10.dp,
                    bottom =
                        MiniPlayerHeight +
                            WindowInsets.navigationBars
                                .asPaddingValues()
                                .calculateBottomPadding() +
                            24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {

                // ====================================================
                // HERO
                // ====================================================

                item(key = "hero") {
                    AboutHeroCard(
                        versionName = versionName,
                        onVersionLongPress = {
                            navController.navigateSafely(
                                Screen.EasterEgg.route,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 6.dp),
                    )
                }

                // ====================================================
                // CHANGLEOG
                // ====================================================

                item(key = "changelog") {
                    ExpandableSection(
                        title = stringResource(
                            R.string.about_changelog_title,
                        ),
                        icon = Icons.Rounded.Update,
                        iconTint = MaterialTheme.colorScheme.primary,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.about_changelog_text,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 21.sp,
                        )
                    }
                }

                // ====================================================
                // SITE
                // ====================================================

                item(key = "website") {
                    AboutActionCard(
                        icon = Icons.Rounded.Language,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = stringResource(
                            R.string.about_official_website_title,
                        ),
                        subtitle = stringResource(
                            R.string.about_official_website_subtitle,
                        ),
                        buttonText = stringResource(
                            R.string.about_official_website_button,
                        ),
                        onClick = {
                            launchUrl(
                                context,
                                officialWebsite,
                            )
                        },
                    )
                }

                // ====================================================
                // YOUTUBE
                // ====================================================

                item(key = "youtube") {
                    AboutActionCard(
                        iconRes = R.drawable.ic_youtube,
                        iconTint = Color.Red,
                        title = stringResource(
                            R.string.about_youtube_title,
                        ),
                        subtitle = stringResource(
                            R.string.about_youtube_subtitle,
                        ),
                        buttonText = stringResource(
                            R.string.about_youtube_button,
                        ),
                        onClick = {
                            launchUrl(
                                context,
                                youtubeUrl,
                            )
                        },
                    )
                }

                // ====================================================
                // REDES SOCIAIS
                // ====================================================

                item(key = "social") {
                    ExpandableSection(
                        title = stringResource(
                            R.string.about_social_media_title,
                        ),
                        icon = Icons.Rounded.Share,
                        iconTint = MaterialTheme.colorScheme.secondary,
                    ) {

                        SocialButton(
                            iconRes = R.drawable.ic_instagram,
                            label = stringResource(
                                R.string.about_instagram,
                            ),
                            onClick = {
                                launchUrl(
                                    context,
                                    instagramUrl,
                                )
                            },
                        )

                        SocialButton(
                            iconRes = R.drawable.ic_tiktok,
                            label = stringResource(
                                R.string.about_tiktok,
                            ),
                            onClick = {
                                launchUrl(
                                    context,
                                    tiktokUrl,
                                )
                            },
                        )
                    }
                }

                // ====================================================
                // FEEDBACK
                // ====================================================

                item(key = "feedback") {
                    FeedbackCard(
                        onClick = {
                            navController.navigateSafely(
                                Screen.Support.route,
                            )
                        },
                    )
                }

                // ====================================================
                // MANTENEDORES HEADER
                // ====================================================

                item(key = "maintainers_header") {
                    SectionHeader(
                        title = stringResource(
                            R.string.about_maintainer_title,
                        ),
                        subtitle = stringResource(
                            R.string.about_maintainer_subtitle,
                        ),
                        modifier = Modifier.padding(
                            top = 18.dp,
                            bottom = 2.dp,
                        ),
                    )
                }

                // ====================================================
                // MANTENEDOR 1
                // ====================================================

                item(key = "golden_system") {
                    ContributorCard(
                        contributor = goldenSystem,
                        shape = expressiveListShape(
                            index = 0,
                            count = 2,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }

                // ====================================================
                // MANTENEDOR 2
                // ====================================================

                item(key = "auris_maintainer") {
                    ContributorCard(
                        contributor = aurisMaintainer,
                        shape = expressiveListShape(
                            index = 1,
                            count = 2,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }
                
                                // ====================================================
                // MANTENEDOR 3
                // ====================================================
                
                item(key = "auris_maintainer2") {
                    ContributorCard(
                        contributor = aurisMaintainer2,
                        shape = expressiveListShape(
                            index = 1,
                            count = 2,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }

                // ====================================================
                // COPYRIGHT
                // ====================================================

                item(key = "copyright") {
                    CopyrightSection()
                }

                item(key = "bottom") {
                    Spacer(
                        modifier = Modifier.height(12.dp),
                    )
                }
            }

            // ========================================================
            // TOP BAR
            // ========================================================

            CollapsibleCommonTopBar(
                title = stringResource(
                    R.string.screen_about,
                ),
                collapseFraction = collapseFraction,
                headerHeight = currentTopBarHeight,
                onBackClick = onNavigationIconClick,
                expandedTitleStartPadding = 20.dp,
                collapsedTitleStartPadding = 68.dp,
                containerColor =
                    if (config.isEnabled) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            )
        }
    }
}

// ============================================================
// HERO CARD
// ============================================================

@Composable
private fun AboutHeroCard(
    versionName: String,
    onVersionLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    val shape = AbsoluteSmoothCornerShape(
        30.dp,
        60,
    )

    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 3.dp,
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.20f,
                            ),
                            MaterialTheme.colorScheme.tertiary.copy(
                                alpha = 0.12f,
                            ),
                            MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    ),
                ),
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp,
                    ),
            ) {

                // ----------------------------------------------------
                // APP HEADER
                // ----------------------------------------------------

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Surface(
                        modifier = Modifier.size(58.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp,
                    ) {

                        Box(
                            contentAlignment = Alignment.Center,
                        ) {

                            Icon(
                                painter = painterResource(
                                    R.drawable.auris_base_monochrome,
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.width(14.dp),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                    ) {

                        Text(
                            text = stringResource(
                                R.string.about_app_name,
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp),
                        )

                        Text(
                            text = stringResource(
                                R.string.about_tagline,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(18.dp),
                )

                // ----------------------------------------------------
                // VERSION
                // ----------------------------------------------------

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.tertiaryContainer,
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.LongPress,
                                    )
                                    onVersionLongPress()
                                },
                            )
                        },
                ) {

                    Row(
                        modifier = Modifier.padding(
                            horizontal = 13.dp,
                            vertical = 8.dp,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {

                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(16.dp),
                        )

                        Text(
                            text = stringResource(
                                R.string.about_version_format,
                                versionName,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(18.dp),
                )

                CommunitySignalsRow()
            }
        }
    }
}

// ============================================================
// COMMUNITY SIGNALS
// ============================================================

@Composable
private fun CommunitySignalsRow() {

    val items = listOf(
        stringResource(
            R.string.about_signal_community_first,
        ) to Icons.Rounded.Public,

        stringResource(
            R.string.about_signal_material3,
        ) to Icons.Rounded.Palette,

        stringResource(
            R.string.about_signal_update,
        ) to Icons.Rounded.Update,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {

        items.forEach { (label, icon) ->

            Surface(
                modifier = Modifier.weight(1f),
                shape = AbsoluteSmoothCornerShape(
                    16.dp,
                    60,
                ),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                    alpha = 0.88f,
                ),
            ) {

                Column(
                    modifier = Modifier.padding(
                        horizontal = 6.dp,
                        vertical = 9.dp,
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp),
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp),
                    )

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ============================================================
// SECTION HEADER
// ============================================================

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        if (subtitle.isNotBlank()) {

            Spacer(
                modifier = Modifier.height(3.dp),
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ============================================================
// EXPANDABLE SECTION
// ============================================================

@Composable
private fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable {
        mutableStateOf(initiallyExpanded)
    }

    val shape = AbsoluteSmoothCornerShape(
        22.dp,
        60,
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
        onClick = {
            expanded = !expanded
        },
    ) {

        Column(
            modifier = Modifier.padding(15.dp),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.13f),
                ) {

                    Box(
                        contentAlignment = Alignment.Center,
                    ) {

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(12.dp),
                )

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Icon(
                    imageVector =
                        if (expanded) {
                            Icons.Rounded.ExpandLess
                        } else {
                            Icons.Rounded.ExpandMore
                        },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter =
                    fadeIn(
                        animationSpec = tween(180),
                    ) +
                        expandVertically(
                            animationSpec = tween(250),
                        ),
                exit =
                    fadeOut(
                        animationSpec = tween(120),
                    ) +
                        shrinkVertically(
                            animationSpec = tween(200),
                        ),
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 14.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    content()
                }
            }
        }
    }
}

// ============================================================
// ACTION CARD
// ============================================================

@Composable
private fun AboutActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    iconTint: Color,
    title: String,
    subtitle: String?,
    buttonText: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = AbsoluteSmoothCornerShape(
            22.dp,
            60,
        ),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.13f),
                ) {

                    Box(
                        contentAlignment = Alignment.Center,
                    ) {

                        when {
                            icon != null -> {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(23.dp),
                                )
                            }

                            iconRes != null -> {
                                Icon(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(23.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.width(12.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                ) {

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (!subtitle.isNullOrBlank()) {

                        Spacer(
                            modifier = Modifier.height(2.dp),
                        )

                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(13.dp),
            )

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = AbsoluteSmoothCornerShape(
                    15.dp,
                    60,
                ),
                contentPadding = PaddingValues(
                    vertical = 11.dp,
                ),
            ) {

                Text(
                    text = buttonText,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(
                    modifier = Modifier.width(5.dp),
                )

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ============================================================
// FEEDBACK CARD
// ============================================================

@Composable
private fun FeedbackCard(
    onClick: () -> Unit,
) {
    val error = MaterialTheme.colorScheme.error

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = AbsoluteSmoothCornerShape(
            22.dp,
            60,
        ),
        color = error.copy(alpha = 0.08f),
        border = BorderStroke(
            1.dp,
            error.copy(alpha = 0.20f),
        ),
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = error.copy(alpha = 0.12f),
                ) {

                    Box(
                        contentAlignment = Alignment.Center,
                    ) {

                        Icon(
                            imageVector = Icons.Rounded.BugReport,
                            contentDescription = null,
                            tint = error,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(12.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                ) {

                    Text(
                        text = stringResource(
                            R.string.about_feedback_title,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp),
                    )

                    Text(
                        text = stringResource(
                            R.string.about_feedback_subtitle,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(13.dp),
            )

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = AbsoluteSmoothCornerShape(
                    15.dp,
                    60,
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {

                Icon(
                    imageVector = Icons.Rounded.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(
                    modifier = Modifier.width(7.dp),
                )

                Text(
                    text = stringResource(
                        R.string.about_feedback_button,
                    ),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ============================================================
// SOCIAL BUTTON
// ============================================================

@Composable
private fun SocialButton(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AbsoluteSmoothCornerShape(
            15.dp,
            60,
        ),
        contentPadding = PaddingValues(
            vertical = 10.dp,
        ),
    ) {

        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(19.dp),
        )

        Spacer(
            modifier = Modifier.width(8.dp),
        )

        Text(
            text = label,
            fontWeight = FontWeight.Medium,
        )

        Spacer(
            modifier = Modifier.weight(1f),
        )

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ============================================================
// CONTRIBUTOR CARD
// ============================================================

@Composable
private fun ContributorCard(
    contributor: Contributor,
    shape: AbsoluteSmoothCornerShape,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null,
) {
    val clickableModifier =
        if (onCardClick != null) {

            Modifier.clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onCardClick,
            )

        } else {
            Modifier
        }

    Surface(
        modifier = modifier
            .clip(shape)
            .then(clickableModifier),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 13.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            ContributorAvatar(
                name = contributor.displayName,
                avatarUrl = contributor.avatarUrl,
                iconRes = contributor.iconRes,
            )

            Spacer(
                modifier = Modifier.width(12.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 5.dp),
            ) {

                Text(
                    text = contributor.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = contributor.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                contributor.detail
                    ?.takeIf { it.isNotBlank() }
                    ?.let { detail ->

                        Spacer(
                            modifier = Modifier.height(3.dp),
                        )

                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                contributor.badge?.let { badge ->

                    Spacer(
                        modifier = Modifier.height(6.dp),
                    )

                    ContributorLabel(
                        text = badge,
                    )
                }
            }

            ContributorSocials(
                contributor = contributor,
            )
        }
    }
}

// ============================================================
// CONTRIBUTOR SOCIALS
// ============================================================

@Composable
private fun ContributorSocials(
    contributor: Contributor,
) {
    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {

        if (contributor.id == "goldensystem") {

            ContributorSocialIcon(
                painterRes = R.drawable.ic_instagram,
                contentDescription = stringResource(
                    R.string.cd_open_instagram,
                ),
                onClick = {
                    contributor.instagramUrl?.let {
                        launchUrl(context, it)
                    }
                },
            )

            ContributorSocialIcon(
                painterRes = R.drawable.ic_tiktok,
                contentDescription = stringResource(
                    R.string.cd_open_tiktok,
                ),
                onClick = {
                    contributor.tiktokUrl?.let {
                        launchUrl(context, it)
                    }
                },
            )
        }

        if (contributor.id == "synvertexstudios") {
            // Sem redes sociais visíveis.
        }
    }
}

// ============================================================
// SOCIAL ICON
// ============================================================

@Composable
private fun ContributorSocialIcon(
    @DrawableRes painterRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(38.dp),
    ) {

        Icon(
            painter = painterResource(painterRes),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(21.dp),
        )
    }
}

// ============================================================
// AVATAR
// ============================================================

@Composable
private fun ContributorAvatar(
    name: String,
    avatarUrl: String?,
    @DrawableRes iconRes: Int?,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        MaterialTheme.colorScheme.surfaceContainerHigh

    val iconTint =
        MaterialTheme.colorScheme.onSurfaceVariant

    val fallbackBackground =
        MaterialTheme.colorScheme.primaryContainer

    val fallbackTint =
        MaterialTheme.colorScheme.onPrimaryContainer

    val initial =
        name
            .removePrefix("@")
            .firstOrNull()
            ?.uppercase()
            ?: "?"

    var cachedBitmap by remember(avatarUrl) {
        mutableStateOf<ImageBitmap?>(null)
    }

    Surface(
        modifier = modifier.size(52.dp),
        shape = CircleShape,
        color = containerColor,
        tonalElevation = 2.dp,
    ) {

        when {

            cachedBitmap != null -> {

                Image(
                    bitmap = cachedBitmap!!,
                    contentDescription = stringResource(
                        R.string.cd_contributor_avatar,
                        name,
                    ),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            !avatarUrl.isNullOrBlank() -> {

                SmartImage(
                    model = ImageRequest.Builder(
                        LocalContext.current,
                    )
                        .data(avatarUrl)
                        .crossfade(true)
                        .size(96)
                        .build(),

                    contentDescription = stringResource(
                        R.string.cd_contributor_avatar,
                        name,
                    ),

                    modifier = Modifier.fillMaxSize(),

                    shape = CircleShape,

                    contentScale = ContentScale.Crop,

                    placeholderResId =
                        iconRes
                            ?: R.drawable.ic_music_placeholder,

                    errorResId =
                        R.drawable.rounded_broken_image_24,

                    targetSize = Size(96, 96),

                    onState = { state ->

                        if (
                            state is AsyncImagePainter.State.Success
                        ) {

                            val bitmap =
                                state.result.drawable
                                    ?.toBitmap()
                                    ?.asImageBitmap()

                            if (bitmap != null) {
                                cachedBitmap = bitmap
                            }
                        }
                    },
                )
            }

            iconRes != null -> {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            fallbackBackground,
                        ),
                    contentAlignment = Alignment.Center,
                ) {

                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = stringResource(
                            R.string.cd_contributor_icon,
                            name,
                        ),
                        tint = iconTint,
                        modifier = Modifier.size(27.dp),
                    )
                }
            }

            else -> {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            fallbackBackground,
                        ),
                    contentAlignment = Alignment.Center,
                ) {

                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = fallbackTint,
                    )
                }
            }
        }
    }
}

// ============================================================
// LABEL
// ============================================================

@Composable
private fun ContributorLabel(
    text: String,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {

        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 4.dp,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ============================================================
// COPYRIGHT
// ============================================================

@Composable
private fun CopyrightSection() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 32.dp,
                vertical = 24.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = 0.55f,
            ),
        )

        Spacer(
            modifier = Modifier.height(18.dp),
        )

        Text(
            text = "Auris Music Player",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(
            modifier = Modifier.height(7.dp),
        )

        Text(
            text = "Copyright (c) 2024 theovilardo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.65f,
            ),
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Copyright (c) 2026 Saymon Silva Pereira",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.65f,
            ),
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Copyright (c) 2026 Golden System Studios",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.65f,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(
            modifier = Modifier.height(8.dp),
        )

        Text(
            text = "Made with Android & Jetpack Compose",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.5f,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

// ============================================================
// FORMATO DAS CARDS DE MANTENEDORES
// ============================================================

private fun expressiveListShape(
    index: Int,
    count: Int,
): AbsoluteSmoothCornerShape {

    val outer = 22.dp
    val inner = 7.dp

    return when {

        count <= 1 ->
            AbsoluteSmoothCornerShape(
                outer,
                60,
            )

        index == 0 ->
            AbsoluteSmoothCornerShape(
                cornerRadiusTL = outer,
                cornerRadiusTR = outer,
                cornerRadiusBL = inner,
                cornerRadiusBR = inner,
                smoothnessAsPercentTL = 60,
                smoothnessAsPercentTR = 60,
                smoothnessAsPercentBL = 60,
                smoothnessAsPercentBR = 60,
            )

        index == count - 1 ->
            AbsoluteSmoothCornerShape(
                cornerRadiusTL = inner,
                cornerRadiusTR = inner,
                cornerRadiusBL = outer,
                cornerRadiusBR = outer,
                smoothnessAsPercentTL = 60,
                smoothnessAsPercentTR = 60,
                smoothnessAsPercentBL = 60,
                smoothnessAsPercentBR = 60,
            )

        else ->
            AbsoluteSmoothCornerShape(
                inner,
                60,
            )
    }
}

// ============================================================
// URL
// ============================================================

private fun launchUrl(
    context: Context,
    url: String,
) {
    val uri = try {
        url.toUri()
    } catch (_: Throwable) {
        return
    }

    val intent = Intent(
        Intent.ACTION_VIEW,
        uri,
    ).apply {
        addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK,
        )
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // Nenhum aplicativo capaz de abrir a URL.
    }
}