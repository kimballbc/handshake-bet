package com.bck.handshakebet.feature.home.data.repository

import com.bck.handshakebet.feature.home.data.remote.BetRemoteSource
import com.bck.handshakebet.feature.home.data.remote.SupabaseBet
import com.bck.handshakebet.feature.home.domain.model.Bet
import com.bck.handshakebet.feature.home.domain.model.BetStatus
import com.bck.handshakebet.feature.home.domain.repository.BetRepository
import javax.inject.Inject

/**
 * Production implementation of [BetRepository].
 *
 * Delegates network calls to [BetRemoteSource], maps [SupabaseBet] DTOs to
 * [Bet] domain models, and translates exceptions into user-friendly error
 * messages wrapped in [Result.failure].
 *
 * @property remoteSource The Supabase data source for raw bet data.
 */
class BetRepositoryImpl @Inject constructor(
    private val remoteSource: BetRemoteSource
) : BetRepository {

    override suspend fun fetchPublicBets(): Result<List<Bet>> = runCatching {
        remoteSource.fetchPublicBets().map { it.toDomain() }
    }.mapError()

    override suspend fun fetchMyBets(): Result<List<Bet>> = runCatching {
        remoteSource.fetchMyBets().map { it.toDomain() }
    }.mapError()

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
                "You don't have permission to view this content."
            else -> "Something went wrong. Please try again."
        }
    }
}
