package com.goldensystem.auris.ai

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalModelProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MODEL_FILENAME = "qwen3-0.6b.gguf"
        private const val MIN_MODEL_SIZE_BYTES = 100_000_000L // sanity check ~100MB
    }

    fun isDeviceSupported(): Boolean {
        return Build.VERSION.SDK_INT >= 33 &&
            Build.SUPPORTED_ABIS.any { it == "arm64-v8a" }
    }

    fun getModelFile(): File {
        val dir = File(context.filesDir, "models")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, MODEL_FILENAME)
    }

    fun isModelDownloaded(): Boolean {
        val file = getModelFile()
        return file.exists() && file.length() > MIN_MODEL_SIZE_BYTES
    }

    fun getModelPath(): String = getModelFile().absolutePath
}