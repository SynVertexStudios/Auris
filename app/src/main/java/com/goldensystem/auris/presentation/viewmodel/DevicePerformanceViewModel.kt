package com.goldensystem.auris.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldensystem.auris.data.performance.DevicePerformanceReport
import com.goldensystem.auris.data.performance.DevicePerformanceRepository
import com.goldensystem.auris.data.performance.DevicePerformanceTier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DevicePerformanceUiState(
    val tier: DevicePerformanceTier = DevicePerformanceTier.MEDIO,
    val report: DevicePerformanceReport? = null,
    val isAnalyzing: Boolean = false
)

@HiltViewModel
class DevicePerformanceViewModel @Inject constructor(
    private val repository: DevicePerformanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicePerformanceUiState())
    val uiState: StateFlow<DevicePerformanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Força nova análise toda vez que a tela é aberta
            _uiState.update { it.copy(isAnalyzing = true) }
            val report = repository.reanalyze()
            _uiState.update {
                it.copy(
                    tier = report.tier,
                    report = report,
                    isAnalyzing = false
                )
            }
        }
    }
}}