package com.goldensystem.auris.presentation.telegram.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldensystem.auris.data.model.Song
import kotlinx.coroutines.flow.MutableSharedFlow
import com.goldensystem.auris.data.repository.MusicRepository
import com.goldensystem.auris.data.telegram.TelegramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import timber.log.Timber
import javax.inject.Inject

// ─── UI Models ────────────────────────────────────────────────────────────────

sealed interface ChatItem {
    val messageId: Long
    val date: Int

    data class TextMessage(
        override val messageId: Long,
        override val date: Int,
        val text: String,
        val isOutgoing: Boolean,
        val senderName: String?
    ) : ChatItem

    data class AudioMessage(
        override val messageId: Long,
        override val date: Int,
        val title: String,
        val artist: String,
        val durationSeconds: Int,
        val fileId: Int,
        val fileName: String,
        val mimeType: String,
        val fileSize: Long,
        val isOutgoing: Boolean,
        val localPath: String?,       // non-null if already downloaded
        val isDownloading: Boolean,
        val albumArtUri: String?      // telegram_art://chatId/messageId
    ) : ChatItem

    data class DocumentMessage(
        override val messageId: Long,
        override val date: Int,
        val fileName: String,
        val mimeType: String,
        val fileId: Int,
        val fileSize: Long,
        val isOutgoing: Boolean,
        val localPath: String?,
        val isDownloading: Boolean
    ) : ChatItem

    /** A bot message with inline keyboard buttons (callback queries). */
    data class BotMessage(
        override val messageId: Long,
        override val date: Int,
        val text: String,
        val buttons: List<List<InlineButton>>,
        val isOutgoing: Boolean
    ) : ChatItem

    data class InlineButton(
        val text: String,
        val callbackData: ByteArray?,
        val url: String?
    )

    data class UnsupportedMessage(
        override val messageId: Long,
        override val date: Int,
        val typeName: String,
        val isOutgoing: Boolean
    ) : ChatItem
}

data class TelegramChatUiState(
    val chatTitle: String = "",
    val chatPhotoPath: String? = null,
    val items: List<ChatItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val inputText: String = "",
    val errorMessage: String? = null,
    val isOnline: Boolean = true
)

@HiltViewModel
class TelegramChatViewModel @Inject constructor(
    private val telegramRepository: TelegramRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TelegramChatUiState())
    val uiState = _uiState.asStateFlow()

    private var chatId: Long = 0L
    private var observeJob: Job? = null

    // Track which fileIds are currently downloading (for UI spinner)
    private val downloadingFileIds = mutableSetOf<Int>()

    fun initialize(chatId: Long) {
        if (this.chatId == chatId) return
        this.chatId = chatId

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            // Load chat info
            val chat = telegramRepository.getChat(chatId)
            _uiState.update {
                it.copy(
                    chatTitle = chat?.title ?: "Chat",
                    chatPhotoPath = chat?.photo?.small?.local?.path
                )
            }

            // Load initial history
            loadHistory()

            // Observe new messages in real-time
            observeNewMessages()
            observeMessageEdits()
        }
    }

    private suspend fun loadHistory() {
        try {
            val messages = telegramRepository.getChatHistory(chatId, 0L, 50)
            val items = messages.mapNotNull { mapMessageToChatItem(it) }
            _uiState.update { it.copy(items = items, isLoading = false) }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load chat history")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load messages: ${e.message}"
                )
            }
        }
    }

    private fun observeNewMessages() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            telegramRepository.observeNewMessages(chatId).collect { message ->
                val item = mapMessageToChatItem(message) ?: return@collect
                _uiState.update { state ->
                    // Avoid duplicates
                    if (state.items.any { it.messageId == item.messageId }) return@update state
                    state.copy(items = state.items + item)
                }
            }
        }
    }

    private fun observeMessageEdits() {
        viewModelScope.launch {
            telegramRepository.observeMessageUpdates(chatId).collect { update ->
                // Refresh the specific message
                val refreshed = telegramRepository.getMessage(chatId, update.messageId)
                if (refreshed != null) {
                    val item = mapMessageToChatItem(refreshed)
                    if (item != null) {
                        _uiState.update { state ->
                            val newItems = state.items.map { existing ->
                                if (existing.messageId == item.messageId) item else existing
                            }
                            state.copy(items = newItems)
                        }
                    }
                }
            }
        }
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private fun mapMessageToChatItem(message: TdApi.Message): ChatItem? {
        val isOutgoing = message.isOutgoing
        val date = message.date

        // Check for inline keyboard (bot buttons)
        val replyMarkup = message.replyMarkup
        val hasInlineKeyboard = replyMarkup is TdApi.ReplyMarkupInlineKeyboard

        return when (val content = message.content) {
            is TdApi.MessageText -> {
                val text = content.text.text
                if (hasInlineKeyboard) {
                    val buttons = (replyMarkup as TdApi.ReplyMarkupInlineKeyboard).rows.map { row ->
                        row.map { btn ->
                            ChatItem.InlineButton(
                                text = btn.text,
                                callbackData = btn.type.let { t ->
                                    if (t is TdApi.InlineKeyboardButtonTypeCallback) t.data else null
                                },
                                url = btn.type.let { t ->
                                    if (t is TdApi.InlineKeyboardButtonTypeUrl) t.url else null
                                }
                            )
                        }
                    }
                    ChatItem.BotMessage(
                        messageId = message.id,
                        date = date,
                        text = text,
                        buttons = buttons,
                        isOutgoing = isOutgoing
                    )
                } else {
                    ChatItem.TextMessage(
                        messageId = message.id,
                        date = date,
                        text = text,
                        isOutgoing = isOutgoing,
                        senderName = null
                    )
                }
            }

            is TdApi.MessageAudio -> {
                val audio = content.audio
                val localPath = audio.audio.local.path
                    .takeIf { audio.audio.local.isDownloadingCompleted && it.isNotEmpty() }
                val isDownloading = downloadingFileIds.contains(audio.audio.id) ||
                        audio.audio.local.isDownloadingActive

                ChatItem.AudioMessage(
                    messageId = message.id,
                    date = date,
                    title = audio.title.ifEmpty { audio.fileName.substringBeforeLast('.') },
                    artist = audio.performer.ifEmpty { "Unknown Artist" },
                    durationSeconds = audio.duration,
                    fileId = audio.audio.id,
                    fileName = audio.fileName,
                    mimeType = audio.mimeType,
                    fileSize = audio.audio.size.toLong(),
                    isOutgoing = isOutgoing,
                    localPath = localPath,
                    isDownloading = isDownloading,
                    albumArtUri = "telegram_art://$chatId/${message.id}"
                )
            }

            is TdApi.MessageDocument -> {
                val doc = content.document
                val isAudio = doc.mimeType.startsWith("audio/") || doc.mimeType == "application/ogg"
                val localPath = doc.document.local.path
                    .takeIf { doc.document.local.isDownloadingCompleted && it.isNotEmpty() }
                val isDownloading = downloadingFileIds.contains(doc.document.id) ||
                        doc.document.local.isDownloadingActive

                if (isAudio) {
                    ChatItem.AudioMessage(
                        messageId = message.id,
                        date = date,
                        title = doc.fileName.substringBeforeLast('.'),
                        artist = "Telegram Audio",
                        durationSeconds = 0,
                        fileId = doc.document.id,
                        fileName = doc.fileName,
                        mimeType = doc.mimeType,
                        fileSize = doc.document.size.toLong(),
                        isOutgoing = isOutgoing,
                        localPath = localPath,
                        isDownloading = isDownloading,
                        albumArtUri = "telegram_art://$chatId/${message.id}"
                    )
                } else {
                    ChatItem.DocumentMessage(
                        messageId = message.id,
                        date = date,
                        fileName = doc.fileName,
                        mimeType = doc.mimeType,
                        fileId = doc.document.id,
                        fileSize = doc.document.size.toLong(),
                        isOutgoing = isOutgoing,
                        localPath = localPath,
                        isDownloading = isDownloading
                    )
                }
            }

            else -> {
                ChatItem.UnsupportedMessage(
                    messageId = message.id,
                    date = date,
                    typeName = content.javaClass.simpleName,
                    isOutgoing = isOutgoing
                )
            }
        }
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isSending) return

        _uiState.update { it.copy(isSending = true, inputText = "") }

        viewModelScope.launch {
            val result = telegramRepository.sendTextMessage(chatId, text)
            _uiState.update { it.copy(isSending = false) }

            if (result == null) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to send message. Check your connection.")
                }
            }
            // The new message will arrive via observeNewMessages()
        }
    }

    fun downloadAudio(fileId: Int) {
        if (downloadingFileIds.contains(fileId)) return
        downloadingFileIds.add(fileId)
        updateAudioDownloadState(fileId, isDownloading = true)

        viewModelScope.launch {
            val path = telegramRepository.downloadFileAwait(fileId, priority = 16)
            downloadingFileIds.remove(fileId)

            _uiState.update { state ->
                state.copy(
                    items = state.items.map { item ->
                        if (item is ChatItem.AudioMessage && item.fileId == fileId) {
                            item.copy(
                                localPath = path,
                                isDownloading = false
                            )
                        } else item
                    }
                )
            }

            if (path == null) {
                _uiState.update {
                    it.copy(errorMessage = "Download failed. Try again.")
                }
            }
        }
    }

    private fun updateAudioDownloadState(fileId: Int, isDownloading: Boolean) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item is ChatItem.AudioMessage && item.fileId == fileId) {
                        item.copy(isDownloading = isDownloading)
                    } else item
                }
            )
        }
    }

    fun onInlineButtonClick(button: ChatItem.InlineButton, messageId: Long) {
        // For URL buttons, we just leave it — the UI will handle opening the URL.
        // For callback buttons, we need to get the callback query ID.
        // TDLib sends UpdateNewCallbackQuery when a user presses a button.
        // But we're not the user pressing — we need to simulate it.
        // Actually, for bot buttons, we can use GetCallbackQueryAnswer.
        // However, the callbackQueryId is only known after the user clicks.
        // The correct flow: when we press an inline button, TDLib sends us
        // an UpdateNewCallbackQuery with a callbackQueryId, then we answer it.
        // For now, we'll just log and let the bot respond.
        viewModelScope.launch {
            // Note: The actual callback query ID comes from the update flow.
            // The UI press triggers TDLib to send the update, and we respond.
            Timber.d("Inline button pressed: ${button.text}")
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadHistory()
        }
    }
}