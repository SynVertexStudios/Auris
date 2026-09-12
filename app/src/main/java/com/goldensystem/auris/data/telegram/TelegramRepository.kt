package com.goldensystem.auris.data.telegram

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import com.goldensystem.auris.data.database.TelegramDao
import com.goldensystem.auris.data.database.TelegramSongEntity
import com.goldensystem.auris.data.database.TelegramTopicEntity
import com.goldensystem.auris.data.model.Song
import android.content.Context
import java.io.File
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import com.goldensystem.auris.data.preferences.PlaylistPreferencesRepository
import com.goldensystem.auris.data.telegram.TelegramCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.isActive
import org.drinkless.tdlib.TdApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

import timber.log.Timber

@Singleton
class TelegramRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clientManager: TelegramClientManager,
    private val dao: TelegramDao,
    private val playlistPreferencesRepository: PlaylistPreferencesRepository,
    private val telegramCacheManager: TelegramCacheManager
) {
    private companion object {
        private const val AUTH_REQUEST_TIMEOUT_MS = 20_000L
        private const val TELEGRAM_PLAYLIST_PREFIX = "telegram_channel:"
        private const val TELEGRAM_TOPIC_PLAYLIST_PREFIX = "telegram_topic:"
    }

    val authorizationState: Flow<TdApi.AuthorizationState?> = clientManager.authorizationState
    val authErrors: SharedFlow<TdApi.Error> = clientManager.errors
    
/**
 * Após login/relogin, o TDLib não carrega chats automaticamente.
 * Este método itera sobre os chats salvos no banco, verifica se ainda
 * existem no TDLib (pelo username) e força o carregamento.
 */
suspend fun hydrateSavedChats(
    savedChannels: List<TelegramChannelEntity>,
    onProgress: (String) -> Unit = {}
): Map<Long, Long> {
    // Retorna: oldChatId -> newChatId
    val remapping = mutableMapOf<Long, Long>()

    for (channel in savedChannels) {
        try {
            onProgress("Hydrating ${channel.title}...")

            // Estratégia 1: tentar pelo chatId antigo diretamente
            val existingChat = try {
                clientManager.sendRequest<TdApi.Chat>(TdApi.GetChat(channel.chatId))
            } catch (e: Exception) {
                null
            }

            if (existingChat != null) {
                // ChatId ainda é válido — força carregamento
                openAndLoadChat(existingChat.id)
                remapping[channel.chatId] = existingChat.id
                continue
            }

            // Estratégia 2: re-resolver pelo username (chatId mudou)
            val username = channel.username?.trim()?.removePrefix("@")
            if (!username.isNullOrBlank()) {
                val resolved = try {
                    clientManager.sendRequest<TdApi.Chat>(
                        TdApi.SearchPublicChat(username)
                    )
                } catch (e: Exception) {
                    Timber.w(e, "Could not re-resolve @$username")
                    null
                }

                if (resolved != null) {
                    Timber.d("Remapped @$username: ${channel.chatId} -> ${resolved.id}")
                    remapping[channel.chatId] = resolved.id
                    openAndLoadChat(resolved.id)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Hydration failed for chat ${channel.chatId}")
        }
    }

    return remapping
}

/**
 * Força o TDLib a "acordar" um chat: abre, carrega histórico recente
 * e (se for fórum) carrega os tópicos.
 */
private suspend fun openAndLoadChat(chatId: Long) {
    try {
        // 1. Marca como aberto — isso faz o TDLib sincronizar
        clientManager.sendRequest<TdApi.Ok>(TdApi.OpenChat(chatId))
        Timber.d("Opened chat $chatId")

        // 2. Dispara carregamento de histórico recente
        //    (o resultado não importa — só queremos forçar o TDLib a popular o cache)
        try {
            clientManager.sendRequest<TdApi.Messages>(
                TdApi.GetChatHistory(chatId, 0L, 0, 20, false)
            )
        } catch (e: Exception) {
            Timber.v("History prefetch failed (non-fatal): ${e.message}")
        }

        // 3. Espera um pouco para o TDLib processar updates assíncronos
        kotlinx.coroutines.delay(200)

        // 4. Se for fórum, "abre" também alguns tópicos para forçar carregamento
        val isForum = try { isForum(chatId) } catch (e: Exception) { false }
        if (isForum) {
            try {
                val topics = clientManager.sendRequest<TdApi.ForumTopics>(
                    TdApi.GetForumTopics().apply {
                        this.chatId = chatId
                        this.query = ""
                        this.offsetDate = 0
                        this.offsetMessageId = 0L
                        this.offsetForumTopicId = 0
                        this.limit = 100
                    }
                )
                Timber.d("Forum $chatId: ${topics.topics.size} topics found")
            } catch (e: Exception) {
                Timber.v("Forum topics prefetch failed: ${e.message}")
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "openAndLoadChat failed for $chatId")
    }
}
    // ────────────────────────────────────────────────────────────────────────
    // Artwork resolution cascade
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Resolves the best available artwork for a Telegram audio message,
     * trying sources in order of quality:
     *
     *  1. Embedded artwork in the downloaded audio file (highest quality)
     *  2. externalAlbumCovers from TDLib (Spotify / Apple Music metadata)
     *  3. albumCoverThumbnail from TDLib (low-res thumbnail)
     *  4. Inline minithumbnail (tiny, last resort)
     *
     * Returns a File pointing to the artwork, or null if nothing is available.
     */
    private suspend fun resolveBestArtwork(
        chatId: Long,
        messageId: Long,
        sourceAudioFile: File? = null
    ): File? {
        // ─── Prioridade 1: capa embutida no arquivo de áudio ────────────────
        if (sourceAudioFile != null && sourceAudioFile.exists()) {
            val embedded = extractEmbeddedArtwork(sourceAudioFile)
            if (embedded != null) {
                Timber.d("Artwork source 1 (embedded): ${embedded.absolutePath}")
                return embedded
            }
        }

        // ─── Prioridade 2 e 3: externalAlbumCovers > albumCoverThumbnail ────
        val message = getMessage(chatId, messageId) ?: return null

        val thumbnailCandidates: List<TdApi.Thumbnail> = when (val content = message.content) {
            is TdApi.MessageAudio -> {
                buildList {
                    content.audio.externalAlbumCovers
                        ?.filter { it.file.local.canBeDownloaded }
                        ?.sortedByDescending { it.width * it.height }
                        ?.let { addAll(it) }
                    content.audio.albumCoverThumbnail?.let { add(it) }
                }
            }
            is TdApi.MessageDocument -> {
                listOfNotNull(content.document.thumbnail)
            }
            else -> emptyList()
        }

        for ((index, thumb) in thumbnailCandidates.withIndex()) {
            val downloaded = downloadFileAwait(
                fileId = thumb.file.id,
                priority = 1,
                enforceCacheLimit = false
            )
            if (!downloaded.isNullOrBlank()) {
                val f = File(downloaded)
                if (f.exists() && f.length() > 0) {
                    Timber.d(
                        "Artwork source ${index + 2} (${thumb.width}x${thumb.height}): ${f.absolutePath}"
                    )
                    return f
                }
            }
        }

        // ─── Prioridade 4: minithumbnail inline ─────────────────────────────
        val miniBytes = when (val content = message.content) {
            is TdApi.MessageAudio -> content.audio.albumCoverMinithumbnail?.data
            is TdApi.MessageDocument -> content.document.minithumbnail?.data
            else -> null
        }
        if (miniBytes != null && miniBytes.isNotEmpty()) {
            val out = File(context.cacheDir, "tg_mini_${chatId}_${messageId}.jpg")
            out.writeBytes(miniBytes)
            Timber.d("Artwork source 4 (minithumbnail): ${out.absolutePath}")
            return out
        }

        return null
    }

    /**
     * Extracts embedded artwork (APIC frame) from an audio file via
     * MediaMetadataRetriever, writing it to cache. Returns the cached file
     * or null if the audio has no embedded artwork.
     *
     * The returned file's name starts with "tg_embedded_" so callers can
     * detect that the artwork is already inside the audio and skip re-embedding.
     */
    private fun extractEmbeddedArtwork(audioFile: File): File? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(audioFile.absolutePath)
            val bytes = retriever.embeddedPicture
            if (bytes == null || bytes.isEmpty()) {
                return null
            }

            // Validate that the bytes are actually an image
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            if (opts.outWidth <= 0 || opts.outHeight <= 0) {
                return null
            }

            val out = File(
                context.cacheDir,
                "tg_embedded_${audioFile.nameWithoutExtension}_${audioFile.lastModified()}.jpg"
            )
            if (!out.exists() || out.length() == 0L) {
                out.writeBytes(bytes)
            }
            Timber.d("Embedded art: ${opts.outWidth}x${opts.outHeight}")
            out
        } catch (e: Exception) {
            Timber.w(e, "extractEmbeddedArtwork failed for ${audioFile.name}")
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
                // Ignore release errors
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Download to public MediaStore with best artwork embed
    // ────────────────────────────────────────────────────────────────────────

    suspend fun downloadAudioToPublic(
        fileId: Int,
        fileName: String,
        mimeType: String,
        chatId: Long,
        messageId: Long,
        priority: Int = 16
    ): String? {                                    // ← CORRIGIDO: chave faltando
        val tempPath = downloadFileAwait(
            fileId = fileId,
            priority = priority,
            enforceCacheLimit = false
        ) ?: return null

        return withContext(Dispatchers.IO) {
            try {
                val sourceFile = File(tempPath)

                if (!sourceFile.exists()) {
                    Timber.e("Downloaded audio does not exist: $tempPath")
                    return@withContext null
                }

                // ─────────────────────────────────────────────
                // Resolve a melhor capa disponível (cascata)
                // ─────────────────────────────────────────────
                val artworkFile = resolveBestArtwork(
                    chatId = chatId,
                    messageId = messageId,
                    sourceAudioFile = sourceFile
                )

                // ─────────────────────────────────────────────
                // Incorpora a capa no arquivo de áudio (se necessário)
                // ─────────────────────────────────────────────
                if (artworkFile != null) {
                    // Se a capa veio da prioridade 1 (embedded), o áudio já a tem.
                    // Re-embedar seria redundante e reescreveria o ID3 à toa.
                    val isAlreadyEmbedded = artworkFile.name.startsWith("tg_embedded_")
                    if (!isAlreadyEmbedded) {
                        try {
                            val audioFile = AudioFileIO.read(sourceFile)
                            val tag = audioFile.getTagOrCreateAndSetDefault()
                            val artwork = ArtworkFactory.createArtworkFromFile(artworkFile)
                            tag.deleteArtworkField()
                            tag.setField(artwork)
                            audioFile.commit()
                            Timber.d("Artwork embedded from ${artworkFile.name}")
                        } catch (e: Exception) {
                            Timber.e(e, "Artwork embed failed")
                        }
                    } else {
                        Timber.d("Skipping embed — artwork already in file")
                    }
                } else {
                    Timber.d("No artwork available for fileId=$fileId (chatId=$chatId, messageId=$messageId)")
                }

                // ─────────────────────────────────────────────
                // Nome do arquivo
                // ─────────────────────────────────────────────
                val safeName = fileName
                    .substringAfterLast('/')
                    .substringAfterLast('\\')
                    .replace(
                        Regex("""[\\/:*?"<>|]"""),
                        "_"
                    )
                    .ifBlank {
                        "audio_$fileId"
                    }

                // ─────────────────────────────────────────────
                // Salva no Download público
                // ─────────────────────────────────────────────
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, safeName)
                    put(
                        MediaStore.Downloads.MIME_TYPE,
                        mimeType.ifBlank { "application/octet-stream" }
                    )
                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/AurisMusicPlayer/file/songs"
                    )
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val resolver = context.contentResolver

                val uri = resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                ) ?: return@withContext null

                try {
                    val output = resolver.openOutputStream(uri)
                    if (output == null) {
                        resolver.delete(uri, null, null)
                        return@withContext null
                    }

                    output.use { outputStream ->
                        sourceFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    val completedValues = ContentValues().apply {
                        put(MediaStore.Downloads.IS_PENDING, 0)
                    }

                    resolver.update(uri, completedValues, null, null)

                    Timber.d("Telegram audio saved with artwork: $uri")
                    uri.toString()
                } catch (e: Exception) {
                    resolver.delete(uri, null, null)
                    Timber.e(e, "Failed to copy Telegram audio to public storage")
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to download Telegram audio")
                null
            } finally {
                telegramCacheManager.enforceStorageLimit()
            }
        }
    }

    // ─── Chat / Messaging Support ─────────────────────────────────────────────

    /**
     * Fetches chat history (messages) for a chat.
     * Returns messages in chronological order (oldest first).
     */
    suspend fun getChatHistory(
        chatId: Long,
        fromMessageId: Long = 0L,
        limit: Int = 50
    ): List<TdApi.Message> {
        return try {
            val messages = clientManager.sendRequest<TdApi.Messages>(
                TdApi.GetChatHistory(chatId, fromMessageId, 0, limit, false)
            )
            messages.messages.reversed()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching chat history for $chatId")
            emptyList()
        }
    }

    /**
     * Sends a text message to a chat.
     */
    suspend fun sendTextMessage(chatId: Long, text: String): Result<TdApi.Message> {
        return try {
            val request = TdApi.SendMessage().apply {
                this.chatId = chatId
                this.topicId = null
                this.replyTo = null
                this.options = null
                this.replyMarkup = null
                this.inputMessageContent = TdApi.InputMessageText(
                    TdApi.FormattedText(text, emptyArray()),
                    TdApi.LinkPreviewOptions(),
                    false
                )
            }
            Result.success(clientManager.sendRequest(request))
        } catch (e: Exception) {
            Timber.e(e, "SendMessage failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Answers a callback query (inline button press).
     */
    suspend fun answerCallbackQuery(
        chatId: Long,
        messageId: Long,
        callbackData: ByteArray
    ): TdApi.CallbackQueryAnswer? {
        return try {
            clientManager.sendRequest(
                TdApi.GetCallbackQueryAnswer(
                    chatId,
                    messageId,
                    TdApi.CallbackQueryPayloadData(callbackData)
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error answering callback query")
            null
        }
    }

    /**
     * Gets a chat by ID (for getting chat info).
     */
    suspend fun getChat(chatId: Long): TdApi.Chat? {
        return try {
            clientManager.sendRequest(TdApi.GetChat(chatId))
        } catch (e: Exception) {
            Timber.e(e, "Error getting chat $chatId")
            null
        }
    }

    /**
     * Flow of new messages for a specific chat.
     */
    fun observeNewMessages(chatId: Long): Flow<TdApi.Message> {
        return clientManager.updates
            .filterIsInstance<TdApi.UpdateNewMessage>()
            .filter { it.message.chatId == chatId }
            .map { it.message }
    }

    /**
     * Flow of message content updates (for edits).
     */
    fun observeMessageUpdates(chatId: Long): Flow<TdApi.UpdateMessageContent> {
        return clientManager.updates
            .filterIsInstance<TdApi.UpdateMessageContent>()
            .filter { it.chatId == chatId }
    }

    fun observeMessageEdits(chatId: Long): Flow<TdApi.UpdateMessageEdited> {
        return clientManager.updates
            .filterIsInstance<TdApi.UpdateMessageEdited>()
            .filter { it.chatId == chatId }
    }

    fun clearMemoryCache() {
        resolvedPathCache.clear()
        Timber.d("TelegramRepository: Memory cache cleared")
    }

    fun isReady(): Boolean = clientManager.isReady()

    suspend fun awaitReady(timeoutMs: Long = 30_000L): Boolean =
        clientManager.awaitReady(timeoutMs)

    fun sendPhoneNumber(phoneNumber: String) {
        clientManager.sendPhoneNumber(phoneNumber)
    }

    suspend fun sendPhoneNumberAwait(
        phoneNumber: String,
        timeoutMs: Long = AUTH_REQUEST_TIMEOUT_MS
    ): Result<Unit> = runAuthRequest(timeoutMs) {
        val settings = TdApi.PhoneNumberAuthenticationSettings()
        clientManager.sendRequest<TdApi.Ok>(
            TdApi.SetAuthenticationPhoneNumber(phoneNumber, settings)
        )
    }

    fun checkAuthenticationCode(code: String) {
        clientManager.checkAuthenticationCode(code)
    }

    suspend fun checkAuthenticationCodeAwait(
        code: String,
        timeoutMs: Long = AUTH_REQUEST_TIMEOUT_MS
    ): Result<Unit> = runAuthRequest(timeoutMs) {
        clientManager.sendRequest<TdApi.Ok>(TdApi.CheckAuthenticationCode(code))
    }

    fun checkAuthenticationPassword(password: String) {
        clientManager.checkAuthenticationPassword(password)
    }

    suspend fun checkAuthenticationPasswordAwait(
        password: String,
        timeoutMs: Long = AUTH_REQUEST_TIMEOUT_MS
    ): Result<Unit> = runAuthRequest(timeoutMs) {
        clientManager.sendRequest<TdApi.Ok>(TdApi.CheckAuthenticationPassword(password))
    }

    fun logout() {
        clientManager.logout()
    }

    private suspend fun runAuthRequest(
        timeoutMs: Long,
        block: suspend () -> TdApi.Object
    ): Result<Unit> {
        return try {
            withTimeout(timeoutMs) { block() }
            Result.success(Unit)
        } catch (timeout: TimeoutCancellationException) {
            Result.failure(IllegalStateException("Telegram did not respond in ${timeoutMs / 1000}s.", timeout))
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    suspend fun searchPublicChat(username: String): TdApi.Chat? {
        return try {
            clientManager.sendRequest(TdApi.SearchPublicChat(username))
        } catch (e: Exception) {
            Timber.e(e, "Error searching public chat: $username")
            null
        }
    }

    // ─── Forum Topic Support ──────────────────────────────────────────────────

    suspend fun isForum(chatId: Long): Boolean {
    return try {
        val chat = clientManager.sendRequest<TdApi.Chat>(TdApi.GetChat(chatId))
        val type = chat.type
        if (type !is TdApi.ChatTypeSupergroup) return false
        
        val supergroup = clientManager.sendRequest<TdApi.Supergroup>(
            TdApi.GetSupergroup(type.supergroupId)
        )
        
        // Se o supergroup ainda não foi carregado, isForum pode estar incompleto
        if (supergroup.status is TdApi.ChatMemberStatusLeft) return false
        
        supergroup.isForum
    } catch (e: Exception) {
        Timber.w(e, "isForum check failed for chatId=$chatId")
        false
    }
}

    suspend fun getForumTopics(chatId: Long): List<TelegramTopicEntity> {
        val topics = mutableListOf<TelegramTopicEntity>()
        try {
            var offsetDate = 0
            var offsetMessageId = 0L
            var offsetForumTopicId = 0

            while (true) {
                val request = TdApi.GetForumTopics().apply {
                    this.chatId = chatId
                    this.query = ""
                    this.offsetDate = offsetDate
                    this.offsetMessageId = offsetMessageId
                    this.offsetForumTopicId = offsetForumTopicId
                    this.limit = 100
                }
                val result = clientManager.sendRequest<TdApi.ForumTopics>(request)

                if (result.topics.isEmpty()) break

                for (topic in result.topics) {
                    val info = topic.info
                    val emojiId = info.icon.customEmojiId

                    val threadId: Long = run {
                        val allFields = info.javaClass.declaredFields
                        Timber.d("ForumTopicInfo fields: ${allFields.map { "${it.name}:${it.type.simpleName}" }}")

                        var resolved = 0L
                        val preferredNames = listOf(
                            "messageThreadId", "message_thread_id",
                            "threadId", "thread_id",
                            "topicId", "topic_id",
                            "forumTopicId", "forum_topic_id"
                        )
                        for (name in preferredNames) {
                            try {
                                val f = info.javaClass.getDeclaredField(name)
                                f.isAccessible = true
                                val v = f.get(info)
                                val candidate = when (v) {
                                    is Long -> v
                                    is Int -> v.toLong()
                                    else -> 0L
                                }
                                if (candidate != 0L) {
                                    Timber.d("ForumTopicInfo: resolved threadId via field '$name' = $candidate")
                                    resolved = candidate
                                    break
                                }
                            } catch (_: NoSuchFieldException) { }
                        }

                        if (resolved == 0L) {
                            val skipNames = setOf(
                                "chatId", "chat_id", "creatorUserId",
                                "creator_user_id", "customEmojiId", "custom_emoji_id",
                                "editDate", "edit_date", "date"
                            )
                            for (f in allFields) {
                                if (f.name in skipNames) continue
                                if (f.type != Long::class.java && f.type != Int::class.java) continue
                                try {
                                    f.isAccessible = true
                                    val candidate = when (val v = f.get(info)) {
                                        is Long -> v
                                        is Int -> v.toLong()
                                        else -> 0L
                                    }
                                    if (candidate != 0L) {
                                        Timber.w("ForumTopicInfo: fallback threadId via field '${f.name}' = $candidate")
                                        resolved = candidate
                                        break
                                    }
                                } catch (_: Exception) { }
                            }
                        }

                        if (resolved == 0L) {
                            Timber.e("ForumTopicInfo: could not resolve threadId for topic '${info.name}'")
                        }
                        resolved
                    }

                    if (threadId != 0L) {
                        topics.add(
                            TelegramTopicEntity(
                                id = "${chatId}_${threadId}",
                                chatId = chatId,
                                threadId = threadId,
                                name = info.name,
                                iconEmoji = if (emojiId != 0L) emojiId.toString() else null
                            )
                        )
                    }
                }

                if (result.nextOffsetDate == 0) break

                offsetDate = result.nextOffsetDate
                offsetMessageId = result.nextOffsetMessageId
                offsetForumTopicId = result.nextOffsetForumTopicId
            }

            Timber.d("Fetched ${topics.size} forum topics for chat $chatId")
        } catch (e: Exception) {
            Timber.e(e, "Error fetching forum topics for chat $chatId")
        }
        return topics
    }

    suspend fun getAudioMessagesByTopic(chatId: Long, threadId: Long): List<Song> {
        Timber.d("Fetching audio for topic threadId=$threadId in chat=$chatId")
        try {
            clientManager.sendRequest<TdApi.Ok>(TdApi.OpenChat(chatId))
        } catch (e: Exception) {
            Timber.w("Failed to open chat: $chatId")
        }

        val allSongs = mutableListOf<Song>()
        var nextFromMessageId = 0L
        val batchSize = 100

        try {
            while (true) {
                val request = TdApi.SearchChatMessages().apply {
                    this.chatId = chatId
                    this.query = ""
                    this.senderId = null
                    this.fromMessageId = nextFromMessageId
                    this.offset = 0
                    this.limit = batchSize
                    this.filter = TdApi.SearchMessagesFilterAudio()

                    val scFields = this.javaClass.declaredFields
                    Timber.d("SearchChatMessages fields: ${scFields.map { "${it.name}:${it.type.simpleName}" }}")

                    var topicSet = false

                    try {
                        val f = this.javaClass.getDeclaredField("topicId")
                        f.isAccessible = true
                        f.set(this, TdApi.MessageTopicForum(threadId.toInt()))
                        Timber.d("SearchChatMessages: set topicId = MessageTopicForum($threadId)")
                        topicSet = true
                    } catch (_: NoSuchFieldException) { }

                    if (!topicSet) {
                        try {
                            val f = this.javaClass.getDeclaredField("messageThreadId")
                            f.isAccessible = true
                            f.set(this, threadId)
                            Timber.d("SearchChatMessages: set messageThreadId = $threadId")
                            topicSet = true
                        } catch (_: NoSuchFieldException) { }
                    }

                    if (!topicSet) {
                        Timber.e("SearchChatMessages: could not set topic filter — results will be unfiltered")
                    }
                }

                val response = clientManager.sendRequest<TdApi.FoundChatMessages>(request)

                if (response.messages.isEmpty()) break

                response.messages.forEach { message ->
                    mapMessageToSong(message)?.let { allSongs.add(it) }
                }

                nextFromMessageId = response.nextFromMessageId
                if (nextFromMessageId == 0L) break
            }
            Timber.d("Topic $threadId: fetched ${allSongs.size} songs")
        } catch (e: Exception) {
            Timber.e(e, "Error fetching audio for topic $threadId in chat $chatId")
        }
        return allSongs
    }

    suspend fun getAudioMessages(chatId: Long): List<Song> {
        Timber.d("Fetching chat history for chat: $chatId")
        try {
            clientManager.sendRequest<TdApi.Ok>(TdApi.OpenChat(chatId))
        } catch (e: Exception) {
            Timber.w("Failed to open chat: $chatId")
        }

        val allSongs = mutableListOf<Song>()
        var nextFromMessageId = 0L
        val batchSize = 100

        try {
            while (true) {
                val request = TdApi.SearchChatMessages().apply {
                    this.chatId = chatId
                    this.query = ""
                    this.senderId = null
                    this.fromMessageId = nextFromMessageId
                    this.offset = 0
                    this.limit = batchSize
                    this.filter = TdApi.SearchMessagesFilterAudio()
                }

                val response = clientManager.sendRequest<TdApi.FoundChatMessages>(request)

                if (response.messages.isEmpty()) break

                response.messages.forEach { message ->
                    mapMessageToSong(message)?.let { allSongs.add(it) }
                }

                nextFromMessageId = response.nextFromMessageId
                if (nextFromMessageId == 0L) break
            }
            Timber.d("Total mapped audio songs: ${allSongs.size}")
            return allSongs
        } catch (e: Exception) {
            Timber.e(e, "Error fetching chat history for chat $chatId")
            return allSongs
        }
    }

    private suspend fun mapMessageToSong(message: TdApi.Message): Song? {
        val content = message.content

        return when (content) {
            is TdApi.MessageAudio -> {
                val audio = content.audio

                var albumArtPath: String? = null
                var thumbnail = audio.albumCoverThumbnail

                if (thumbnail == null && audio.externalAlbumCovers?.isNotEmpty() == true) {
                    thumbnail = audio.externalAlbumCovers.maxByOrNull { it.width * it.height }
                }

                if (thumbnail != null) {
                    albumArtPath = "telegram_art://${message.chatId}/${message.id}"
                    if (thumbnail.file.local.isDownloadingCompleted && thumbnail.file.local.path.isNotEmpty()) {
                        resolvedPathCache[thumbnail.file.id] = thumbnail.file.local.path
                    }
                }

                Song(
                    id = "${message.chatId}_${message.id}",
                    title = audio.title.takeIf { it.isNotEmpty() }
                        ?: audio.fileName.substringBeforeLast('.').ifEmpty { "Unknown Title" },
                    artist = audio.performer.takeIf { it.isNotEmpty() } ?: "Unknown Artist",
                    artistId = -1,
                    album = "Telegram Stream",
                    albumId = -1,
                    path = "",
                    contentUriString = "telegram://${message.chatId}/${message.id}",
                    albumArtUriString = albumArtPath,
                    duration = audio.duration * 1000L,
                    telegramFileId = audio.audio.id,
                    telegramChatId = message.chatId,
                    mimeType = audio.mimeType,
                    bitrate = 0,
                    sampleRate = 0,
                    year = 0,
                    trackNumber = 0,
                    dateAdded = message.date.toLong(),
                    isFavorite = false
                )
            }
            is TdApi.MessageDocument -> {
                val document = content.document

                val isAudioMime = document.mimeType.startsWith("audio/") || document.mimeType == "application/ogg"
                val isAudioExtension = document.fileName.lowercase().run {
                    endsWith(".mp3") || endsWith(".flac") || endsWith(".wav") ||
                            endsWith(".m4a") || endsWith(".ogg") || endsWith(".aac")
                }

                if (isAudioMime || isAudioExtension) {
                    var albumArtPath: String? = null
                    val thumbnail = document.thumbnail
                    if (thumbnail != null) {
                        albumArtPath = "telegram_art://${message.chatId}/${message.id}"
                        if (thumbnail.file.local.isDownloadingCompleted && thumbnail.file.local.path.isNotEmpty()) {
                            resolvedPathCache[thumbnail.file.id] = thumbnail.file.local.path
                        }
                    }

                    Song(
                        id = "${message.chatId}_${message.id}",
                        title = document.fileName.substringBeforeLast('.').ifEmpty { "Unknown Track" },
                        artist = "Telegram Audio",
                        artistId = -1,
                        album = "Telegram Stream",
                        albumId = -1,
                        path = "",
                        contentUriString = "telegram://${message.chatId}/${message.id}",
                        albumArtUriString = albumArtPath,
                        duration = 0L,
                        telegramFileId = document.document.id,
                        telegramChatId = message.chatId,
                        mimeType = document.mimeType,
                        bitrate = 0,
                        sampleRate = 0,
                        year = 0,
                        trackNumber = 0,
                        dateAdded = message.date.toLong(),
                        isFavorite = false
                    )
                } else null
            }
            else -> null
        }
    }

    suspend fun downloadFile(fileId: Int, priority: Int = 1): TdApi.File? {
        return try {
            clientManager.sendRequest(TdApi.DownloadFile(fileId, priority, 0, 0, false))
        } catch (e: Exception) {
            Timber.e(e, "Error evaluating DownloadFile for fileId: $fileId")
            null
        }
    }

    suspend fun getFile(fileId: Int): TdApi.File? {
        return try {
            clientManager.sendRequest(TdApi.GetFile(fileId))
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMessage(chatId: Long, messageId: Long): TdApi.Message? {
        return try {
            clientManager.sendRequest(TdApi.GetMessage(chatId, messageId))
        } catch (e: Exception) {
            Timber.e(e, "Error fetching message: $chatId / $messageId")
            null
        }
    }

    suspend fun isFileCached(fileId: Int): Boolean {
        resolvedPathCache[fileId]?.let { path ->
            if (java.io.File(path).exists()) return true
            resolvedPathCache.remove(fileId)
        }
        val file = getFile(fileId)
        return file?.local?.isDownloadingCompleted == true &&
                file.local.path.isNotEmpty() &&
                java.io.File(file.local.path).exists()
    }

    suspend fun resolveTelegramUri(uriString: String): Pair<Int, Long>? {
        uriResolutionCache[uriString]?.let { return it }

        val uri = android.net.Uri.parse(uriString)
        if (uri.scheme != "telegram") return null

        val chatId = uri.host?.toLongOrNull()
        val messageId = uri.pathSegments.firstOrNull()?.toLongOrNull()
        if (chatId == null || messageId == null) return null

        val message = getMessage(chatId, messageId) ?: return null

        val result = when (val content = message.content) {
            is TdApi.MessageAudio -> Pair(content.audio.audio.id, content.audio.audio.size)
            is TdApi.MessageDocument -> Pair(content.document.document.id, content.document.document.size)
            else -> null
        }

        if (result != null) uriResolutionCache[uriString] = result
        return result
    }

    fun preResolveTelegramUri(uriString: String) {
        if (uriResolutionCache.containsKey(uriString)) return
        repositoryScope.launch {
            try {
                resolveTelegramUri(uriString)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    suspend fun refreshMessage(chatId: Long, messageId: Long): TdApi.Message? {
        return try {
            val history = clientManager.sendRequest<TdApi.Messages>(
                TdApi.GetChatHistory(chatId, messageId, 0, 1, false)
            )
            history.messages.firstOrNull { it.id == messageId }
                ?: clientManager.sendRequest(TdApi.GetMessage(chatId, messageId))
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing message: $messageId")
            null
        }
    }

    private val resolvedPathCache = java.util.concurrent.ConcurrentHashMap<Int, String>()
    private val uriResolutionCache = java.util.concurrent.ConcurrentHashMap<String, Pair<Int, Long>>()
    private val repositoryScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO
    )
    private val activeDownloads = java.util.concurrent.ConcurrentHashMap<Int, kotlinx.coroutines.Deferred<String?>>()
    private val downloadSemaphore = kotlinx.coroutines.sync.Semaphore(4)

    private val _downloadCompleted = MutableSharedFlow<Int>(extraBufferCapacity = 16)
    val downloadCompleted: SharedFlow<Int> = _downloadCompleted.asSharedFlow()
    private val _songFileUpdated = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val songFileUpdated: SharedFlow<String> = _songFileUpdated.asSharedFlow()

    fun warmUpArtworkForSongs(
        songs: List<TelegramSongEntity>,
        maxSongs: Int = 24
    ) {
        val targets = songs.asSequence()
            .map { it.chatId to it.messageId }
            .distinct()
            .take(maxSongs)
            .toList()

        if (targets.isEmpty()) return

        repositoryScope.launch {
            targets.forEach { (chatId, messageId) ->
                try {
                    warmUpArtwork(chatId, messageId)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.v(e, "Artwork warm-up failed for $chatId/$messageId")
                }
            }
        }
    }

    private suspend fun warmUpArtwork(chatId: Long, messageId: Long) {
        val message = getMessage(chatId, messageId) ?: return
        val fileId = extractArtworkFileId(message.content) ?: return
        val existingFile = getFile(fileId)
        if (existingFile?.local?.isDownloadingCompleted == true && existingFile.local.path.isNotEmpty()) {
            resolvedPathCache[fileId] = existingFile.local.path
            return
        }
        downloadFileAwait(fileId, priority = 1)
    }

    private fun extractArtworkFileId(content: TdApi.MessageContent?): Int? {
        return when (content) {
            is TdApi.MessageAudio -> {
                val thumbnail = content.audio.albumCoverThumbnail
                    ?: content.audio.externalAlbumCovers?.maxByOrNull { it.width * it.height }
                thumbnail?.file?.id
            }
            is TdApi.MessageDocument -> content.document.thumbnail?.file?.id
            else -> null
        }
    }

    private suspend fun persistSongFilePathIfNeeded(fileId: Int, path: String?) {
        if (path.isNullOrBlank()) return

        val existingSong = dao.getSongByFileId(fileId) ?: return
        if (existingSong.filePath == path) return

        dao.insertSongs(listOf(existingSong.copy(filePath = path)))
        _songFileUpdated.tryEmit(existingSong.id)
    }

    suspend fun downloadFileAwait(
        fileId: Int,
        priority: Int = 1,
        enforceCacheLimit: Boolean = true
    ): String? {
        resolvedPathCache[fileId]?.let { path ->
            if (java.io.File(path).exists()) return path
            resolvedPathCache.remove(fileId)
        }

        val existingJob = activeDownloads[fileId]
        if (existingJob != null && existingJob.isActive) return existingJob.await()

        val newJob = repositoryScope.async(start = kotlinx.coroutines.CoroutineStart.LAZY) {
            try {
                downloadSemaphore.withPermit {
                    val currentFile = getFile(fileId)
                    if (currentFile?.local?.isDownloadingCompleted == true) {
                        currentFile.local.path.takeIf { it.isNotEmpty() }?.let {
                            resolvedPathCache[fileId] = it
                            persistSongFilePathIfNeeded(fileId, it)
                            _downloadCompleted.tryEmit(fileId)

                            if (enforceCacheLimit) {
                                telegramCacheManager.enforceStorageLimit()
                            }

                            return@withPermit it
                        }
                    }

                    val initialFile = getFile(fileId)
                    val isSmallFile = initialFile?.size == 0L || (initialFile?.size ?: 0) < 1024 * 1024

                    if (isSmallFile) {
                        return@withPermit try {
                            val resultFile = withTimeout(15_000L) {
                                clientManager.sendRequest<TdApi.File>(TdApi.DownloadFile(fileId, priority, 0, 0, true))
                            }
                            if (resultFile.local.isDownloadingCompleted && resultFile.local.path.isNotEmpty()) {
                                resolvedPathCache[fileId] = resultFile.local.path
                                persistSongFilePathIfNeeded(fileId, resultFile.local.path)
                                _downloadCompleted.tryEmit(fileId)

                                if (enforceCacheLimit) {
                                    telegramCacheManager.enforceStorageLimit()
                                }

                                resultFile.local.path
                            } else null
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            if (e.message?.contains("canceled") != true && e.message?.contains("has failed") != true) {
                                Timber.w("Sync download failed for $fileId: ${e.message}")
                            }
                            null
                        }
                    }

                    try {
                        clientManager.sendRequest<TdApi.File>(TdApi.DownloadFile(fileId, priority, 0, 0, false))
                    } catch (e: Exception) {
                        Timber.w("Async download request failed for $fileId: ${e.message}")
                        return@withPermit null
                    }

                    val completedPath = withTimeoutOrNull(60_000L) {
                        clientManager.updates
                            .filterIsInstance<TdApi.UpdateFile>()
                            .filter { it.file.id == fileId }
                            .first { update ->
                                val file = update.file
                                when {
                                    file.local.isDownloadingCompleted && file.local.path.isNotEmpty() -> true
                                    !file.local.canBeDownloaded -> throw Exception("File cannot be downloaded")
                                    else -> false
                                }
                            }
                            .file.local.path
                    }

                    if (completedPath != null) {
                        resolvedPathCache[fileId] = completedPath
                        persistSongFilePathIfNeeded(fileId, completedPath)
                        _downloadCompleted.tryEmit(fileId)

                        if (enforceCacheLimit) {
                            telegramCacheManager.enforceStorageLimit()
                        }

                        return@withPermit completedPath
                    }

                    val finalFile = getFile(fileId)
                    return@withPermit if (finalFile?.local?.isDownloadingCompleted == true && finalFile.local.path.isNotEmpty()) {
                        persistSongFilePathIfNeeded(fileId, finalFile.local.path)
                        _downloadCompleted.tryEmit(fileId)

                        if (enforceCacheLimit) {
                            telegramCacheManager.enforceStorageLimit()
                        }

                        finalFile.local.path
                    } else null
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w("downloadFileAwait error for $fileId: ${e.message}")
                throw e
            } finally {
                activeDownloads.remove(fileId)
            }
        }

        activeDownloads[fileId] = newJob
        return try {
            newJob.start()
            newJob.await()
        } catch (e: kotlinx.coroutines.CancellationException) {
            newJob.cancel(e)
            throw e
        }
    }

    // ─── App Playlist Management ──────────────────────────────────────────────

    private fun getAppPlaylistIdForChannel(chatId: Long) = "$TELEGRAM_PLAYLIST_PREFIX$chatId"
    private fun getAppPlaylistIdForTopic(chatId: Long, threadId: Long) =
        "$TELEGRAM_TOPIC_PLAYLIST_PREFIX${chatId}_$threadId"

    private fun toUnifiedTelegramSongId(telegramSongId: String): Long {
        val songId = -(telegramSongId.hashCode().toLong().absoluteValue)
        return if (songId == 0L) -1L else songId
    }

    suspend fun updateAppPlaylistForTelegramChannel(
        chatId: Long,
        channelTitle: String,
        telegramEntities: List<TelegramSongEntity>
    ) {
        try {
            val unifiedSongIds = telegramEntities.map { toUnifiedTelegramSongId(it.id).toString() }
            upsertPlaylist(getAppPlaylistIdForChannel(chatId), channelTitle, unifiedSongIds, "TELEGRAM")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update app playlist for Telegram channel $chatId")
        }
    }

    suspend fun updateAppPlaylistForTopic(
        chatId: Long,
        threadId: Long,
        topicName: String,
        telegramEntities: List<TelegramSongEntity>
    ) {
        try {
            val unifiedSongIds = telegramEntities.map { toUnifiedTelegramSongId(it.id).toString() }
            upsertPlaylist(
                getAppPlaylistIdForTopic(chatId, threadId),
                topicName,
                unifiedSongIds,
                "TELEGRAM_TOPIC"
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to update app playlist for topic $threadId in chat $chatId")
        }
    }

    private suspend fun upsertPlaylist(
        playlistId: String,
        name: String,
        songIds: List<String>,
        source: String
    ) {
        val existing = withContext(Dispatchers.IO) {
            playlistPreferencesRepository.userPlaylistsFlow
                .map { it.find { p -> p.id == playlistId } }
                .first()
        }

        if (existing != null) {
            playlistPreferencesRepository.updatePlaylist(
                existing.copy(
                    name = name,
                    songIds = songIds,
                    lastModified = System.currentTimeMillis(),
                    source = source
                )
            )
        } else {
            playlistPreferencesRepository.createPlaylist(
                name = name,
                songIds = songIds,
                customId = playlistId,
                source = source
            )
        }
    }

    suspend fun deleteAppPlaylistForTelegramChannel(chatId: Long) {
        try {
            playlistPreferencesRepository.deletePlaylist(getAppPlaylistIdForChannel(chatId))
        } catch (e: Exception) {
            Timber.w(e, "Failed to delete app playlist for Telegram channel $chatId")
        }
    }

    suspend fun deleteAppPlaylistForTopic(chatId: Long, threadId: Long) {
        try {
            playlistPreferencesRepository.deletePlaylist(getAppPlaylistIdForTopic(chatId, threadId))
        } catch (e: Exception) {
            Timber.w(e, "Failed to delete app playlist for topic $threadId in chat $chatId")
        }
    }

    suspend fun deleteAllTopicPlaylistsForChannel(chatId: Long) {
        try {
            val all = withContext(Dispatchers.IO) {
                playlistPreferencesRepository.userPlaylistsFlow.first()
            }
            val prefix = "$TELEGRAM_TOPIC_PLAYLIST_PREFIX${chatId}_"
            all.filter { it.id.startsWith(prefix) }.forEach {
                playlistPreferencesRepository.deletePlaylist(it.id)
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to delete topic playlists for channel $chatId")
        }
    }
}