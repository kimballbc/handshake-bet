package com.bck.handshakebet.feature.home.data.remote

import android.util.Log
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

private const val TAG = "BCK"

/**
 * Direct Supabase data source for user-lookup operations.
 *
 * Used during bet creation to let the challenger search for an opponent by
 * display name. All results are raw [SupabaseUser] DTOs — mapping to domain
 * models happens in [com.bck.handshakebet.feature.home.data.repository.UserRepositoryImpl].
 *
 * Phase 5 note: [searchUsers] currently searches all registered users.
 * Restrict to the caller's friends list once Phase 5 (Friends) lands.
 *
 * @property postgrest Supabase Postgrest client for database queries.
 * @property auth      Supabase Auth client for reading the current session.
 */
class UserRemoteSource @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) {

    /**
     * Returns up to 20 users whose `display_name` contains [query] (case-insensitive),
     * sorted alphabetically. Returns an empty list when [query] is blank.
     *
     * TODO(Phase 5): Restrict results to the caller's friends list.
     */
    suspend fun searchUsers(query: String): List<SupabaseUser> {
        if (query.isBlank()) return emptyList()
        val currentUserId = auth.currentUserOrNull()?.id
        Log.d(TAG, "UserRemoteSource.searchUsers → query=\"$query\", currentUserId=$currentUserId")
        return try {
            val result = postgrest.from("users")
                .select {
                    filter {
                        ilike("display_name", "%$query%")
                        // Exclude the current user from search results so they
                        // cannot challenge themselves.
                        currentUserId?.let { neq("id", it) }
                    }
                    order("display_name", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                    limit(20)
                }
                .decodeList<SupabaseUser>()
            Log.d(TAG, "UserRemoteSource.searchUsers ← ${result.size} results for \"$query\"")
            result
        } catch (e: Exception) {
            Log.e(TAG, "UserRemoteSource.searchUsers ✗ ${e::class.simpleName}: ${e.message}", e)
            throw e
        }
    }

    /**
     * Returns the display name stored in Supabase Auth metadata for the
     * currently signed-in user, or `null` if there is no active session.
     *
     * This avoids a round-trip to `public.users` when we only need the
     * creator's display name for a new bet.
     */
    fun currentUserDisplayName(): String? {
        val name = auth.currentUserOrNull()
            ?.userMetadata
            ?.get("display_name")
            ?.jsonPrimitive
            ?.contentOrNull
            ?.removeSurrounding("\"")
        Log.d(TAG, "UserRemoteSource.currentUserDisplayName → \"$name\"")
        return name
    }

    /** Returns the current user's Supabase Auth UUID, or `null` if unauthenticated. */
    fun currentUserId(): String? {
        val id = auth.currentUserOrNull()?.id
        Log.d(TAG, "UserRemoteSource.currentUserId → $id")
        return id
    }
}
