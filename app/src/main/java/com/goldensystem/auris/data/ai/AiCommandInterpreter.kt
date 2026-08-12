package com.goldensystem.auris.data.ai

import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiCommandInterpreter @Inject constructor(
    private val aiOrchestrator: AiOrchestrator
) {

    suspend fun interpret(userMessage: String): AiCommandResponse? {
        val prompt = """
            Você é um assistente de música. Analise o pedido do usuário e retorne um JSON com a ação a ser executada.
            
            Ações disponíveis:
            - play: tocar (se não especificar o que)
            - pause: pausar
            - next: próxima música
            - previous: música anterior
            - play_song: tocar uma música específica
            - play_artist: tocar um artista específico
            - play_album: tocar um álbum específico
            - play_favorites: tocar músicas favoritas
            - add_to_playlist: adicionar música a uma playlist
            - remove_from_playlist: remover música de uma playlist
            - create_playlist: criar uma nova playlist
            - shuffle: ativar/desativar aleatório
            - repeat: alternar repetição
            - volume: ajustar volume (up/down ou número)
            - status: mostrar o que está tocando
            
            Exemplos:
            - "toca blinding lights" → {"action":"play_song","target":"blinding lights","message":"Tocando Blinding Lights"}
            - "pausa a música" → {"action":"pause","message":"Música pausada"}
            - "toca the weeknd" → {"action":"play_artist","target":"the weeknd","message":"Tocando The Weeknd"}
            - "toca o álbum after hours" → {"action":"play_album","target":"after hours","message":"Tocando After Hours"}
            - "aumenta o volume" → {"action":"volume","value":"up","message":"Volume aumentado"}
            - "cria uma playlist chamada festa" → {"action":"create_playlist","target":"festa","message":"Playlist 'festa' criada"}
            - "adiciona blinding lights na playlist favoritas" → {"action":"add_to_playlist","target":"blinding lights","value":"favoritas","message":"Adicionado à playlist"}
            - "o que tá tocando?" → {"action":"status","message":"Verificando música atual"}
            - "toca minhas favoritas" → {"action":"play_favorites","message":"Tocando favoritas"}
            - "próxima música" → {"action":"next","message":"Próxima música"}
            
            IMPORTANTE: 
            - Se o usuário pedir uma música, artista ou álbum, sempre preencha o campo "target" com o nome.
            - Para volume, use "up", "down" ou um número de 1 a 100 no campo "value".
            - Para adicionar/remover de playlist, use "target" para a música e "value" para a playlist.
            - Sempre retorne uma mensagem amigável no campo "message".
            
            Pedido do usuário: "$userMessage"
            
            Retorne APENAS o JSON, sem explicações adicionais.
        """.trimIndent()

        return try {
            val response = aiOrchestrator.generateContent(
                prompt = prompt,
                type = AiSystemPromptType.GENERAL,
                temperature = 0.3f
            )
            
            val cleanResponse = response
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<AiCommandResponse>(cleanResponse)
        } catch (e: Exception) {
            // Fallback: tenta interpretar com parser simples
            val fallbackCommand = simpleParse(userMessage)
            if (fallbackCommand != null) {
                AiCommandResponse(
                    action = fallbackCommand.first,
                    target = fallbackCommand.second,
                    value = fallbackCommand.third,
                    message = "Comando interpretado"
                )
            } else {
                null
            }
        }
    }
    
    // Parser simples de fallback para quando a IA falhar
    private fun simpleParse(message: String): Triple<String, String?, String?>? {
        val lower = message.lowercase().trim()
        
        return when {
            lower.contains("pausa") || lower.contains("pause") -> 
                Triple("pause", null, null)
            lower.contains("próxima") || lower.contains("proxima") || lower.contains("next") -> 
                Triple("next", null, null)
            lower.contains("anterior") || lower.contains("previous") || lower.contains("voltar") -> 
                Triple("previous", null, null)
            lower.contains("status") || lower.contains("tocando") || lower.contains("now playing") -> 
                Triple("status", null, null)
            lower.contains("favorita") || lower.contains("curtida") -> 
                Triple("play_favorites", null, null)
            lower.contains("aleatório") || lower.contains("aleatorio") || lower.contains("shuffle") -> 
                Triple("shuffle", null, null)
            lower.contains("repetir") || lower.contains("repeat") -> 
                Triple("repeat", null, null)
            lower.contains("volume") && lower.contains("aumentar") -> 
                Triple("volume", null, "up")
            lower.contains("volume") && lower.contains("diminuir") -> 
                Triple("volume", null, "down")
            lower.contains("play") || lower.contains("tocar") -> {
                // Tenta extrair o que vem depois de "play" ou "tocar"
                val patterns = listOf("play", "tocar")
                var target = message
                patterns.forEach { pattern ->
                    target = target.replace(Regex("\\b$pattern\\b", RegexOption.IGNORE_CASE), "")
                }
                target = target.trim()
                if (target.isNotBlank()) {
                    // Verifica se é artista ou álbum
                    when {
                        lower.contains("artista") -> Triple("play_artist", target, null)
                        lower.contains("álbum") || lower.contains("album") -> Triple("play_album", target, null)
                        else -> Triple("play_song", target, null)
                    }
                } else {
                    Triple("play", null, null)
                }
            }
            else -> null
        }
    }
}