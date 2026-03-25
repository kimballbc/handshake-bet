package com.bck.handshakebet.feature.home.data.remote

import android.util.Log
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

private const val TAG = "BCK"

/**
 * Direct Supabase data source for bet-related queries and mutations.
 *
 * Read methods return raw [SupabaseBet] DTOs. Write methods accept minimal
 * inline payloads (see [BetInsert] and [BetStatusUpdate]). Error translation
 * and domain mapping happen in
 * [com.bck.handshakebet.feature.home.data.repository.BetRepositoryImpl].
 *
 * @property postgrest Supabase Postgrest client for database queries.
 * @property auth      Supabase Auth client for reading the current user's ID.
 */
class BetRemoteSource @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) {

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns public bets with status `active` or `completed`, ordered by
     * creation date descending, limited to the 25 most recent.
     */
    suspend fun fetchPublicBets(): List<SupabaseBet> {
        Log.d(TAG, "BetRemoteSource.fetchPublicBets → querying bets (public, active|completed, limit 25)")
        return try {
            val result = postgrest.from("bets")
                .select {
                    filter {
                        eq("is_public", true)
                        or {
                            filter(FilterOperation("status", FilterOperator.EQ, "active"))
                            filter(FilterOperation("status", FilterOperator.EQ, "completed"))
                        }
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(25)
                }
                .decodeList<SupabaseBet>()
            Log.d(TAG, "BetRemoteSource.fetchPublicBets ← returned ${result.size} bets")
            result
        } catch (e: Exception) {
            Log.e(TAG, "BetRemoteSource.fetchPublicBets ✗ ${e::class.simpleName}: ${e.message}", e)
            throw e
        }
    }

    /**
     * Returns all bets where the currently signed-in user is either the creator
     * or the opponent, ordered by creation date descending.
     */
    suspend fun fetchMyBets(): List<SupabaseBet> {
        val userId = auth.currentUserOrNull()?.id
        Log.d(TAG, "BetRemoteSource.fetchMyBets → userId=$userId")
        if (userId == null) {
            Log.w(TAG, "BetRemoteSource.fetchMyBets ✗ no active session, returning empty")
            return emptyList()
        }
        return try {
            val result = postgrest.from("bets")
                .select {
                    filter {
                        or {
                            filter(FilterOperation("creator_id", FilterOperator.EQ, userId))
                            filter(FilterOperation("opponent_id", FilterOperator.EQ, userId))
                        }
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<SupabaseBet>()
            Log.d(TAG, "BetRemoteSource.fetchMyBets ← returned ${result.size} bets")
            result
        } catch (e: Exception) {
            Log.e(TAG, "BetRemoteSource.fetchMyBets ✗ ${e::class.simpleName}: ${e.message}", e)
            throw e
        }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Inserts a new bet row. No return value is needed — the ViewModel
     * navigates to the Account screen on success, which performs a fresh fetch.
     */
    suspend fun createBet(insert: BetInsert) {
        Log.d(TAG, """
            BetRemoteSource.createBet →
              title="${insert.title}"
              description="${insert.description}"
              creatorId=${insert.creatorId}
              creatorDisplayName="${insert.creatorDisplayName}"
              opponentId=${insert.opponentId}
              opponentDisplayName="${insert.opponentDisplayName}"
              isPublic=${insert.isPublic}
              prideWagered=${insert.prideWagered}
              status=${insert.status}
        """.trimIndent())
        try {
            postgrest.from("bets").insert(insert)
            Log.d(TAG, "BetRemoteSource.createBet ← insert succeeded")
        } catch (e: Exception) {
            Log.e(TAG, "BetRemoteSource.createBet ✗ ${e::class.simpleName}: ${e.message}", e)
            throw e
        }
    }

    /** Transitions a bet's status to `active` (opponent accepts). */
    suspend fun acceptBet(betId: String) {
        Log.d(TAG, "BetRemoteSource.acceptBet → betId=$betId")
        try {
            postgrest.from("bets")
                .update({ set("status", "active") }) { filter { eq("id", betId) } }
            Log.d(TAG, "BetRemoteSource.acceptBet ← succeeded")
        } catch (e: Exception) {
            Log.e(TAG, "BetRemoteSource.acceptBet ✗ ${e::class.simpleName}: ${e.message}", e)
            throw e
        }
    }

    /** Transitions a bet's status to `rejected` (opponent declines). */
    suspend fun rejectBet(betId: String) {
        Log.d(TAG, "BetRemoteSource.rejectBet → betId=$betId")
        try {
            postgrest.from("bets")
                .update({ set("status", "rejected") }) { filter { eq("id", betId) } }
            Log.d(TAG, "BetRemoteSource.rejectBet ← succeeded")
        } catch (e: Exception) {
            Log.e(TAG, "BetRemoteSource.rejectBet ✗ ${e::class.simpleName}: ${e.message}", e)
            throw e
        }
    }

    /** Transitions a bet's status to `cancelled` (creator withdraws). */
    suspend fun cancelBet(betId: String) {
        Log.d(TAG, "BetRemoteSource.cancelBet → betId=$betId")
        try {
            postgrest.from("bets")
                .update({ set("status", "cancelled") }) { filter { eq("id", betId) } }
            Log.d(TAG, "BetRemoteSource.cancelBet ← succeeded")
        } catch (e: Exception) {
            Log.e(TAG, "BetRemoteSource.cancelBet ✗ ${e::class.simpleName}: ${e.message}", e)
            throw e
        }
    }

    /** Transitions a bet's status to `completed` and records the winner. */
    suspend fun completeBet(betId: String, winnerId: String) {
        Log.d(TAG, "BetRemoteSource.completeBet → betId=$betId, winnerId=$winnerId")
        try {
            postgrest.from("bets")
                .update({
                    set("status", "completed")
                    set("winner_id", winnerId)
                }) { filter { eq("id", betId) } }
            Log.d(TAG, "BetRemoteSource.completeBet ← succeeded")
        } catch (e: Exception) {
            Log.e(TAG, "BetRemoteSource.completeBet ✗ ${e::class.simpleName}: ${e.message}", e)
            throw e
        }
    }

    // ── Insert DTO ────────────────────────────────────────────────────────────

    /**
     * Minimal insert payload for the `bets` table.
     *
     * Excludes `id` and `created_at` — both are generated server-side by Supabase.
     */
    @Serializable
    data class BetInsert(
        @SerialName("title")                 val title: String,
        @SerialName("description")           val description: String,
        @SerialName("creator_id")            val creatorId: String,
        @SerialName("creator_display_name")  val creatorDisplayName: String,
        @SerialName("opponent_id")           val opponentId: String,
        @SerialName("opponent_display_name") val opponentDisplayName: String,
        @SerialName("status")                val status: String = "pending",
        @SerialName("is_public")             val isPublic: Boolean,
        @SerialName("pride_wagered")         val prideWagered: Int
    )
}
