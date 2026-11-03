package com.goldensystem.auris.data.ai.provider

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import com.goldensystem.auris.ai.LocalModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

class LocalAiClient(
    private val context: Context,
    private val modelProvider: LocalModelProvider
) : AiClient {

    companion object {
        const val MODEL_ID = "qwen3-0.6b-q8"
    }

    private val engine: InferenceEngine by lazy {
        AiChat.getInferenceEngine(context)
    }

    @Volatile private var loadedForPath: String? = null
    @Volatile private var appliedSystemPrompt: String? = null

    private suspend fun ensureReady(systemPrompt: String) {
        val path = modelProvider.getModelPath()

        if (loadedForPath != path ||
            engine.state.value !is InferenceEngine.State.ModelReady) {
            if (!modelProvider.isModelDownloaded()) {
                throw IllegalStateException(
                    "Modelo local não foi baixado. Abra as configurações de IA e baixe o Qwen3 0.6B (~610 MB)."
                )
            }
            withContext(Dispatchers.IO) {
                engine.loadModel(path)
            }
            loadedForPath = path
            appliedSystemPrompt = null
        }

        if (appliedSystemPrompt != systemPrompt) {
            withContext(Dispatchers.IO) {
                engine.setSystemPrompt(systemPrompt)
            }
            appliedSystemPrompt = systemPrompt
        }
    }

    override suspend fun generateContent(
        model: String,
        systemPrompt: String,
        prompt: String,
        temperature: Float
    ): String {
        ensureReady(systemPrompt)

        val tokens = withContext(Dispatchers.IO) {
            engine.sendUserPrompt(prompt, predictLength = 512).toList()
        }

        if (tokens.isEmpty()) {
            throw Exception("Auris Local AI retornou resposta vazia.")
        }

        return tokens.joinToString(separator = "")
    }

    override suspend fun countTokens(model: String, systemPrompt: String, prompt: String): Int {
        return (systemPrompt.length + prompt.length) / 4
    }

    override suspend fun getAvailableModels(apiKey: String): List<String> = listOf(MODEL_ID)

    override suspend fun validateApiKey(apiKey: String): Boolean {
        return modelProvider.isDeviceSupported() && modelProvider.isModelDownloaded()
    }

    override fun getDefaultModel(): String = MODEL_ID
}