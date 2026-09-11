@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.goldensystem.auris.presentation.telegram.chat

import android.content.Intent
import android.net.Uri
import kotlin.math.absoluteValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.goldensystem.auris.R
import com.goldensystem.auris.presentation.components.SmartImage
import com.goldensystem.auris.ui.theme.GoogleSansRounded
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

// ─── Entry point ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TelegramChatScreen(
    chatId: Long,
    onBack: () -> Unit,
    onPlayAudio: (song: com.goldensystem.auris.data.model.Song) -> Unit,
    viewModel: TelegramChatViewModel = hiltViewModel()
) {
    LaunchedEffect(chatId) {
        viewModel.initialize(chatId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        MaterialTheme.colorScheme.surface
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .imePadding()
        ) {
            // ── Top bar ───────────────────────────────────────────────────────
            ChatTopBar(
                title = uiState.chatTitle,
                photoPath = uiState.chatPhotoPath,
                onBack = onBack
            )

            // ── Messages list ─────────────────────────────────────────────────
            val listState = rememberLazyListState()
            LaunchedEffect(uiState.items.size) {
                if (uiState.items.isNotEmpty()) {
                    listState.animateScrollToItem(uiState.items.size - 1)
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading && uiState.items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (uiState.items.isEmpty()) {
                    EmptyChatState()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            items = uiState.items,
                            key = { it.messageId }
                        ) { item ->
                            ChatItemRow(
                                item = item,
                                onDownloadAudio = viewModel::downloadAudio,
                                onPlayAudio = onPlayAudio,
                                onInlineButtonClick = viewModel::onInlineButtonClick
                            )
                        }
                    }
                }
            }

            // ── Input bar ─────────────────────────────────────────────────────
            ChatInputBar(
                text = uiState.inputText,
                isSending = uiState.isSending,
                onTextChange = viewModel::onInputChanged,
                onSend = viewModel::sendMessage
            )

            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

// ─── Top bar ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    title: String,
    photoPath: String?,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    if (!photoPath.isNullOrEmpty() && File(photoPath).exists()) {
                        AsyncImage(
                            model = File(photoPath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = title.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = GoogleSansRounded,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Column {
                    Text(
                        text = title.ifEmpty { "Chat" },
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = GoogleSansRounded,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Sincronizado com Telegram",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = GoogleSansRounded,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            FilledIconButton(
                onClick = onBack,
                modifier = Modifier.padding(start = 8.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyChatState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "Nenhuma mensagem ainda",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = GoogleSansRounded,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Envie o nome de uma música, artista ou link\npara começar.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = GoogleSansRounded,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ─── Chat item dispatcher ─────────────────────────────────────────────────────

@Composable
private fun ChatItemRow(
    item: ChatItem,
    onDownloadAudio: (Int) -> Unit,
    onPlayAudio: (com.goldensystem.auris.data.model.Song) -> Unit,
    onInlineButtonClick: (ChatItem.InlineButton, Long) -> Unit
) {
    when (item) {
        is ChatItem.TextMessage -> TextMessageBubble(item)
        is ChatItem.BotMessage -> BotMessageBubble(item, onInlineButtonClick)
        is ChatItem.AudioMessage -> AudioMessageBubble(item, onDownloadAudio, onPlayAudio)
        is ChatItem.DocumentMessage -> DocumentMessageBubble(item)
        is ChatItem.UnsupportedMessage -> UnsupportedMessageBubble(item)
    }
}

// ─── Message bubbles ──────────────────────────────────────────────────────────

@Composable
private fun TextMessageBubble(item: ChatItem.TextMessage) {
    MessageBubbleShell(
        isOutgoing = item.isOutgoing,
        timestamp = item.date
    ) {
        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = GoogleSansRounded,
            color = if (item.isOutgoing)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BotMessageBubble(
    item: ChatItem.BotMessage,
    onInlineButtonClick: (ChatItem.InlineButton, Long) -> Unit
) {
    MessageBubbleShell(
        isOutgoing = item.isOutgoing,
        timestamp = item.date
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = GoogleSansRounded,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (item.buttons.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.buttons.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { button ->
                                InlineButtonChip(
                                    button = button,
                                    onClick = { onInlineButtonClick(button, item.messageId) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineButtonChip(
    button: ChatItem.InlineButton,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.clickable {
            if (button.url != null) {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(button.url)))
                }
            } else {
                onClick()
            }
        },
        shape = AbsoluteSmoothCornerShape(
            cornerRadiusTR = 12.dp, cornerRadiusTL = 12.dp,
            cornerRadiusBR = 12.dp, cornerRadiusBL = 12.dp,
            smoothnessAsPercentTR = 60, smoothnessAsPercentTL = 60,
            smoothnessAsPercentBR = 60, smoothnessAsPercentBL = 60
        ),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (button.url != null) {
                Icon(
                    Icons.Rounded.Link,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = button.text,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AudioMessageBubble(
    item: ChatItem.AudioMessage,
    onDownloadAudio: (Int) -> Unit,
    onPlayAudio: (com.goldensystem.auris.data.model.Song) -> Unit
) {
    val context = LocalContext.current

    MessageBubbleShell(
        isOutgoing = item.isOutgoing,
        timestamp = item.date,
        isWide = true
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Album art
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                SmartImage(
                    model = item.albumArtUri ?: R.drawable.rounded_album_24,
                    contentDescription = item.title,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.artist,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = GoogleSansRounded,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.durationSeconds > 0) {
                    Text(
                        text = formatDuration(item.durationSeconds),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = GoogleSansRounded,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Download / play button
            when {
                item.isDownloading -> {
                    LoadingIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item.localPath != null -> {
    FilledTonalIconButton(
        onClick = {
            val syntheticArtistId = -(item.artist.hashCode().toLong().absoluteValue)
            val syntheticAlbumId = -("Telegram Chat".hashCode().toLong().absoluteValue)
            val song = com.goldensystem.auris.data.model.Song(
                id = "chat_${item.messageId}",
                title = item.title,
                artist = item.artist,
                artistId = syntheticArtistId,
                artists = emptyList(),
                album = "Telegram Chat",
                albumId = syntheticAlbumId,
                albumArtist = "Telegram",
                path = item.localPath,
                contentUriString = item.localPath,
                albumArtUriString = item.albumArtUri,
                duration = item.durationSeconds * 1000L,
                genre = null,
                lyrics = null,
                isFavorite = false,
                trackNumber = 0,
                year = 0,
                dateAdded = item.date.toLong() * 1000L,
                mimeType = item.mimeType,
                bitrate = 0,
                sampleRate = 0,
                telegramFileId = item.fileId,
                telegramChatId = 0L
            )
            onPlayAudio(song)
        },
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Icon(Icons.Rounded.PlayArrow, contentDescription = "Reproduzir")
    }
}
                else -> {
                    FilledTonalIconButton(
                        onClick = { onDownloadAudio(item.fileId) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = "Baixar")
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentMessageBubble(item: ChatItem.DocumentMessage) {
    MessageBubbleShell(
        isOutgoing = item.isOutgoing,
        timestamp = item.date
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = item.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = GoogleSansRounded,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatFileSize(item.fileSize),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = GoogleSansRounded,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UnsupportedMessageBubble(item: ChatItem.UnsupportedMessage) {
    MessageBubbleShell(
        isOutgoing = item.isOutgoing,
        timestamp = item.date
    ) {
        Text(
            text = "📎 ${item.typeName}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = GoogleSansRounded,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Bubble shell ─────────────────────────────────────────────────────────────

@Composable
private fun MessageBubbleShell(
    isOutgoing: Boolean,
    timestamp: Int,
    isWide: Boolean = false,
    content: @Composable () -> Unit
) {
    val bubbleShape = AbsoluteSmoothCornerShape(
        cornerRadiusTR = 20.dp, cornerRadiusTL = 20.dp,
        cornerRadiusBR = if (isOutgoing) 4.dp else 20.dp,
        cornerRadiusBL = if (isOutgoing) 20.dp else 4.dp,
        smoothnessAsPercentTR = 60, smoothnessAsPercentTL = 60,
        smoothnessAsPercentBR = 60, smoothnessAsPercentBL = 60
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = bubbleShape,
            color = if (isOutgoing)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 1.dp,
            modifier = Modifier
                .widthIn(
                    min = 80.dp,
                    max = if (isWide) 340.dp else 280.dp
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                content()
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatTime(timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = GoogleSansRounded,
                    color = if (isOutgoing)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// ─── Input bar ────────────────────────────────────────────────────────────────

@Composable
private fun ChatInputBar(
    text: String,
    isSending: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val inputShape = AbsoluteSmoothCornerShape(
        cornerRadiusTR = 24.dp, cornerRadiusTL = 24.dp,
        cornerRadiusBR = 24.dp, cornerRadiusBL = 24.dp,
        smoothnessAsPercentTR = 60, smoothnessAsPercentTL = 60,
        smoothnessAsPercentBR = 60, smoothnessAsPercentBL = 60
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 8.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Nome da música, artista, link...",
                        fontFamily = GoogleSansRounded,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                maxLines = 5,
                shape = inputShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )

            val canSend = text.isNotBlank() && !isSending
            FilledIconButton(
                onClick = onSend,
                enabled = canSend,
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                if (isSending) {
                    LoadingIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Enviar"
                    )
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatTime(unixSeconds: Int): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(unixSeconds * 1000L))
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    return "%.1f MB".format(mb)
}