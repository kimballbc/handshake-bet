package com.bck.handshakebet.feature.stats.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bck.handshakebet.feature.stats.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Stats screen.
 *
 * Loads computed statistics on creation and exposes [refresh] for manual reload.
 *
 * @property statsRepository Data source for user statistics.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun refresh() = loadStats()

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.value = StatsUiState.Loading
            statsRepository.loadStats()
                .onSuccess { data ->
                    _uiState.value = StatsUiState.Success(
                        totalCompleted      = data.totalCompleted,
                        winRate             = data.winRate,
                        currentStreak       = data.currentStreak,
                        bestWinStreak       = data.bestWinStreak,
                        averagePrideWagered = data.averagePrideWagered,
                        topOpponent         = data.topOpponent
                    )
                }
                .onFailure { e ->
                    _uiState.value = StatsUiState.Error(
                        e.message ?: "Could not load your stats."
                    )
                }
        }
    }
}
