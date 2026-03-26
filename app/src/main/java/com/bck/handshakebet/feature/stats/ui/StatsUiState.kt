package com.bck.handshakebet.feature.stats.ui

import com.bck.handshakebet.feature.stats.domain.model.OpponentStats

sealed interface StatsUiState {

    data object Loading : StatsUiState

    data class Success(
        val totalCompleted: Int,
        val winRate: Float?,
        val currentStreak: Int,
        val bestWinStreak: Int,
        val averagePrideWagered: Float?,
        val topOpponent: OpponentStats?
    ) : StatsUiState

    data class Error(val message: String) : StatsUiState
}
