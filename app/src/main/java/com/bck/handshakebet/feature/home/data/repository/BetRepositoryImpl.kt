package com.bck.handshakebet.feature.home.data.repository

import android.util.Log
import com.bck.handshakebet.feature.home.data.remote.BetRemoteSource
import com.bck.handshakebet.feature.home.data.remote.SupabaseBet
import com.bck.handshakebet.feature.home.domain.model.Bet
import com.bck.handshakebet.feature.home.domain.model.BetStatus
import com.bck.handshakebet.feature.home.domain.repository.BetRepository
import io.github.jan.supabase.auth.Auth
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

private const val TAG = "BCK"

/**
 * Production implementation of [BetRepository].
 *
 * Delegates network calls to [BetRemoteSource], maps [SupabaseBet] DTOs to
 * [Bet] domain models, and translates exceptions into user-friendly error
 * messages wrapped in [Result.failure].
 *
 * [auth] is injected here (rather than through [BetRemoteSource]) so that
 * [createBet] can read the current user's display name from Auth metadata
 * without adding a round-trip to the `users` table.
 *
 * @property remoteSource The Supabase data source for raw bet data.
 * @property auth         Supabase Auth client for current-user metadata.
 */
class BetRepositoryImpl @Inject constructor(
    private val remoteSource: BetRemoteSource,
    private val auth: Auth
) : BetRepository {

    // ── Read ──────────────────────────────────────────────────────────────────

    override suspend fun fetchPublicBets(): Result<List<Bet>> {
        Log.d(TAG, "BetRepositoryImpl.fetchPublicBets →")
        return runCatching {
            remoteSource.fetchPublicBets().map { it.toDomain() }
        }.mapError().also { result ->
            if (result.isSuccess) Log.d(TAG, "BetRepositoryImpl.fetchPublicBets ← ${result.getOrNull()?.size} bets")
            else Log.e(TAG, "BetRepositoryImpl.fetchPublicBets ✗ ${result.exceptionOrNull()?.message}")
        }
    }

    override suspend fun fetchMyBets(): Result<List<Bet>> {
        Log.d(TAG, "BetRepositoryImpl.fetchMyBets →")
        return runCatching {
            remoteSource.fetchMyBets().map { it.toDomain() }
        }.mapError().also { result ->
            if (result.isSuccess) Log.d(TAG, "BetRepositoryImpl.fetchMyBets ← ${result.getOrNull()?.size} bets")
            else Log.e(TAG, "BetRepositoryImpl.fetchMyBets ✗ ${result.exceptionOrNull()?.message}")
        }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    override suspend fun createBet(
        title: String,
        description: String,
        isPublic: Boolean,
        opponentId: String,
        opponentDisplayName: String,
        prideWagered: Int
    ): Result<Unit> {
        Log.d(TAG, "BetRepositoryImpl.createBet → title=\"$title\", opponent=$opponentDisplayName, wager=$prideWagered, public=$isPublic")
        return runCatching {
        val currentUser = auth.currentUserOrNull()
            ?: error("Cannot create a bet — no active session.")
        val creatorDisplayName = currentUser.userMetadata
            ?.get("display_name")
            ?.jsonPrimitive
            ?.contentOrNull
            ?.removeSurrounding("\"")
            ?: currentUser.email
            ?: currentUser.id
        Log.d(TAG, "BetRepositoryImpl.createBet  creatorId=${currentUser.id}, creatorDisplayName=\"$creatorDisplayName\"")
        val insert = BetRemoteSource.BetInsert(
            title = title,
            description = description,
            creatorId = currentUser.id,
            creatorDisplayName = creatorDisplayName,
            opponentId = opponentId,
            opponentDisplayName = opponentDisplayName,
            isPublic = isPublic,
            prideWagered = prideWagered
        )
        remoteSource.createBet(insert)
    }.mapError().also { result ->
        if (result.isSuccess) Log.d(TAG, "BetRepositoryImpl.createBet ← success")
        else Log.e(TAG, "BetRepositoryImpl.createBet ✗ ${result.exceptionOrNull()?.message}", result.exceptionOrNull())
    }
    }

    override suspend fun acceptBet(betId: String): Result<Unit> {
        Log.d(TAG, "BetRepositoryImpl.acceptBet → betId=$betId")
        return runCatching { remoteSource.acceptBet(betId) }.mapError().also {
            if (it.isFailure) Log.e(TAG, "BetRepositoryImpl.acceptBet ✗ ${it.exceptionOrNull()?.message}")
        }
    }

    override suspend fun rejectBet(betId: String): Result<Unit> {
        Log.d(TAG, "BetRepositoryImpl.rejectBet → betId=$betId")
        return runCatching { remoteSource.rejectBet(betId) }.mapError().also {
            if (it.isFailure) Log.e(TAG, "BetRepositoryImpl.rejectBet ✗ ${it.exceptionOrNull()?.message}")
        }
    }

    override suspend fun cancelBet(betId: String): Result<Unit> {
        Log.d(TAG, "BetRepositoryImpl.cancelBet → betId=$betId")
        return runCatching { remoteSource.cancelBet(betId) }.mapError().also {
            if (it.isFailure) Log.e(TAG, "BetRepositoryImpl.cancelBet ✗ ${it.exceptionOrNull()?.message}")
        }
    }

    override suspend fun completeBet(betId: String, winnerId: String): Result<Unit> {
        Log.d(TAG, "BetRepositoryImpl.completeBet → betId=$betId, winnerId=$winnerId")
        return runCatching { remoteSource.completeBet(betId, winnerId) }.mapError().also {
            if (it.isFailure) Log.e(TAG, "BetRepositoryImpl.completeBet ✗ ${it.exceptionOrNull()?.message}")
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    /**
     * Maps a [SupabaseBet] DTO to the [Bet] domain model.
     */
    private fun SupabaseBet.toDomain(): Bet = Bet(
        id = id,
        title = title,
        description = description,
        creatorId = creatorId,
        creatorDisplayName = creatorDisplayName,
        opponentId = opponentId,
        opponentDisplayName = opponentDisplayName,
        status = BetStatus.fromString(status),
        isPublic = isPublic,
        prideWagered = prideWagered,
        winnerId = winnerId,
        createdAt = createdAt
    )

    /**
     * Translates a [Result] failure exception into a user-facing error message.
     * The original exception is preserved as the cause.
     */
    private fun <T> Result<T>.mapError(): Result<T> = this.recoverCatching { cause ->
        throw Exception(cause.toFriendlyMessage(), cause)
    }

    /**
     * Converts raw Supabase exception messages to short, human-readable strings.
     */
    private fun Throwable.toFriendlyMessage(): String {
        val msg = message?.lowercase() ?: return "Something went wrong. Please try again."
        return when {
            "network" in msg || "connect" in msg || "unable to resolve" in msg ->
                "No internet connection. Please check your network."
            "timeout" in msg ->
                "Request timed out. Please try again."
            "jwt" in msg || "unauthori" in msg || "forbidden" in msg ->
                "Your session has expired. Please sign in again."
            "row-level security" in msg || "permission" in msg ->
                "You don't have permission to perform this action."
            "no active session" in msg ->
                "You are not signed in. Please sign in and try again."
            else -> "Something went wrong. Please try again."
        }
    }
}
