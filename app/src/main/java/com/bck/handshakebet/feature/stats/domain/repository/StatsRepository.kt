package com.bck.handshakebet.feature.stats.domain.repository

import com.bck.handshakebet.feature.stats.domain.model.StatsData

/**
 * Contract for fetching the current user's computed statistics.
 */
interface StatsRepository {

    /**
     * Loads and computes stats from the current user's completed bets.
     *
     * @return [Result.success] with [StatsData] on success,
     *   or [Result.failure] with a user-friendly message on error.
     */
    suspend fun loadStats(): Result<StatsData>
}
