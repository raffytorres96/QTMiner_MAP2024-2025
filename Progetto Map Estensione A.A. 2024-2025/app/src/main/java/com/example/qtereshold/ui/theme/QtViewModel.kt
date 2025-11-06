package com.example.qtereshold.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qtereshold.data.*
import com.example.qtereshold.repo.QtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val isRunning: Boolean = false,
    val progress: Int = 0,
    val clusters: List<Cluster> = emptyList(),
    val noise: List<String> = emptyList(),
    val error: String? = null
)

class QtViewModel(private val repo: QtRepository): ViewModel() {

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    fun runClustering(params: QtParams, data: List<Point>) {
        viewModelScope.launch {
            _ui.update { it.copy(isRunning = true, progress = 0, error = null, clusters = emptyList(), noise = emptyList()) }
            try {
                val start = repo.startJob(params, data)
                val result = repo.pollUntilDone(start.jobId) { p ->
                    _ui.update { it.copy(progress = p) }
                }
                _ui.update { it.copy(isRunning = false, progress = 100, clusters = result.clusters, noise = result.noise) }
            } catch (e: Exception) {
                _ui.update { it.copy(isRunning = false, error = e.message ?: "Errore") }
            }
        }
    }
}
