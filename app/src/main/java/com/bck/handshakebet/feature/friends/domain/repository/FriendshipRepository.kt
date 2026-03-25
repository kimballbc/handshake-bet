package com.bck.handshakebet.feature.friends.domain.repository

import com.bck.handshakebet.feature.friends.domain.model.FriendshipsData
import com.bck.handshakebet.feature.home.domain.model.UserSummary

/**
 * Contract for all friendship operations.
 *
 * Callers (ViewModels) depend only on this interface, enabling straightforward
 * mocking in unit tests without touching Supabase or Android.
 *
 * All suspend functions return [Result] so errors are always explicit and
 * handled at the call site rather than propagated as unchecked exceptions.
 * Error messages inside [Result.failure] are pre-formatted for display.
 */
interface FriendshipRepository {

    /**
     * Loads all friendship data for the current user in one round trip:
     * accepted friends, incoming pending requests, and sent pending requests.
     *
     * @return [Result.success] with a [FriendshipsData] snapshot, or
     *   [Result.failure] with a user-friendly message.
     */
    suspend fun loadFriendships(): Result<FriendshipsData>

    /**
     * Sends a friend request from the current user to [recipientId].
     *
     * @return [Result.success] on success, or [Result.failure] with a
     *   user-friendly message (e.g. duplicate request, self-request).
     */
    suspend fun sendFriendRequest(recipientId: String): Result<Unit>

    /**
     * Accepts an incoming friend request identified by [friendshipId].
     *
     * Only callable when the current user is the recipient.
     *
     * @return [Result.success] on success, or [Result.failure].
     */
    suspend fun acceptFriendRequest(friendshipId: String): Result<Unit>

    /**
     * Rejects an incoming friend request identified by [friendshipId].
     *
     * @return [Result.success] on success, or [Result.failure].
     */
    suspend fun rejectFriendRequest(friendshipId: String): Result<Unit>

    /**
     * Removes an existing friendship or withdraws a sent request.
     *
     * Either party may call this regardless of status.
     *
     * @return [Result.success] on success, or [Result.failure].
     */
    suspend fun removeFriendship(friendshipId: String): Result<Unit>

    /**
     * Searches the current user's accepted friends whose display name contains
     * [query] (case-insensitive). Used in the New Bet opponent picker to restrict
     * selection to friends only.
     *
     * Returns an empty list when [query] is blank or fewer than 2 characters.
     *
     * @return [Result.success] with matching [UserSummary]s, or [Result.failure].
     */
    suspend fun searchFriends(query: String): Result<List<UserSummary>>
}
