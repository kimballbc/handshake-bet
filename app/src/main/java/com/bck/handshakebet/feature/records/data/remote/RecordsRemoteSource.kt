package com.bck.handshakebet.feature.records.data.remote

import android.util.Log
import com.bck.handshakebet.feature.home.data.remote.SupabaseBet
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import javax.inject.Inject

private const val TAG = "BCK"

/**
 * Direct Supabase data source for records-related queries.
 *
 * Fetches completed bets for the current user. Outcome computation (WIN/DRAW/LOSS)
 * is left to [com.bck.handshakebet.feature.records.data.repository.RecordsRepositoryImpl]
 * so this class stays focused on raw I/O.
 *
 * @property postgrest Supabase Postgrest client.
 * @property auth      Supabase Auth client for reading the current user's ID.
 */
class RecordsRemoteSource @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) {

    /**
     * Returns all completed bets where the current user is either the creator
     * or the opponent, ordered newest-first.
     */
    suspend fun fetchCompletedBets(): List<SupabaseBet> {
        val userId = auth.currentUserOrNull()?.id
        Log.d(TAG, "RecordsRemoteSource.fetchCompletedBets → userId=$userId")
        if (userId == null) {
            Log.w(TAG, "RecordsRemoteSource.fetchCompletedBets ✗ no active session, returning empty")
            return emptyList()
        }
        return try {
            val result = postgrest.from("bets")
                .select {
                    filter {
                        eq("status", "completed")
                        or {
                            filter(FilterOperation("creator_id", FilterOperator.EQ, userId))
                            filter(FilterOperation("opponent_id", FilterOperator.EQ, userId))
                        }
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<SupabaseBet>()
            Log.d(TAG, "RecordsRemoteSource.fetchCompletedBets ← returned ${result.size} bets")
            result
        } catch (e: Exception) {
            Log.e(TAG, "RecordsRemoteSource.fetchCompletedBets ✗ ${e::class.simpleName}: ${e.message}", e)
            throw e
        }
    }
}
