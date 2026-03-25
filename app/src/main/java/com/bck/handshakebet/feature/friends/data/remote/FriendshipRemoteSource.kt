package com.bck.handshakebet.feature.friends.data.remote

import android.util.Log
import com.bck.handshakebet.feature.home.data.remote.SupabaseUser
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import javax.inject.Inject

private const val TAG = "BCK"

/**
 * Direct Supabase data source for friendship operations.
 *
 * All write operations (send, accept, reject, remove) operate on the
 * `public.friendships` table, which is protected by RLS policies ensuring
 * only authorised parties may act on each row.
 *
 * [fetchAllFriendships] intentionally loads every row where the current user
 * appears (as requester or recipient) in a single query, then pairs them with
 * display-name data from a second `public.users` query. The calling repository
 * partitions the results into friends / incoming / sent lists.
 *
 * @property postgrest Supabase PostgREST client for database queries.
 * @property auth      Supabase Auth client for reading the current session.
 */
class FriendshipRemoteSource @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) {

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns all friendship rows where the current user is the requester
     * or the recipient, paired with display-name data from `public.users`.
     *
     * Performs two network requests:
     * 1. `friendships` — fetch all rows involving the current user.
     * 2. `users`       — batch-fetch display names for every other-party UUID.
     *
     * @throws IllegalStateException if there is no active session.
     */
    suspend fun fetchAllFriendships(): Pair<List<SupabaseFriendship>, Map<String, String>> {
        val currentUserId = auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No active session")

        Log.d(TAG, "FriendshipRemoteSource.fetchAllFriendships → currentUserId=$currentUserId")

        // 1. Fetch all friendship rows for this user.
        val friendships = postgrest.from("friendships")
            .select {
                filter {
                    or {
                        filter(FilterOperation("requester_id", FilterOperator.EQ, currentUserId))
                        filter(FilterOperation("recipient_id", FilterOperator.EQ, currentUserId))
                    }
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<SupabaseFriendship>()

        Log.d(TAG, "FriendshipRemoteSource.fetchAllFriendships ← ${friendships.size} rows")

        if (friendships.isEmpty()) return Pair(emptyList(), emptyMap())

        // 2. Collect all "other party" UUIDs and batch-fetch their display names.
        val otherIds = friendships
            .map { if (it.requesterId == currentUserId) it.recipientId else it.requesterId }
            .distinct()

        // Build an IN filter using the PostgREST format: (id1,id2,id3)
        val idList = otherIds.joinToString(",")
        val users = postgrest.from("users")
            .select {
                filter {
                    filter(FilterOperation("id", FilterOperator.IN, "($idList)"))
                }
            }
            .decodeList<SupabaseUser>()

        val displayNameById = users.associate { it.id to it.displayName }
        Log.d(TAG, "FriendshipRemoteSource.fetchAllFriendships ← ${users.size} user display names loaded")

        return Pair(friendships, displayNameById)
    }

    /**
     * Returns accepted-friend user records whose display name matches [query], for
     * use in the New Bet opponent picker.
     *
     * Performs two network requests:
     * 1. `friendships` — fetch accepted rows for the current user.
     * 2. `users`       — filter by ID + display_name ILIKE.
     *
     * Returns an empty list when [query] is blank.
     */
    suspend fun searchFriends(query: String): List<SupabaseUser> {
        if (query.isBlank()) return emptyList()

        val currentUserId = auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No active session")

        Log.d(TAG, "FriendshipRemoteSource.searchFriends → query=\"$query\"")

        // 1. Get all accepted friendship rows.
        val friendships = postgrest.from("friendships")
            .select {
                filter {
                    or {
                        filter(FilterOperation("requester_id", FilterOperator.EQ, currentUserId))
                        filter(FilterOperation("recipient_id", FilterOperator.EQ, currentUserId))
                    }
                    eq("status", "accepted")
                }
            }
            .decodeList<SupabaseFriendship>()

        if (friendships.isEmpty()) {
            Log.d(TAG, "FriendshipRemoteSource.searchFriends ← 0 friends")
            return emptyList()
        }

        val friendIds = friendships.map {
            if (it.requesterId == currentUserId) it.recipientId else it.requesterId
        }.distinct()

        // 2. Filter those users by display_name ILIKE %query%.
        val idList = friendIds.joinToString(",")
        val results = postgrest.from("users")
            .select {
                filter {
                    filter(FilterOperation("id", FilterOperator.IN, "($idList)"))
                    ilike("display_name", "%$query%")
                }
                order("display_name", Order.ASCENDING)
                limit(20)
            }
            .decodeList<SupabaseUser>()

        Log.d(TAG, "FriendshipRemoteSource.searchFriends ← ${results.size} results for \"$query\"")
        return results
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /** Inserts a new `"pending"` friendship row from the current user to [recipientId]. */
    suspend fun sendFriendRequest(recipientId: String) {
        val currentUserId = auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No active session")
        Log.d(TAG, "FriendshipRemoteSource.sendFriendRequest → to=$recipientId")
        postgrest.from("friendships").insert(
            mapOf(
                "requester_id" to currentUserId,
                "recipient_id" to recipientId,
                "status"       to "pending"
            )
        )
    }

    /** Updates the friendship row [friendshipId] to `"accepted"`. */
    suspend fun acceptFriendRequest(friendshipId: String) {
        Log.d(TAG, "FriendshipRemoteSource.acceptFriendRequest → id=$friendshipId")
        postgrest.from("friendships")
            .update({ set("status", "accepted") }) {
                filter { eq("id", friendshipId) }
            }
    }

    /** Updates the friendship row [friendshipId] to `"rejected"`. */
    suspend fun rejectFriendRequest(friendshipId: String) {
        Log.d(TAG, "FriendshipRemoteSource.rejectFriendRequest → id=$friendshipId")
        postgrest.from("friendships")
            .update({ set("status", "rejected") }) {
                filter { eq("id", friendshipId) }
            }
    }

    /** Deletes the friendship row [friendshipId] (unfriend or withdraw request). */
    suspend fun removeFriendship(friendshipId: String) {
        Log.d(TAG, "FriendshipRemoteSource.removeFriendship → id=$friendshipId")
        postgrest.from("friendships").delete {
            filter { eq("id", friendshipId) }
        }
    }

    /** Returns the current user's Supabase Auth UUID, or `null` if unauthenticated. */
    fun currentUserId(): String? = auth.currentUserOrNull()?.id
}
