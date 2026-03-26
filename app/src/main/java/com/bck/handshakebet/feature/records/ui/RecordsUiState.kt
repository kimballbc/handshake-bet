package com.bck.handshakebet.feature.records.ui

import com.bck.handshakebet.feature.records.domain.model.CompletedBet

/**
 * UI state for the Records screen.
 *
 * Uses a sealed hierarchy so the screen can handle loading, success, and
 * error states without null-safety gymnastics.
 */
sealed interface RecordsUiState {

    /** Initial state while the records are being fetched. */
    data object Loading : RecordsUiState

    /**
     * Records loaded successfully.
     *
     * @property wins          Total bets won.
     * @property draws         Total draws.
     * @property losses        Total bets lost.
     * @property prideBalance  Net pride balance (wins − losses, weighted by wager).
     * @property completedBets Full history, newest-first.
     */
    data class Success(
        val wins: Int,
        val draws: Int,
        val losses: Int,
        val prideBalance: Int,
        val completedBets: List<CompletedBet>
    ) : RecordsUiState

    /**
     * Records failed to load.
     *
     * @property message User-facing error message.
     */
    data class Error(val message: String) : RecordsUiState
}
