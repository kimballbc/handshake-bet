package com.bck.handshakebet.feature.records.data.repository

import android.util.Log
import com.bck.handshakebet.feature.home.data.remote.SupabaseBet
import com.bck.handshakebet.feature.records.data.remote.RecordsRemoteSource
import com.bck.handshakebet.feature.records.domain.model.BetOutcome
import com.bck.handshakebet.feature.records.domain.model.CompletedBet
import com.bck.handshakebet.feature.records.domain.model.RecordsData
import com.bck.handshakebet.feature.records.domain.repository.RecordsRepository
import io.github.jan.supabase.auth.Auth
import javax.inject.Inject

private const val TAG = "BCK"

/** Sentinel stored in `winner_id` when a bet is declared a draw. */
private const val DRAW_SENTINEL = "draw"

/**
 * Production implementation of [RecordsRepository].
 *
 * Computes wins/draws/losses and pride balance in-memory from the raw
 * [SupabaseBet] DTOs — the `user_records` table is not used for the POC.
 *
 * Outcome rules (relative to the current user):
 * - `winner_id == currentUserId` → WIN
 * - `winner_id == "draw"`        → DRAW
 * - anything else                → LOSS
 *
 * Pride balance = Σ(prideWagered for wins) − Σ(prideWagered for losses).
 *
 * @property remoteSource Supabase data source for fetching completed bets.
 * @property auth         Provides the current user's ID.
 */
class RecordsRepositoryImpl @Inject constructor(
    private val remoteSource: RecordsRemoteSource,
    private val auth: Auth
) : RecordsRepository {

    override suspend fun loadRecords(): Result<RecordsData> {
        Log.d(TAG, "RecordsRepositoryImpl.loadRecords →")
        return runCatching {
            val userId = auth.currentUserOrNull()?.id ?: ""
            val bets = remoteSource.fetchCompletedBets()

            val completedBets = bets.map { it.toCompletedBet(userId) }

            var wins = 0; var draws = 0; var losses = 0; var prideBalance = 0
            completedBets.forEach { bet ->
                when (bet.outcome) {
                    BetOutcome.WIN  -> { wins++;   prideBalance += bet.prideWagered }
                    BetOutcome.DRAW -> { draws++ }
                    BetOutcome.LOSS -> { losses++; prideBalance -= bet.prideWagered }
                }
            }

            RecordsData(
                wins          = wins,
                draws         = draws,
                losses        = losses,
                prideBalance  = prideBalance,
                completedBets = completedBets
            )
        }.recoverCatching { e ->
            Log.e(TAG, "RecordsRepositoryImpl.loadRecords ✗ ${e::class.simpleName}: ${e.message}", e)
            throw Exception("Could not load your records. Please try again.")
        }.also { result ->
            if (result.isSuccess) {
                val d = result.getOrNull()!!
                Log.d(TAG, "RecordsRepositoryImpl.loadRecords ← W${d.wins}/D${d.draws}/L${d.losses} balance=${d.prideBalance}")
            }
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private fun SupabaseBet.toCompletedBet(currentUserId: String): CompletedBet {
        val outcome = when {
            winnerId == currentUserId -> BetOutcome.WIN
            winnerId == DRAW_SENTINEL -> BetOutcome.DRAW
            else                      -> BetOutcome.LOSS
        }
        // Show the other participant's name relative to the current user
        val opponentName = if (creatorId == currentUserId) {
            opponentDisplayName ?: "Opponent"
        } else {
            creatorDisplayName
        }
        return CompletedBet(
            id                   = id,
            title                = title,
            opponentDisplayName  = opponentName,
            prideWagered         = prideWagered,
            outcome              = outcome,
            createdAt            = createdAt
        )
    }
}
