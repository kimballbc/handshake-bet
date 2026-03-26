package com.bck.handshakebet.feature.records.domain.model

/**
 * Aggregated records and history for the current user.
 *
 * @property wins          Total number of bets won.
 * @property draws         Total number of bets that ended in a draw.
 * @property losses        Total number of bets lost.
 * @property prideBalance  Net pride balance (wins * prideWagered − losses * prideWagered).
 * @property completedBets Full history of completed bets, newest-first.
 */
data class RecordsData(
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val prideBalance: Int,
    val completedBets: List<CompletedBet>
)
