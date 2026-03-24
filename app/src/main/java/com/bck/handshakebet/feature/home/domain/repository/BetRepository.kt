package com.bck.handshakebet.feature.home.domain.repository

import com.bck.handshakebet.feature.home.domain.model.Bet

/**
 * Contract for all bet-related data operations.
 *
 * Implementations retrieve bets from the Supabase backend and map them to the
 * [Bet] domain model. All methods return [Result] so callers handle errors
 * explicitly without relying on exceptions.
 *
 * Read methods are defined here (Phase 2). Mutation methods (create, accept,
 * reject, complete) will be added in Phase 3.
 */
interface BetRepository {

    /**
     * Fetches all publicly visible, active bets.
     *
     * @return [Result.success] with a list of public [Bet]s on success,
     *   or [Result.failure] with a user-friendly message on error.
     */
    suspend fun fetchPublicBets(): Result<List<Bet>>

    /**
     * Fetches all bets where the currently signed-in user is either the
     * creator or the opponent.
     *
     * @return [Result.success] with a list of the user's [Bet]s on success,
     *   or [Result.failure] with a user-friendly message on error.
     */
    suspend fun fetchMyBets(): Result<List<Bet>>
}
