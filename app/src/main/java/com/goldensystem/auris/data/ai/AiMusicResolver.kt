package com.goldensystem.auris.data.ai

import com.goldensystem.auris.data.model.Song
import com.goldensystem.auris.presentation.viewmodel.PlayerViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AiMusicResolver @Inject constructor(
    private val playerViewModel: PlayerViewModel,
    private val aiOrchestrator: AiOrchestrator
) {

    sealed class ResolveResult {
        data class Found(val song: Song) : ResolveResult()
        data class FoundList(val songs: List<Song>) : ResolveResult()
        data class Suggested(val songs: List<Song>, val message: String) : ResolveResult()
        data class NotFound(val message: String) : ResolveResult()
    }

    suspend fun resolveSong(target: String): ResolveResult {
        val allSongs = playerViewModel.musicRepository.getAllSongsOnce()
        
        // 1. Busca exata
        val exactMatch = allSongs.find { it.title.equals(target, ignoreCase = true) }
        if (exactMatch != null) {
            return ResolveResult.Found(exactMatch)
        }
        
        // 2. Busca por similaridade
        val similar = allSongs.filter { 
            it.title.lowercase().contains(target.lowercase()) ||
            target.lowercase().contains(it.title.lowercase())
        }
        
        if (similar.isNotEmpty()) {
            // ✅ CORRIGIDO: first() → [0]
            val message = "Não encontrei '$target', mas encontrei '${similar[0].title}' - ${similar[0].displayArtist}"
            return ResolveResult.Suggested(similar, message)
        }
        
        // 3. Busca por artista
        val byArtist = allSongs.filter { 
            it.displayArtist.lowercase().contains(target.lowercase()) ||
            target.lowercase().contains(it.displayArtist.lowercase())
        }
        
        if (byArtist.isNotEmpty()) {
            // ✅ CORRIGIDO: first() → [0]
            val message = "Não encontrei a música '$target', mas encontrei músicas de ${byArtist[0].displayArtist}"
            return ResolveResult.Suggested(byArtist, message)
        }
        
        // 4. Tenta sugestão da IA
        val suggestion = generateSuggestion(target, allSongs)
        if (suggestion != null) {
            val message = "Não encontrei '$target', mas você pode gostar de '${suggestion.title}' - ${suggestion.displayArtist}"
            return ResolveResult.Suggested(listOf(suggestion), message)
        }
        
        return ResolveResult.NotFound("❌ Não encontrei '$target' na sua biblioteca. Tente outro nome ou artista.")
    }

    suspend fun resolveArtist(target: String): ResolveResult {
        val allSongs = playerViewModel.musicRepository.getAllSongsOnce()
        
        val exactMatch = allSongs.filter { it.displayArtist.equals(target, ignoreCase = true) }
        if (exactMatch.isNotEmpty()) {
            return ResolveResult.FoundList(exactMatch)
        }
        
        val similar = allSongs.filter { 
            it.displayArtist.lowercase().contains(target.lowercase()) ||
            target.lowercase().contains(it.displayArtist.lowercase())
        }
        
        if (similar.isNotEmpty()) {
            // ✅ CORRIGIDO: first() → [0]
            val message = "Não encontrei '$target', mas encontrei '${similar[0].displayArtist}'"
            return ResolveResult.Suggested(similar, message)
        }
        
        return ResolveResult.NotFound("❌ Artista '$target' não encontrado na sua biblioteca.")
    }

    suspend fun resolveAlbum(target: String): ResolveResult {
        val allAlbums = playerViewModel.musicRepository.getAllAlbumsOnce()
        
        val exactMatch = allAlbums.find { it.title.equals(target, ignoreCase = true) }
        if (exactMatch != null) {
            val songs = playerViewModel.musicRepository.getSongsForAlbum(exactMatch.id).first()
            return ResolveResult.FoundList(songs)
        }
        
        val similar = allAlbums.filter { 
            it.title.lowercase().contains(target.lowercase()) ||
            target.lowercase().contains(it.title.lowercase())
        }
        
        if (similar.isNotEmpty()) {
            val songs = playerViewModel.musicRepository.getSongsForAlbum(similar[0].id).first()
            // ✅ CORRIGIDO: first() → [0]
            val message = "Não encontrei '$target', mas encontrei '${similar[0].title}'"
            return ResolveResult.Suggested(songs, message)
        }
        
        return ResolveResult.NotFound("❌ Álbum '$target' não encontrado na sua biblioteca.")
    }

    private suspend fun generateSuggestion(target: String, allSongs: List<Song>): Song? {
        if (allSongs.isEmpty()) return null
        
        val context = allSongs.take(30)
        
        val prompt = """
            O usuário pediu a música: "$target"
            
            Não encontrei exatamente isso na biblioteca.
            
            Sugira uma música similar da lista abaixo:
            ${context.joinToString("\n") { "${it.title} - ${it.displayArtist}" }}
            
            Retorne APENAS o nome da música sugerida, sem explicações.
            Se nenhuma for adequada, retorne "nenhuma".
        """.trimIndent()

        return try {
            val response = aiOrchestrator.generateContent(
                prompt = prompt,
                type = AiSystemPromptType.GENERAL,
                temperature = 0.3f
            )
            
            val suggestedTitle = response.trim()
            if (suggestedTitle.lowercase() == "nenhuma") {
                null
            } else {
                allSongs.find { 
                    it.title.equals(suggestedTitle, ignoreCase = true) ||
                    it.title.lowercase().contains(suggestedTitle.lowercase())
                }
            }
        } catch (e: Exception) {
            allSongs.randomOrNull()
        }
    }
}