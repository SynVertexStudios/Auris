package com.goldensystem.auris.data.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Snapshot detalhado de uma análise — útil pra debug / tela de diagnóstico.
 */
data class DevicePerformanceReport(
    val tier: DevicePerformanceTier,
    val totalScore: Int,
    val maxScore: Int,
    val ramGb: Double,
    val cpuCores: Int,
    val maxCpuFreqGhz: Double,
    val androidSdk: Int,
    val abi: String,
    val isLowRam: Boolean,
    val isProbablyEmulator: Boolean,
    val breakdown: Map<String, Int>
)

/**
 * Analisa o hardware e o Android do celular, atribuindo uma nota.
 * Executado no máximo uma vez (pelo [DevicePerformanceRepository]).
 *
 * Thread-safe: stateless, sem estado interno.
 */
@Singleton
class DevicePerformanceAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
    private const val TAG = "DevicePerfAnalyzer"
    private const val MAX_SCORE = 11
    private const val BAIXO_THRESHOLD = 5
    private const val BOM_THRESHOLD = 9
}

    fun analyze(): DevicePerformanceReport {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // ---- Coleta ----
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val ramGb = memInfo.totalMem / (1024.0 * 1024 * 1024)

        val cores = Runtime.getRuntime().availableProcessors()
        val sdk = Build.VERSION.SDK_INT
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
        val maxFreqGhz = readMaxCpuFreqGhz()
        val isLowRam = am.isLowRamDevice
        val isEmulator = detectEmulator()

        // ---- Pontuação ----
        val ramScore = when {
            ramGb >= 8.0 -> 4
            ramGb >= 6.0 -> 3
            ramGb >= 4.0 -> 2
            ramGb >= 3.0 -> 1
            else -> -2  // < 3GB = penalidade forte
        }

        val coresScore = when {
            cores >= 8 -> 2
            cores >= 6 -> 1
            else -> 0
        }

        // Android: peso reduzido — um celular antigo atualizado pode ser potente,
// e um celular novo pode ser fraco. Versão do SO não determina hardware.
val sdkScore = when {
    sdk >= 34 -> 2   // Android 14+
    sdk >= 30 -> 1   // Android 11+
    else -> 0
}

val abiScore = when {
    abi == "arm64-v8a" -> 2
    abi == "armeabi-v7a" -> 1
    else -> 0
}

val freqScore = if (maxFreqGhz >= 2.0) 1 else 0

val lowRamPenalty = if (isLowRam) -4 else 0
// Emulador é apenas informativo — não penaliza a nota, pois emuladores
// podem ter hardware virtual generoso. Fica visível no breakdown pra diagnóstico.
val emulatorPenalty = 0

        val total = ramScore + coresScore + sdkScore + abiScore +
                    freqScore + lowRamPenalty + emulatorPenalty

// Regras de reprovação imediata — hardware não importa:
// 1) Android < 13 (SDK < 33): sem suporte a RenderEffect/Modifier.blur nativo.
// 2) ABI não arm64: sem suporte a código 64-bit, limita memória por processo
//    a ~3-4GB e não recebe otimizações modernas de bibliotecas nativas.
val isUnsupportedAndroid = sdk < 33
val isUnsupportedAbi = abi != "arm64-v8a"

val tier = if (isUnsupportedAndroid || isUnsupportedAbi) {
    DevicePerformanceTier.BAIXO
} else {
    when {
        total >= BOM_THRESHOLD -> DevicePerformanceTier.BOM
        total >= BAIXO_THRESHOLD -> DevicePerformanceTier.MEDIO
        else -> DevicePerformanceTier.BAIXO
    }
}

        val breakdown = mapOf(
            "ram" to ramScore,
            "cores" to coresScore,
            "sdk" to sdkScore,
            "abi" to abiScore,
            "freq" to freqScore,
            "lowRam" to lowRamPenalty,
            "emulator" to emulatorPenalty
        )

        val report = DevicePerformanceReport(
            tier = tier,
            totalScore = total,
            maxScore = MAX_SCORE,
            ramGb = ramGb,
            cpuCores = cores,
            maxCpuFreqGhz = maxFreqGhz,
            androidSdk = sdk,
            abi = abi,
            isLowRam = isLowRam,
            isProbablyEmulator = isEmulator,
            breakdown = breakdown
        )

        Timber.tag(TAG).d(
            "Performance: tier=%s score=%d/%d RAM=%.1fGB cores=%d sdk=%d abi=%s freq=%.2fGHz lowRam=%s emu=%s breakdown=%s",
            tier.name, total, MAX_SCORE, ramGb, cores, sdk, abi,
            maxFreqGhz, isLowRam, isEmulator, breakdown
        )

        return report
    }

    /**
     * Lê /sys/devices/system/cpu/cpuN/cpufreq/cpuinfo_max_freq (em kHz) e retorna em GHz.
     * Em dispositivos onde o arquivo é ilegível (comum em Android 10+), retorna 0.0.
     */
    private fun readMaxCpuFreqGhz(): Double {
        return try {
            val cpuDirs = File("/sys/devices/system/cpu/").listFiles { f ->
                f.isDirectory && f.name.matches(Regex("cpu\\d+"))
            } ?: return 0.0

            var maxKhz = 0L
            for (dir in cpuDirs) {
                val freqFile = File(dir, "cpufreq/cpuinfo_max_freq")
                if (freqFile.exists() && freqFile.canRead()) {
                    val khz = freqFile.readText().trim().toLongOrNull() ?: continue
                    if (khz > maxKhz) maxKhz = khz
                }
            }
            maxKhz / 1_000_000.0
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to read CPU max freq")
            0.0
        }
    }

    private fun detectEmulator(): Boolean {
        val fp = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val product = Build.PRODUCT.lowercase()
        val hardware = Build.HARDWARE.lowercase()
        return fp.startsWith("generic") ||
               fp.contains("emulator") ||
               model.contains("emulator") ||
               model.contains("android sdk built for") ||
               product.contains("sdk") ||
               product.contains("emulator") ||
               hardware.contains("goldfish") ||
               hardware.contains("ranchu")
    }
}