@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)

package com.goldensystem.auris.presentation.telegram.chat

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Send
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
import kotlin.math.absoluteValue
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

// =============================================================================
// ENTRY POINT
// =============================================================================

@Composable
fun TelegramChatScreen(
    chatId: Long,
    onBack: () -> Unit,
    onPlayAudio: (com.goldensystem.auris.data.model.Song) -> Unit,
    viewModel: TelegramChatViewModel = hiltViewModel()
) {
    LaunchedEffect(chatId) {
        viewModel.initialize(chatId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    /*
     * Só faz scroll automático quando chegam mensagens novas.
     * Mantém a experiência natural quando o usuário está lendo mensagens antigas.
     */
    LaunchedEffect(uiState.items.size) {
        if (uiState.items.isNotEmpty()) {
            val lastIndex = uiState.items.lastIndex

            if (!listState.canScrollForward || listState.firstVisibleItemIndex >= lastIndex - 2) {
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    val showScrollButton =
        uiState.items.isNotEmpty() &&
            listState.canScrollBackward &&
            listState.firstVisibleItemIndex < uiState.items.lastIndex - 2

    val background = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.055f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = Color.Transparent,
        topBar = {
            ChatTopBar(
                title = uiState.chatTitle,
                photoPath = uiState.chatPhotoPath,
                onBack = onBack
            )
        },
        bottomBar = {
            ChatInputBar(
                text = uiState.inputText,
                isSending = uiState.isSending,
                onTextChange = viewModel::onInputChanged,
                onSend = viewModel::sendMessage
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 88.dp)
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(innerPadding)
        ) {

            when {
                uiState.isLoading && uiState.items.isEmpty() -> {
                    LoadingChatState()
                }

                uiState.items.isEmpty() -> {
                    EmptyChatState()
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
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

            // =========================================================================
            // SCROLL TO BOTTOM
            // =========================================================================

            AnimatedVisibility(
                visible = showScrollButton,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 16.dp,
                        bottom = 92.dp
                    ),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FilledTonalIconButton(
                    onClick = {
    if (uiState.items.isNotEmpty()) {
        coroutineScope.launch {
            listState.animateScrollToItem(uiState.items.lastIndex)
        }
    }
},
                    modifier = Modifier.size(46.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowDownward,
                        contentDescription = "Ir para o final"
                    )
                }
            }
        }
    }
}

// =============================================================================
// TOP BAR
// =============================================================================

@Composable
private fun ChatTopBar(
    title: String,
    photoPath: String?,
    onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    ChatAvatar(
                        title = title,
                        photoPath = photoPath,
                        size = 44.dp
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title.ifBlank { "Chat" },
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = GoogleSansRounded,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.primary
                                    )
                            )

                            Spacer(Modifier.width(5.dp))

                            Text(
                                text = "Sincronizado com Telegram",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = GoogleSansRounded,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                FilledIconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(start = 8.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

// =============================================================================
// AVATAR
// =============================================================================

@Composable
private fun ChatAvatar(
    title: String,
    photoPath: String?,
    size: androidx.compose.ui.unit.Dp
) {
    val avatarShape = CircleShape

    Box(
        modifier = Modifier
            .size(size)
            .clip(avatarShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            ),
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
                text = title
                    .trim()
                    .take(1)
                    .uppercase()
                    .ifBlank { "T" },
                style = MaterialTheme.typography.titleMedium,
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// =============================================================================
// LOADING STATE
// =============================================================================

@Composable
private fun LoadingChatState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LoadingIndicator(
                modifier = Modifier.size(44.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Carregando conversa",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = GoogleSansRounded,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================================
// EMPTY STATE
// =============================================================================

@Composable
private fun EmptyChatState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Conversa vazia",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Envie uma música, artista ou link para começar a usar este chat.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = GoogleSansRounded,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =============================================================================
// CHAT ITEM DISPATCHER
// =============================================================================

@Composable
private fun ChatItemRow(
    item: ChatItem,
    onDownloadAudio: (Int) -> Unit,
    onPlayAudio: (com.goldensystem.auris.data.model.Song) -> Unit,
    onInlineButtonClick: (ChatItem.InlineButton, Long) -> Unit
) {
    when (item) {
        is ChatItem.TextMessage ->
            TextMessageBubble(item)

        is ChatItem.BotMessage ->
            BotMessageBubble(
                item = item,
                onInlineButtonClick = onInlineButtonClick
            )

        is ChatItem.AudioMessage ->
            AudioMessageBubble(
                item = item,
                onDownloadAudio = onDownloadAudio,
                onPlayAudio = onPlayAudio
            )

        is ChatItem.DocumentMessage ->
            DocumentMessageBubble(item)

        is ChatItem.UnsupportedMessage ->
            UnsupportedMessageBubble(item)
    }
}

// =============================================================================
// TEXT MESSAGE
// =============================================================================

@Composable
private fun TextMessageBubble(
    item: ChatItem.TextMessage
) {
    MessageBubbleShell(
        isOutgoing = item.isOutgoing,
        timestamp = item.date
    ) {
        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = GoogleSansRounded,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            color = if (item.isOutgoing) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

// =============================================================================
// BOT MESSAGE
// =============================================================================

@Composable
private fun BotMessageBubble(
    item: ChatItem.BotMessage,
    onInlineButtonClick: (ChatItem.InlineButton, Long) -> Unit
) {
    MessageBubbleShell(
        isOutgoing = item.isOutgoing,
        timestamp = item.date,
        isWide = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {

            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = GoogleSansRounded,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (item.buttons.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    item.buttons.forEach { row ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            row.forEach { button ->

                                InlineButtonChip(
                                    button = button,
                                    onClick = {
                                        onInlineButtonClick(
                                            button,
                                            item.messageId
                                        )
                                    },
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

// =============================================================================
// INLINE BUTTON
// =============================================================================

@Composable
private fun InlineButtonChip(
    button: ChatItem.InlineButton,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember(button.text, button.url) {
        mutableStateOf(false)
    }

    val buttonColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.secondaryContainer,
        label = "buttonColor"
    )

    Surface(
        modifier = modifier
            .clip(
                AbsoluteSmoothCornerShape(
                    cornerRadiusTR = 14.dp,
                    cornerRadiusTL = 14.dp,
                    cornerRadiusBR = 14.dp,
                    cornerRadiusBL = 14.dp,
                    smoothnessAsPercentTR = 65,
                    smoothnessAsPercentTL = 65,
                    smoothnessAsPercentBR = 65,
                    smoothnessAsPercentBL = 65
                )
            )
            .clickable(
                enabled = !isLoading
            ) {
                if (isLoading) return@clickable

                // Ativa o efeito somente neste botão
                isLoading = true

                // Mantém o comportamento original do botão
                if (button.url != null) {
                    runCatching {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(button.url)
                            )
                        )
                    }
                } else {
                    onClick()
                }

                // Depois de 2 segundos, volta ao normal
                coroutineScope.launch {
                    kotlinx.coroutines.delay(2000)
                    isLoading = false
                }
            },
        color = buttonColor
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            // -------------------------------------------------------------
            // CONTEÚDO NORMAL
            // Fica sempre visível por baixo do shimmer
            // -------------------------------------------------------------

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 13.dp,
                        vertical = 11.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                if (button.url != null) {
                    Icon(
                        imageVector = Icons.Rounded.Link,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
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

            // -------------------------------------------------------------
            // SHIMMER
            // Fica por cima do texto, mas é transparente
            // -------------------------------------------------------------

            AnimatedVisibility(
                visible = isLoading,
                modifier = Modifier.matchParentSize(),
                enter = fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(120)
                ),
                exit = fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(180)
                )
            ) {
                ShimmerLoading()
            }
        }
    }
}


@Composable
private fun ShimmerLoading(
    modifier: Modifier = Modifier
) {
    val infiniteTransition =
        androidx.compose.animation.core.rememberInfiniteTransition(
            label = "buttonShimmer"
        )

    val shimmerPosition by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 2.5f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 850,
                easing = androidx.compose.animation.core.LinearEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "shimmerPosition"
    )

    val baseColor = MaterialTheme.colorScheme.onSecondaryContainer
        .copy(alpha = 0.055f)

    val highlightColor = MaterialTheme.colorScheme.onSecondaryContainer
        .copy(alpha = 0.22f)

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            baseColor,
            highlightColor,
            baseColor,
            Color.Transparent
        ),
        start = androidx.compose.ui.geometry.Offset(
            x = shimmerPosition * 350f,
            y = 0f
        ),
        end = androidx.compose.ui.geometry.Offset(
            x = shimmerPosition * 350f + 130f,
            y = 0f
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(shimmerBrush)
    )
}

// =============================================================================
// AUDIO MESSAGE
// =============================================================================

@Composable
private fun AudioMessageBubble(
    item: ChatItem.AudioMessage,
    onDownloadAudio: (Int) -> Unit,
    onPlayAudio: (com.goldensystem.auris.data.model.Song) -> Unit
) {
    MessageBubbleShell(
        isOutgoing = item.isOutgoing,
        timestamp = item.date,
        isWide = true
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // -----------------------------------------------------------------
                // ALBUM ART
                // -----------------------------------------------------------------

                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(
                            AbsoluteSmoothCornerShape(
                                cornerRadiusTR = 17.dp,
                                cornerRadiusTL = 17.dp,
                                cornerRadiusBR = 17.dp,
                                cornerRadiusBL = 17.dp,
                                smoothnessAsPercentTR = 70,
                                smoothnessAsPercentTL = 70,
                                smoothnessAsPercentBR = 70,
                                smoothnessAsPercentBL = 70
                            )
                        )
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
                        shape = AbsoluteSmoothCornerShape(
                            cornerRadiusTR = 17.dp,
                            cornerRadiusTL = 17.dp,
                            cornerRadiusBR = 17.dp,
                            cornerRadiusBL = 17.dp,
                            smoothnessAsPercentTR = 70,
                            smoothnessAsPercentTL = 70,
                            smoothnessAsPercentBR = 70,
                            smoothnessAsPercentBL = 70
                        ),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // -----------------------------------------------------------------
                // INFO
                // -----------------------------------------------------------------

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = GoogleSansRounded,
                        fontWeight = FontWeight.Bold,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.75f
                            )
                        )
                    }
                }

                // -----------------------------------------------------------------
                // ACTION
                // -----------------------------------------------------------------

                AudioActionButton(
                    item = item,
                    onDownloadAudio = onDownloadAudio,
                    onPlayAudio = onPlayAudio
                )
            }

            // ---------------------------------------------------------------------
            // AUDIO STATUS
            // ---------------------------------------------------------------------

            when {
                item.isDownloading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LoadingIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Baixando áudio...",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = GoogleSansRounded,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                item.localPath != null -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Disponível offline",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = GoogleSansRounded,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// AUDIO ACTION
// =============================================================================

@Composable
private fun AudioActionButton(
    item: ChatItem.AudioMessage,
    onDownloadAudio: (Int) -> Unit,
    onPlayAudio: (com.goldensystem.auris.data.model.Song) -> Unit
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (item.isDownloading) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "audioButtonScale"
    )

    Box(
        modifier = Modifier.scale(buttonScale)
    ) {
        when {
            item.isDownloading -> {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(25.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item.localPath != null -> {
                FilledIconButton(
                    onClick = {
                        val syntheticArtistId =
                            -(item.artist.hashCode().toLong().absoluteValue)

                        val syntheticAlbumId =
                            -("Telegram Chat".hashCode().toLong().absoluteValue)

                        val song =
                            com.goldensystem.auris.data.model.Song(
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
                    modifier = Modifier.size(46.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Reproduzir"
                    )
                }
            }

            else -> {
                FilledTonalIconButton(
                    onClick = {
                        onDownloadAudio(item.fileId)
                    },
                    modifier = Modifier.size(46.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "Baixar"
                    )
                }
            }
        }
    }
}

// =============================================================================
// DOCUMENT MESSAGE
// =============================================================================

@Composable
private fun DocumentMessageBubble(
    item: ChatItem.DocumentMessage
) {
    MessageBubbleShell(
        isOutgoing = item.isOutgoing,
        timestamp = item.date
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

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

// =============================================================================
// UNSUPPORTED MESSAGE
// =============================================================================

@Composable
private fun UnsupportedMessageBubble(
    item: ChatItem.UnsupportedMessage
) {
    MessageBubbleShell(
        isOutgoing = item.isOutgoing,
        timestamp = item.date
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Error,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = item.typeName,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = GoogleSansRounded,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================================
// MESSAGE BUBBLE SHELL
// =============================================================================

@Composable
private fun MessageBubbleShell(
    isOutgoing: Boolean,
    timestamp: Int,
    isWide: Boolean = false,
    content: @Composable () -> Unit
) {
    val bubbleShape = AbsoluteSmoothCornerShape(
        cornerRadiusTR = 20.dp,
        cornerRadiusTL = 20.dp,
        cornerRadiusBR = if (isOutgoing) 5.dp else 20.dp,
        cornerRadiusBL = if (isOutgoing) 20.dp else 5.dp,
        smoothnessAsPercentTR = 65,
        smoothnessAsPercentTL = 65,
        smoothnessAsPercentBR = 65,
        smoothnessAsPercentBL = 65
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = if (isOutgoing) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {

        Surface(
            shape = bubbleShape,
            color = if (isOutgoing) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            tonalElevation = if (isOutgoing) 1.dp else 2.dp,
            modifier = Modifier.widthIn(
                min = 82.dp,
                max = if (isWide) 355.dp else 310.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(
                    start = 14.dp,
                    end = 10.dp,
                    top = 11.dp,
                    bottom = 7.dp
                )
            ) {

                content()

                Spacer(Modifier.height(5.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = formatTime(timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = GoogleSansRounded,
                        color = if (isOutgoing) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = 0.62f
                            )
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.68f
                            )
                        }
                    )

                    if (isOutgoing) {
                        Spacer(Modifier.width(4.dp))

                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Enviada",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = 0.7f
                            )
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// INPUT BAR
// =============================================================================

@Composable
private fun ChatInputBar(
    text: String,
    isSending: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val inputShape = AbsoluteSmoothCornerShape(
        cornerRadiusTR = 25.dp,
        cornerRadiusTL = 25.dp,
        cornerRadiusBR = 25.dp,
        cornerRadiusBL = 25.dp,
        smoothnessAsPercentTR = 70,
        smoothnessAsPercentTL = 70,
        smoothnessAsPercentBR = 70,
        smoothnessAsPercentBL = 70
    )

    val canSend = text.isNotBlank() && !isSending

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 4.dp,
        shadowElevation = 10.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 9.dp,
                    bottom =
                        9.dp +
                            WindowInsets.navigationBars
                                .asPaddingValues()
                                .calculateBottomPadding()
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
                        text = "Mensagem...",
                        fontFamily = GoogleSansRounded,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.72f
                        )
                    )
                },
                maxLines = 5,
                shape = inputShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor =
                        MaterialTheme.colorScheme.surfaceContainerHighest,

                    unfocusedContainerColor =
                        MaterialTheme.colorScheme.surfaceContainer,

                    disabledContainerColor =
                        MaterialTheme.colorScheme.surfaceContainer,

                    focusedBorderColor =
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),

                    unfocusedBorderColor =
                        Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (canSend) {
                            onSend()
                        }
                    }
                )
            )

            val sendScale by animateFloatAsState(
                targetValue = if (canSend) 1f else 0.92f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "sendScale"
            )

            FilledIconButton(
                onClick = onSend,
                enabled = canSend,
                modifier = Modifier
                    .size(52.dp)
                    .scale(sendScale),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor =
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                    disabledContentColor =
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.55f
                        )
                )
            ) {

                if (isSending) {
                    LoadingIndicator(
                        modifier = Modifier.size(23.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Enviar"
                    )
                }
            }
        }
    }
}

// =============================================================================
// HELPERS
// =============================================================================

private fun formatTime(unixSeconds: Int): String {
    val sdf = SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    )

    return sdf.format(
        Date(unixSeconds * 1000L)
    )
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60

    return "%d:%02d".format(
        minutes,
        remainingSeconds
    )
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) {
        return "$bytes B"
    }

    val kb = bytes / 1024.0

    if (kb < 1024) {
        return "%.1f KB".format(kb)
    }

    val mb = kb / 1024.0

    if (mb < 1024) {
        return "%.1f MB".format(mb)
    }

    val gb = mb / 1024.0

    return "%.1f GB".format(gb)
}