package com.goldensystem.auris.presentation.external

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExternalPlayerUiState(
    val title: String = "Carregando...",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val duration: Long = 0L,
    val currentPosition: Long = 0L
)

@HiltViewModel
class ExternalPlayerViewModel @Inject constructor(
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExternalPlayerUiState())
    val uiState: StateFlow<ExternalPlayerUiState> = _uiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var currentUri: Uri? = null

    fun loadAudio(uri: Uri) {
        currentUri = uri
        
        // Liberar player anterior se existir
        exoPlayer?.release()
        
        // Criar novo ExoPlayer
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
            
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.update { it.copy(isPlaying = isPlaying) }
                }
                
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    // Atualizar metadados
                    val metadata = mediaItem?.mediaMetadata
                    _uiState.update { 
                        it.copy(
                            title = metadata?.title?.toString() ?: uri.lastPathSegment ?: "Áudio",
                            artist = metadata?.artist?.toString() ?: "",
                            duration = duration
                        )
                    }
                }
            })
        }
        
        // Atualizar metadados iniciais
        _uiState.update { 
            it.copy(
                title = uri.lastPathSegment ?: "Áudio",
                artist = ""
            )
        }
        
        // Iniciar reprodução
        exoPlayer?.play()
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun previous() {
        exoPlayer?.seekToPrevious()
    }

    fun next() {
        exoPlayer?.seekToNext()
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer?.release()
        exoPlayer = null
    }
}