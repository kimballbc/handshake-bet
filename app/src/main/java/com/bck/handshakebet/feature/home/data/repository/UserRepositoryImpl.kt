package com.bck.handshakebet.feature.home.data.repository

import com.bck.handshakebet.feature.home.data.remote.UserRemoteSource
import com.bck.handshakebet.feature.home.domain.model.UserSummary
import com.bck.handshakebet.feature.home.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Production implementation of [UserRepository].
 *
 * Delegates to [UserRemoteSource] and maps [com.bck.handshakebet.feature.home.data.remote.SupabaseUser]
 * DTOs to [UserSummary] domain models. Exceptions are translated into
 * user-friendly messages wrapped in [Result.failure].
 *
 * @property remoteSource The data source wrapping Supabase user queries.
 */
class UserRepositoryImpl @Inject constructor(
    private val remoteSource: UserRemoteSource
) : UserRepository {

    override suspend fun searchUsers(query: String): Result<List<UserSummary>> = runCatching {
        remoteSource.searchUsers(query).map { UserSummary(id = it.id, displayName = it.displayName) }
    }.recoverCatching { cause ->
        throw Exception(toFriendlyMessage(cause), cause)
    }

    override suspend fun getCurrentUserSummary(): Result<UserSummary?> = runCatching {
        val id = remoteSource.currentUserId() ?: return@runCatching null
        val displayName = remoteSource.fetchDisplayName(id) ?: return@runCatching null
        UserSummary(id = id, displayName = displayName)
    }.recoverCatching { cause ->
        throw Exception(toFriendlyMessage(cause), cause)
    }

    // ── Error handling ────────────────────────────────────────────────────────

    private fun toFriendlyMessage(cause: Throwable): String {
        val msg = cause.message?.lowercase() ?: return "Something went wrong. Please try again."
        return when {
            "network" in msg || "connect" in msg -> "No internet connection. Please check your network."
            "timeout" in msg                     -> "Request timed out. Please try again."
            "jwt" in msg || "unauthori" in msg   -> "Your session has expired. Please sign in again."
            else                                 -> "Something went wrong. Please try again."
        }
    }
}
