package com.bck.handshakebet.feature.home.domain.repository

import com.bck.handshakebet.feature.home.domain.model.UserSummary

/**
 * Contract for user-lookup operations needed during bet creation.
 *
 * Phase 5 note: [searchUsers] currently searches all registered users.
 * Once friends are introduced it should be scoped to the caller's friends list.
 */
interface UserRepository {

    /**
     * Searches for users whose display name contains [query] (case-insensitive).
     *
     * Returns an empty list when [query] is blank. Results are limited to
     * 20 entries ordered alphabetically by display name.
     *
     * @return [Result.success] with a list of matching [UserSummary]s,
     *   or [Result.failure] with a user-friendly message on error.
     */
    suspend fun searchUsers(query: String): Result<List<UserSummary>>

    /**
     * Returns a [UserSummary] for the currently signed-in user, or `null`
     * if no session is active. Used to populate [creator_display_name] when
     * creating a bet without a separate network call.
     */
    fun getCurrentUserSummary(): UserSummary?
}
