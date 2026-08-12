package com.goldensystem.auris.data.ai

import com.goldensystem.auris.presentation.viewmodel.PlayerViewModel
import javax.inject.Inject

class AiCommandExecutor @Inject constructor(
    private val playerViewModel: PlayerViewModel,
    private val musicResolver: AiMusicResolver
) {

    suspend fun execute(command: AiCommandResponse): String {
        return when (command.action) {
            "play" -> executePlay()
            "pause" -> executePause()
            "next" -> executeNext()
            "previous" -> executePrevious()
            "play_song" -> executePlaySong(command.target)
            "play_artist" -> executePlayArtist(command.target)
            "play_album" -> executePlayAlbum(command.target)
            "play_favorites" -> executePlayFavorites()
            "add_to_playlist" -> executeAddToPlaylist(command.target, command.value)
            "remove_from_playlist" -> executeRemoveFromPlaylist(command.target, command.value)
            "create_playlist" -> executeCreatePlaylist(command.target)
            "shuffle" -> executeShuffle()
            "repeat" -> executeRepeat()
            "volume" -> executeVolume(command.value)
            "status" -> executeStatus()
            else -> "❌ Ação '${command.action}' não reconhecida"
        }
    }

    private fun executePlay(): String {
        playerViewModel.playPause()
        return "▶️ Reproduzindo"
    }

    private fun executePause(): String {
        playerViewModel.playPause()
        return "⏸️ Pausado"
    }

    private fun executeNext(): String {
        playerViewModel.nextSong()
        val current = playerViewModel.stablePlayerState.value.currentSong
        return if (current != null) "⏭️ Próxima: ${current.title}" else "⏭️ Próxima música"
    }

    private fun executePrevious(): String {
        playerViewModel.previousSong()
        val current = playerViewModel.stablePlayerState.value.currentSong
        return if (current != null) "⏮️ Anterior: ${current.title}" else "⏮️ Música anterior"
    }

    private suspend fun executePlaySong(target: String?): String {
        if (target.isNullOrBlank()) return "❌ Nome da música não especificado"
        
        val result = musicResolver.resolveSong(target)
        
        return when (result) {
            is AiMusicResolver.ResolveResult.Found -> {
                val song = result.song
                val allSongs = playerViewModel.musicRepository.getAllSongsOnce()
                val contextSongs = allSongs.filter { it.artist == song.artist }
                playerViewModel.showAndPlaySong(song, contextSongs, "AI Command")
                "🎵 Tocando: ${song.title} - ${song.displayArtist}"
            }
            
            is AiMusicResolver.ResolveResult.FoundList -> {
                val songs = result.songs
                if (songs.isNotEmpty()) {
                    playerViewModel.playSongs(songs, songs.first(), "AI Command")
                    "🎵 Tocando ${songs.size} músicas"
                } else {
                    "❌ Nenhuma música encontrada"
                }
            }
            
            is AiMusicResolver.ResolveResult.Suggested -> {
                val songs = result.songs
                val song = songs.first()
                playerViewModel.showAndPlaySong(song, songs, "AI Command")
                result.message + " 🎵 Tocando: ${song.title}"
            }
            
            is AiMusicResolver.ResolveResult.NotFound -> {
                result.message
            }
        }
    }

    private suspend fun executePlayArtist(target: String?): String {
        if (target.isNullOrBlank()) return "❌ Nome do artista não especificado"
        
        val result = musicResolver.resolveArtist(target)
        
        return when (result) {
            is AiMusicResolver.ResolveResult.FoundList -> {
                val songs = result.songs
                playerViewModel.playSongs(songs, songs.first(), target)
                "🎤 Tocando ${songs.size} músicas de ${target}"
            }
            
            is AiMusicResolver.ResolveResult.Suggested -> {
                val songs = result.songs
                playerViewModel.playSongs(songs, songs.first(), target)
                result.message + " 🎤 Tocando ${songs.size} músicas"
            }
            
            is AiMusicResolver.ResolveResult.NotFound -> {
                result.message
            }
            
            else -> "❌ Erro ao processar"
        }
    }

    private suspend fun executePlayAlbum(target: String?): String {
        if (target.isNullOrBlank()) return "❌ Nome do álbum não especificado"
        
        val result = musicResolver.resolveAlbum(target)
        
        return when (result) {
            is AiMusicResolver.ResolveResult.FoundList -> {
                val songs = result.songs
                if (songs.isNotEmpty()) {
                    playerViewModel.playSongs(songs, songs.first(), target)
                    "💿 Tocando ${songs.size} músicas do álbum ${target}"
                } else {
                    "❌ Álbum vazio"
                }
            }
            
            is AiMusicResolver.ResolveResult.Suggested -> {
                val songs = result.songs
                if (songs.isNotEmpty()) {
                    playerViewModel.playSongs(songs, songs.first(), target)
                    result.message + " 💿 Tocando ${songs.size} músicas"
                } else {
                    "❌ Álbum vazio"
                }
            }
            
            is AiMusicResolver.ResolveResult.NotFound -> {
                result.message
            }
            
            else -> "❌ Erro ao processar"
        }
    }

    private suspend fun executePlayFavorites(): String {
        val favorites = playerViewModel.musicRepository.getFavoriteSongsOnce()
        return if (favorites.isNotEmpty()) {
            playerViewModel.playSongsShuffled(favorites, "Favoritas")
            "❤️ Tocando ${favorites.size} músicas favoritas"
        } else {
            "❌ Nenhuma música favorita encontrada"
        }
    }

    private suspend fun executeAddToPlaylist(songTitle: String?, playlistName: String?): String {
        if (songTitle.isNullOrBlank()) return "❌ Nome da música não especificado"
        if (playlistName.isNullOrBlank()) return "❌ Nome da playlist não especificado"
        
        val allSongs = playerViewModel.musicRepository.getAllSongsOnce()
        val song = allSongs.find { 
            it.title.lowercase().contains(songTitle.lowercase()) ||
            songTitle.lowercase().contains(it.title.lowercase())
        }
        
        if (song == null) {
            return "❌ Música '$songTitle' não encontrada"
        }
        
        val playlists = playerViewModel.playlistPreferencesRepository.getPlaylistsOnce()
        val playlist = playlists.find { 
            it.name.lowercase().contains(playlistName.lowercase()) ||
            playlistName.lowercase().contains(it.name.lowercase())
        }
        
        return if (playlist != null) {
            if (playlist.songIds.contains(song.id)) {
                "⚠️ '${song.title}' já está na playlist '${playlist.name}'"
            } else {
                val updatedSongIds = playlist.songIds + song.id
                val updatedPlaylist = playlist.copy(songIds = updatedSongIds)
                val updatedList = playlists.map { if (it.id == playlist.id) updatedPlaylist else it }
                playerViewModel.playlistPreferencesRepository.replaceAllPlaylists(updatedList)
                "✅ '${song.title}' adicionada à playlist '${playlist.name}'"
            }
        } else {
            val newPlaylist = com.goldensystem.auris.data.model.Playlist(
                id = java.util.UUID.randomUUID().toString(),
                name = playlistName,
                songIds = listOf(song.id),
                createdAt = System.currentTimeMillis()
            )
            playerViewModel.playlistPreferencesRepository.replaceAllPlaylists(playlists + newPlaylist)
            "✅ Playlist '$playlistName' criada com '${song.title}'"
        }
    }

    private suspend fun executeRemoveFromPlaylist(songTitle: String?, playlistName: String?): String {
        if (songTitle.isNullOrBlank()) return "❌ Nome da música não especificado"
        if (playlistName.isNullOrBlank()) return "❌ Nome da playlist não especificado"
        
        val allSongs = playerViewModel.musicRepository.getAllSongsOnce()
        val song = allSongs.find { 
            it.title.lowercase().contains(songTitle.lowercase()) ||
            songTitle.lowercase().contains(it.title.lowercase())
        }
        
        if (song == null) {
            return "❌ Música '$songTitle' não encontrada"
        }
        
        val playlists = playerViewModel.playlistPreferencesRepository.getPlaylistsOnce()
        val playlist = playlists.find { 
            it.name.lowercase().contains(playlistName.lowercase()) ||
            playlistName.lowercase().contains(it.name.lowercase())
        }
        
        return if (playlist != null) {
            if (playlist.songIds.contains(song.id)) {
                val updatedSongIds = playlist.songIds.filter { it != song.id }
                val updatedPlaylist = playlist.copy(songIds = updatedSongIds)
                val updatedList = playlists.map { if (it.id == playlist.id) updatedPlaylist else it }
                playerViewModel.playlistPreferencesRepository.replaceAllPlaylists(updatedList)
                "🗑️ '${song.title}' removida da playlist '${playlist.name}'"
            } else {
                "⚠️ '${song.title}' não está na playlist '${playlist.name}'"
            }
        } else {
            "❌ Playlist '$playlistName' não encontrada"
        }
    }

    private suspend fun executeCreatePlaylist(target: String?): String {
        if (target.isNullOrBlank()) return "❌ Nome da playlist não especificado"
        
        val currentSongs = playerViewModel.playerUiState.value.currentPlaybackQueue.toList()
        
        if (currentSongs.isEmpty()) {
            return "❌ Nenhuma música na fila para criar playlist"
        }
        
        val playlist = com.goldensystem.auris.data.model.Playlist(
            id = java.util.UUID.randomUUID().toString(),
            name = target,
            songIds = currentSongs.map { it.id },
            createdAt = System.currentTimeMillis()
        )
        
        val playlists = playerViewModel.playlistPreferencesRepository.getPlaylistsOnce()
        playerViewModel.playlistPreferencesRepository.replaceAllPlaylists(playlists + playlist)
        
        return "✅ Playlist '$target' criada com ${currentSongs.size} músicas!"
    }

    private fun executeShuffle(): String {
        playerViewModel.toggleShuffle()
        val enabled = playerViewModel.stablePlayerState.value.isShuffleEnabled
        return if (enabled) "🔀 Aleatório ativado" else "🔀 Aleatório desativado"
    }

    private fun executeRepeat(): String {
        playerViewModel.cycleRepeatMode()
        val mode = playerViewModel.stablePlayerState.value.repeatMode
        val text = when (mode) {
            1 -> "🔁 Repetir todas"
            2 -> "🔂 Repetir uma"
            else -> "➡️ Repetição desativada"
        }
        return text
    }

    private fun executeVolume(value: String?): String {
        val current = playerViewModel.trackVolume.value
        
        return when (value?.lowercase()) {
            "up", "aumentar", "+" -> {
                val new = (current + 0.1f).coerceAtMost(1f)
                playerViewModel.setTrackVolume(new)
                "🔊 Volume: ${(new * 100).toInt()}%"
            }
            "down", "diminuir", "-" -> {
                val new = (current - 0.1f).coerceAtLeast(0f)
                playerViewModel.setTrackVolume(new)
                "🔊 Volume: ${(new * 100).toInt()}%"
            }
            else -> {
                val level = value?.toIntOrNull()?.coerceIn(1, 100) ?: 50
                playerViewModel.setTrackVolume(level / 100f)
                "🔊 Volume: $level%"
            }
        }
    }

    private fun executeStatus(): String {
        val state = playerViewModel.stablePlayerState.value
        val current = state.currentSong
        return if (current != null) {
            "🎵 ${current.title}\n🎤 ${current.displayArtist}\n${if (state.isPlaying) "▶️ Reproduzindo" else "⏸️ Pausado"}"
        } else {
            "⏸️ Nenhuma música tocando"
        }
    }
}