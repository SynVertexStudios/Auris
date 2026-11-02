package com.goldensystem.auris.data.performance

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persiste e expõe a categoria de desempenho.
 *
 * Uso:
 *   1. No Application.onCreate → `ensureAnalyzed()` (roda uma vez, salva).
 *   2. Em qualquer lugar depois → `currentTier()` ou `tier` (StateFlow), sem reanálise.
 */
@Singleton
class DevicePerformanceRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val analyzer: DevicePerformanceAnalyzer
) {
    private object Keys {
        val TIER_LEVEL = intPreferencesKey("device_performance_tier")
    }

    // Cache em memória — depois do primeiro load, nunca toca o disco de novo
    private val _tier = MutableStateFlow<DevicePerformanceTier?>(null)
    val tier: StateFlow<DevicePerformanceTier?> = _tier.asStateFlow()

    // Último report de análise (null se veio do disco sem reanalisar)
    private val _lastReport = MutableStateFlow<DevicePerformanceReport?>(null)
    val lastReport: StateFlow<DevicePerformanceReport?> = _lastReport.asStateFlow()

    /**
     * Carrega a categoria salva. Se não existir (primeira abertura), analisa e salva.
     * Idempotente e seguro pra chamar várias vezes — só analisa uma vez.
     */
    suspend fun ensureAnalyzed(): DevicePerformanceTier {
        _tier.value?.let { return it }

        val saved = dataStore.data.first()[Keys.TIER_LEVEL]
        if (saved != null) {
            val tier = DevicePerformanceTier.fromLevel(saved)
            _tier.value = tier
            return tier
        }

        // Primeira execução — analisa e salva
        val report = analyzer.analyze()
        saveTier(report.tier)
        _lastReport.value = report
        return report.tier
    }

    /**
     * Retorna a categoria de forma síncrona. Só deve ser chamado depois de
     * [ensureAnalyzed]. Se ainda não foi analisado, retorna MEDIO como padrão seguro.
     */
    fun currentTier(): DevicePerformanceTier =
        _tier.value ?: DevicePerformanceTier.MEDIO

    /**
     * Força nova análise e salva o resultado. Use no "Reanalisar dispositivo" das
     * configurações. Ignora o valor salvo anteriormente.
     */
    suspend fun reanalyze(): DevicePerformanceReport {
        val report = analyzer.analyze()
        saveTier(report.tier)
        _lastReport.value = report
        return report
    }

    /**
     * Override manual — usuário define a categoria que quiser.
     * Útil quando a detecção automática erra em algum OEM estranho.
     */
    suspend fun overrideTier(tier: DevicePerformanceTier) {
        saveTier(tier)
    }

    private suspend fun saveTier(tier: DevicePerformanceTier) {
        dataStore.edit { prefs -> prefs[Keys.TIER_LEVEL] = tier.level }
        _tier.value = tier
    }
}