package com.bck.handshakebet.feature.home.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import javax.inject.Inject

/**
 * Direct Supabase data source for bet-related queries.
 *
 * All methods return raw [SupabaseBet] DTOs. Error translation and domain
 * mapping happen in [com.bck.handshakebet.feature.home.data.repository.BetRepositoryImpl].
 *
 * @property postgrest Supabase Postgrest client for database queries.
 * @property auth      Supabase Auth client for reading the current user's ID.
 */
class BetRemoteSource @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) {

    /**
     * Returns all bets that are marked public and have an [active][com.bck.handshakebet.feature.home.domain.model.BetStatus.ACTIVE] status,
     * ordered by creation date descending.
     */
    suspend fun fetchPublicBets(): List<SupabaseBet> =
        postgrest.from("bets")
            .select {
                filter {
                    eq("is_public", true)
                    eq("status", "active")
                }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList()

    /**
     * Returns all bets where the currently signed-in user is either the creator
     * or the opponent, ordered by creation date descending.
     */
    suspend fun fetchMyBets(): List<SupabaseBet> {
        val userId = auth.currentUserOrNull()?.id ?: return emptyList()
        return postgrest.from("bets")
            .select {
                filter {
                    or {
                        filter(FilterOperation("creator_id", FilterOperator.EQ, userId))
                        filter(FilterOperation("opponent_id", FilterOperator.EQ, userId))
                    }
                }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList()
    }
}
