package com.goldensystem.auris.presentation.components

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.compose.material.icons.rounded.Star
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import com.goldensystem.auris.BuildConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.goldensystem.auris.R
import com.goldensystem.auris.presentation.navigation.Screen
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

@Composable
fun BetaInfoBottomSheet(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    val context = LocalContext.current
    
    // URLs
    val feedbackUrl = remember { 
        "https://github.com/synvertexstudios/Auris/issues/new/choose" 
    }
    val officialWebsite = remember { 
        "https://synvertexstudios.github.io/Auris-website/home.html" 
    }
    val youtubeUrl = remember { 
        "https://www.youtube.com/@AurisMusicPlayer" 
    }
    val instagramUrl = remember { 
        "https://www.instagram.com/aurismp" 
    }
    val tiktokUrl = remember { 
        "https://www.tiktok.com/@auris_music_player" 
    }

    val versionName = BuildConfig.VERSION_NAME

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- HEADER: Versão ----------
            item(key = "header") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Auris v$versionName",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.about_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---------- CHANGELOG (Expansível) ----------
            item(key = "changelog") {
                ExpandableSection(
                    title = stringResource(R.string.about_changelog_title),
                    icon = Icons.Rounded.Info,
                    iconTint = MaterialTheme.colorScheme.primary,
                    initiallyExpanded = false
                ) {
                    Text(
                        text = stringResource(R.string.about_changelog_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---------- SEÇÃO: RECURSOS NOVOS (EXPANSÍVEL) ----------
            item(key = "new_features") {
    ExpandableSection(
        title = stringResource(R.string.aurissheet_newfeatures),
        icon = Icons.Rounded.Star,
        iconTint = MaterialTheme.colorScheme.tertiary,
        initiallyExpanded = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Tema Personalizado
            ActionCardSmall(
                icon = Icons.Rounded.Palette,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = stringResource(R.string.custom_theme_title),
                subtitle = stringResource(R.string.aurissheet_custom_theme_subtitle),
                buttonText = stringResource(R.string.aurissheet_open),
                onClick = { 
                    navController?.navigate(Screen.CustomThemeSettings.route) 
                }
            )
            
            // Vídeos
            ActionCardSmall(
                icon = Icons.Rounded.VideoLibrary,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.video_gallery_title),
                subtitle = stringResource(R.string.aurissheet_video_subtitle),
                buttonText = stringResource(R.string.aurissheet_open),
                onClick = { 
                    navController?.navigate(Screen.VideoGallery.route) }
                )
            }
        }
    }

            // ---------- SITE OFICIAL ----------
            item(key = "official_website") {
                ActionCard(
                    icon = Icons.Rounded.Language,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.about_official_website_title),
                    subtitle = stringResource(R.string.about_official_website_subtitle),
                    buttonText = stringResource(R.string.about_official_website_button),
                    onClick = { launchUrl(context, officialWebsite) }
                )
            }

            // ---------- YOUTUBE ----------
            item(key = "youtube") {
                ActionCard(
                    iconRes = R.drawable.ic_youtube,
                    iconTint = Color.Red,
                    title = stringResource(R.string.about_youtube_title),
                    subtitle = stringResource(R.string.about_youtube_subtitle),
                    buttonText = stringResource(R.string.about_youtube_button),
                    onClick = { launchUrl(context, youtubeUrl) }
                )
            }

            // ---------- REDES SOCIAIS ----------
            item(key = "social_media") {
                ExpandableSection(
                    title = stringResource(R.string.about_social_media_title),
                    icon = Icons.Rounded.Share,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    initiallyExpanded = false
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SocialButton(
                            iconRes = R.drawable.ic_instagram,
                            label = stringResource(R.string.about_instagram),
                            onClick = { launchUrl(context, instagramUrl) }
                        )
                        SocialButton(
                            iconRes = R.drawable.ic_tiktok,
                            label = stringResource(R.string.about_tiktok),
                            onClick = { launchUrl(context, tiktokUrl) }
                        )
                    }
                }
            }

            // ---------- FEEDBACK ----------
            item(key = "feedback") {
                ActionCard(
                    icon = Icons.Rounded.BugReport,
                    iconTint = MaterialTheme.colorScheme.error,
                    title = stringResource(R.string.about_feedback_title),
                    subtitle = stringResource(R.string.about_feedback_subtitle),
                    buttonText = stringResource(R.string.about_feedback_button),
                    buttonColors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    onClick = { 
            navController?.navigate(Screen.Support.route) }
                )
            }

            // ---------- COPYRIGHT ----------
            item(key = "copyright") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Copyright (c) 2024 theovilardo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Copyright (c) 2026 Saymon Silva Pereira",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Copyright (c) 2026 Golden System Studios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // ---------- FAB FLUTUANTE ----------
        Button(
            onClick = { launchUrl(
            navController?.navigate(Screen.Support.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .height(56.dp),
            shape = AbsoluteSmoothCornerShape(18.dp, 60),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.github),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.about_feedback_button))
        }
    }
}

// ---------- COMPONENTE: Seção Expansível ----------
@Composable
private fun ExpandableSection(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AbsoluteSmoothCornerShape(20.dp, 60),
        color = MaterialTheme.colorScheme.surfaceContainer,
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconTint.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

// ---------- COMPONENTE: Card com Ação (Grande) ----------
@Composable
private fun ActionCard(
    icon: ImageVector? = null,
    iconRes: Int? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    title: String,
    subtitle: String? = null,
    buttonText: String,
    buttonColors: androidx.compose.material3.ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AbsoluteSmoothCornerShape(20.dp, 60),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconTint.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    } else if (iconRes != null) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = AbsoluteSmoothCornerShape(14.dp, 60),
                colors = buttonColors
            ) {
                Text(buttonText)
            }
        }
    }
}

// ---------- COMPONENTE: Card com Ação (Pequeno - para dentro de seções) ----------
@Composable
private fun ActionCardSmall(
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AbsoluteSmoothCornerShape(16.dp, 60),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            OutlinedButton(
                onClick = onClick,
                shape = AbsoluteSmoothCornerShape(12.dp, 60),
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(buttonText, fontSize = MaterialTheme.typography.labelMedium.fontSize)
            }
        }
    }
}

// ---------- COMPONENTE: Botão Social ----------
@Composable
private fun SocialButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AbsoluteSmoothCornerShape(14.dp, 60),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

// ---------- UTIL: Abrir URL ----------
private fun launchUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) { }
}
