package com.goldensystem.auris.data.performance

/**
 * Categoria de desempenho do dispositivo.
 * Determinada uma vez na primeira execução e persistida.
 *
 * - BAIXO (1): pouco desempenho. Funções pesadas bloqueadas, sem animações.
 * - MEDIO (2): algumas coisas podem falhar, travar ou fechar inesperadamente.
 * - BOM (3): uso completo do app.
 */
enum class DevicePerformanceTier(val level: Int) {
    BAIXO(1),
    MEDIO(2),
    BOM(3);

    val isLow: Boolean get() = this == BAIXO
    val isMedium: Boolean get() = this == MEDIO
    val isHigh: Boolean get() = this == BOM

    companion object {
        fun fromLevel(level: Int): DevicePerformanceTier = when (level) {
            1 -> BAIXO
            2 -> MEDIO
            else -> BOM
        }
    }
}