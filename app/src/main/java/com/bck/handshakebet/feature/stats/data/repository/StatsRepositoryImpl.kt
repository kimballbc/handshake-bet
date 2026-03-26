package com.bck.handshakebet.feature.stats.data.repository

import android.util.Log
import com.bck.handshakebet.feature.home.data.remote.SupabaseBet
import com.bck.handshakebet.feature.records.data.remote.RecordsRemoteSource
import com.bck.handshakebet.feature.stats.domain.model.OpponentStats
import com.bck.handshakebet.feature.stats.domain.model.StatsData
import com.bck.handshakebet.feature.stats.domain.repository.StatsRepository
import io.github.jan.supabase.auth.Auth
import javax.inject.Inject

private const val TAG        = "BCK"
private const val DRAW_SENTINEL = "draw"

/**
 * Production implementation of [StatsRepository].
 *
 * Reuses [RecordsRemoteSource] to fetch completed bets, then computes all stats
 * in-memory so there are no additional network round-trips.
 *
 * **Win rate** = wins / (wins + losses). Draws are excluded from the denominator
 * because they aren't decisive outcomes.
 *
 * **Streaks** are computed chronologically (oldest → newest). Draws reset any
 * active streak to zero.
 *
 * **Top opponent** = the opponent with the most completed bets. Ties are broken
 * by display name alphabetically.
 *
 * @property remoteSource Provides the completed bet rows.
 * @property auth         Resolves the current user's ID.
 */
class StatsRepositoryImpl @Inject constructor(
    private val remoteSource: RecordsRemoteSource,
    private val auth: Auth
) : StatsRepository {

    override suspend fun loadStats(): Result<StatsData> {
        Log.d(TAG, "StatsRepositoryImpl.loadStats →")
        return runCatching {
            val userId = auth.currentUserOrNull()?.id ?: ""
            // Fetch newest-first; reverse for chronological streak calculation.
            val bets   = remoteSource.fetchCompletedBets()

            if (bets.isEmpty()) {
                return@runCatching StatsData(
                    totalCompleted      = 0,
                    winRate             = null,
                    currentStreak       = 0,
                    bestWinStreak       = 0,
                    averagePrideWagered = null,
                    topOpponent         = null
                )
            }

            var wins = 0; var losses = 0
            var bestStreak = 0; var runningStreak = 0
            var currentStreak = 0

            // Iterate oldest-first for streak math
            val chronological = bets.reversed()
            chronological.forEach { bet ->
                when {
                    bet.winnerId == userId        -> {
                        wins++
                        runningStreak = if (runningStreak >= 0) runningStreak + 1 else 1
                        if (runningStreak > bestStreak) bestStreak = runningStreak
                    }
                    bet.winnerId == DRAW_SENTINEL -> runningStreak = 0
                    else                          -> {
                        losses++
                        runningStreak = if (runningStreak <= 0) runningStreak - 1 else -1
                    }
                }
            }
            // Current streak = the streak as of the most recent bet
            currentStreak = runningStreak

            val winRate = if (wins + losses > 0) wins.toFloat() / (wins + losses) else null
            val avgWager = bets.map { it.prideWagered }.average().toFloat()

            val topOpponent = bets
                .groupBy { it.opponentName(userId) }
                .mapValues { (name, group) ->
                    val w = group.count { it.winnerId == userId }
                    val l = group.count { it.winnerId != userId && it.winnerId != DRAW_SENTINEL }
                    OpponentStats(
                        displayName = name,
                        totalBets   = group.size,
                        wins        = w,
                        losses      = l
                    )
                }
                .values
                .maxWithOrNull(compareByDescending<OpponentStats> { it.totalBets }
                    .thenBy { it.displayName })

            StatsData(
                totalCompleted      = bets.size,
                winRate             = winRate,
                currentStreak       = currentStreak,
                bestWinStreak       = bestStreak,
                averagePrideWagered = avgWager,
                topOpponent         = topOpponent
            )
        }.recoverCatching { e ->
            Log.e(TAG, "StatsRepositoryImpl.loadStats ✗ ${e::class.simpleName}: ${e.message}", e)
            throw Exception("Could not load your stats. Please try again.")
        }.also { result ->
            if (result.isSuccess) {
                val d = result.getOrNull()!!
                Log.d(TAG, "StatsRepositoryImpl.loadStats ← total=${d.totalCompleted} winRate=${d.winRate} streak=${d.currentStreak}")
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns the display name of the other participant relative to [currentUserId]. */
    private fun SupabaseBet.opponentName(currentUserId: String): String =
        if (creatorId == currentUserId) opponentDisplayName ?: "Opponent"
        else creatorDisplayName
}
