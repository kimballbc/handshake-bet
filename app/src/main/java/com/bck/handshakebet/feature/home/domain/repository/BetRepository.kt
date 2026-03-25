package com.bck.handshakebet.feature.home.domain.repository

import com.bck.handshakebet.feature.home.domain.model.Bet // used by fetchPublicBets/fetchMyBets

/**
 * Contract for all bet-related data operations.
 *
 * Implementations retrieve bets from the Supabase backend and map them to the
 * [Bet] domain model. All methods return [Result] so callers handle errors
 * explicitly without relying on exceptions.
 *
 * Read methods were added in Phase 2. Mutation methods (create, accept, reject,
 * complete, cancel) were added in Phase 3.
 */
interface BetRepository {

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Fetches public bets with status `active` or `completed`, ordered
     * newest-first, limited to 25 results.
     *
     * @return [Result.success] with a list of public [Bet]s on success,
     *   or [Result.failure] with a user-friendly message on error.
     */
    suspend fun fetchPublicBets(): Result<List<Bet>>

    /**
     * Fetches all bets where the currently signed-in user is either the
     * creator or the opponent, ordered newest-first.
     *
     * @return [Result.success] with a list of the user's [Bet]s,
     *   or [Result.failure] with a user-friendly message on error.
     */
    suspend fun fetchMyBets(): Result<List<Bet>>

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Creates a new bet initiated by the currently signed-in user.
     *
     * The bet starts with [com.bck.handshakebet.feature.home.domain.model.BetStatus.PENDING]
     * status and awaits the opponent's response. On success the caller navigates
     * to the Account screen, which performs a fresh fetch — no created [Bet]
     * object is returned.
     *
     * @param title               Short description of the wager.
     * @param description         Optional longer terms of the bet.
     * @param isPublic            Whether the bet appears in the public feed.
     * @param opponentId          Supabase UUID of the challenged user.
     * @param opponentDisplayName Display name of the opponent (denormalised).
     * @param prideWagered        Amount of pride wagered (1–100).
     * @return [Result.success] on success, or [Result.failure] on error.
     */
    suspend fun createBet(
        title: String,
        description: String,
        isPublic: Boolean,
        opponentId: String,
        opponentDisplayName: String,
        prideWagered: Int
    ): Result<Unit>

    /**
     * Accepts a pending bet, transitioning its status to
     * [com.bck.handshakebet.feature.home.domain.model.BetStatus.ACTIVE].
     * Only the bet's opponent may call this.
     */
    suspend fun acceptBet(betId: String): Result<Unit>

    /**
     * Rejects a pending bet, transitioning its status to
     * [com.bck.handshakebet.feature.home.domain.model.BetStatus.REJECTED].
     * Only the bet's opponent may call this.
     */
    suspend fun rejectBet(betId: String): Result<Unit>

    /**
     * Cancels a pending bet before the opponent responds, transitioning its
     * status to [com.bck.handshakebet.feature.home.domain.model.BetStatus.CANCELLED].
     * Only the bet's creator may call this.
     */
    suspend fun cancelBet(betId: String): Result<Unit>

    /**
     * Completes an active bet and records the winner, transitioning its status
     * to [com.bck.handshakebet.feature.home.domain.model.BetStatus.COMPLETED].
     * Either party may declare the winner.
     *
     * @param betId    UUID of the bet to complete.
     * @param winnerId Supabase UUID of the winning user.
     */
    suspend fun completeBet(betId: String, winnerId: String): Result<Unit>
}
