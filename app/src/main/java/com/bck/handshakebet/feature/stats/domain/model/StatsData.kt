package com.bck.handshakebet.feature.stats.domain.model

/**
 * Computed statistics for the current user.
 *
 * All values are derived in-memory from the completed bets in the `bets` table.
 *
 * @property totalCompleted      Total number of completed bets.
 * @property winRate             Wins / (wins + losses), ignoring draws. `null` if no
 *                               decisive bets have been completed.
 * @property currentStreak       Consecutive wins (positive) or losses (negative) counting
 *                               back from the most recent bet. Draws break any streak.
 * @property bestWinStreak       Longest consecutive win streak ever recorded.
 * @property averagePrideWagered Mean pride wagered across all completed bets, or `null`
 *                               if there are no completed bets.
 * @property topOpponent         The opponent with the most completed bets, or `null` if
 *                               no bets exist.
 */
data class StatsData(
    val totalCompleted: Int,
    val winRate: Float?,
    val currentStreak: Int,
    val bestWinStreak: Int,
    val averagePrideWagered: Float?,
    val topOpponent: OpponentStats?
)
