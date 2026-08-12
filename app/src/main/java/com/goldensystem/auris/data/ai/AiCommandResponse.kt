package com.goldensystem.auris.data.ai

import kotlinx.serialization.Serializable

@Serializable
data class AiCommandResponse(
    val action: String,           // play, pause, next, previous, play_song, play_artist, play_album, add_to_playlist, remove_from_playlist, create_playlist, shuffle, repeat, volume, status, play_favorites
    val target: String? = null,   // nome da música, artista, álbum, playlist
    val value: String? = null,    // valor extra (ex: volume, playlist name)
    val message: String = ""      // mensagem de resposta para o usuário
)