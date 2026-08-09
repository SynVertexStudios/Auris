package com.goldensystem.auris.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goldensystem.auris.R
import com.goldensystem.auris.data.preferences.ColorPreset
import com.goldensystem.auris.data.preferences.COLOR_PRESETS
import com.goldensystem.auris.presentation.viewmodel.CustomThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePresetsBottomSheet(
    viewModel: CustomThemeViewModel,
    onDismiss: () -> Unit
) {
    val config by viewModel.customThemeConfig.collectAsStateWithLifecycle()
    var selectedPresetName by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.custom_theme_presets_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.auth_cd_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Subtitle
            Text(
                text = stringResource(R.string.custom_theme_presets_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de predefinições
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(COLOR_PRESETS) { preset ->
                    val isSelected = selectedPresetName == preset.name
                    
                    PresetCard(
                        preset = preset,
                        isSelected = isSelected,
                        onClick = {
                            selectedPresetName = preset.name
                            // Aplicar as cores
                            viewModel.updatePrimaryColor(preset.primaryColor)
                            viewModel.updateSecondaryColor(preset.secondaryColor)
                            viewModel.updateBackgroundColor(preset.backgroundColor)
                            viewModel.updateOnPrimaryColor(preset.onPrimaryColor)
                            viewModel.updateOnSurfaceColor(preset.onSurfaceColor)
                            viewModel.updateAccentColor(preset.accentColor)
                            
                            // Pequeno delay antes de fechar para feedback visual
                            kotlinx.coroutines.delay(300)
                            onDismiss()
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botão de reset
            TextButton(
                onClick = {
                    viewModel.resetToDefault()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.custom_theme_reset_to_default))
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "preset_card_scale"
    )
    
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        animationSpec = tween(durationMillis = 200),
        label = "preset_card_border"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 200),
        label = "preset_card_border_color"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .border(
                borderWidth,
                borderColor,
                RoundedCornerShape(20.dp)
            ),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = if (isSelected) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Círculo com gradiente das cores
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                Color(preset.primaryColor),
                                Color(preset.secondaryColor),
                                Color(preset.accentColor),
                                Color(preset.primaryColor)
                            )
                        )
                    )
            ) {
                // Ícone central
                Icon(
                    preset.icon,
                    contentDescription = preset.name,
                    tint = Color(preset.onPrimaryColor).copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }
            
            // Informações
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                // Mini preview das cores
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
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
                                .height(6.dp),
                            shape = RoundedCornerShape(3.dp),
                            color = Color(color)
                        ) {}
                    }
                }
            }
            
            // Indicador de seleção
            if (isSelected) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(
                        initialScale = 0.5f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                    )
                ) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = stringResource(R.string.presentation_batch_f_cd_selected),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}