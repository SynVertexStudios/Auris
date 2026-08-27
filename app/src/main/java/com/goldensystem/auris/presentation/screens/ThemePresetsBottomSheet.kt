package com.goldensystem.auris.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldensystem.auris.R
import com.goldensystem.auris.data.preferences.ColorPreset
import com.goldensystem.auris.data.preferences.COLOR_PRESETS
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePresetsBottomSheet(
    viewModel: CustomThemeViewModel,
    onDismiss: () -> Unit
) {
    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()
    var selectedPresetName by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                isVisible = false
                delay(300)
                viewModel.saveCustomTheme()
                onDismiss()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 16.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.custom_theme_presets_title
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = stringResource(
                                R.string.custom_theme_presets_subtitle
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                isVisible = false
                                delay(300)
                                viewModel.saveCustomTheme()
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(
                                R.string.auth_cd_close
                            ),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(COLOR_PRESETS) { preset ->

                        val isSelected =
                            selectedPresetName == preset.name

                        PresetCard(
                            preset = preset,
                            isSelected = isSelected,
                            onClick = {
                                selectedPresetName = preset.name

                                viewModel.updatePrimaryColor(
                                    preset.primaryColor
                                )
                                viewModel.updateSecondaryColor(
                                    preset.secondaryColor
                                )
                                viewModel.updateBackgroundColor(
                                    preset.backgroundColor
                                )
                                viewModel.updateOnPrimaryColor(
                                    preset.onPrimaryColor
                                )
                                viewModel.updateOnSurfaceColor(
                                    preset.onSurfaceColor
                                )
                                viewModel.updateAccentColor(
                                    preset.accentColor
                                )

                                scope.launch {
                                    isVisible = false
                                    delay(300)
                                    viewModel.saveCustomTheme()
                                    onDismiss()
                                }
                            }
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                TextButton(
                    onClick = {
                        scope.launch {
                            isVisible = false
                            delay(300)
                            viewModel.resetToDefault()
                            viewModel.saveCustomTheme()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Icon(
                        Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        stringResource(
                            R.string.custom_theme_reset_to_default
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: ColorPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        preset.primaryColor,
                        preset.secondaryColor,
                        preset.backgroundColor,
                        preset.accentColor
                    ).forEach { color ->

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(7.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = Color(color)
                        ) {}
                    }
                }
            }

            if (isSelected) {
                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(
                        R.string.presentation_batch_f_cd_selected
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}