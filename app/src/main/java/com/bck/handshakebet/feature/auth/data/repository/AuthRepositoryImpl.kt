package com.bck.handshakebet.feature.auth.data.repository

import com.bck.handshakebet.core.domain.model.User
import com.bck.handshakebet.feature.auth.data.remote.AuthRemoteSource
import com.bck.handshakebet.feature.auth.domain.model.SignUpOutcome
import com.bck.handshakebet.feature.auth.domain.repository.AuthRepository
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import android.util.Log
import javax.inject.Inject

/**
 * Supabase-backed implementation of [AuthRepository].
 *
 * Responsibilities:
 * - Delegates raw SDK calls to [AuthRemoteSource].
 * - Maps Supabase [UserInfo] objects to the [User] domain model.
 * - Translates SDK exceptions into user-friendly [Result.failure] messages
 *   so callers never need to parse raw error strings.
 *
 * @property remoteSource The data source wrapping Supabase Auth SDK calls.
 */
class AuthRepositoryImpl @Inject constructor(
    private val remoteSource: AuthRemoteSource
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val userInfo = remoteSource.signIn(email, password)
            Result.success(userInfo.toDomainUser())
        } catch (e: Exception) {
            Result.failure(Exception(toFriendlyMessage(e)))
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<SignUpOutcome> {
        return try {
            val userInfo = remoteSource.signUp(email, password, displayName)
            val outcome = if (userInfo != null) {
                SignUpOutcome.Success(userInfo.toDomainUser())
            } else {
                SignUpOutcome.EmailVerificationRequired
            }
            Result.success(outcome)
        } catch (e: Exception) {
            Result.failure(Exception(toFriendlyMessage(e)))
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            remoteSource.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(toFriendlyMessage(e)))
        }
    }

    override suspend fun isSignedIn(): Boolean =
        remoteSource.currentSessionOrNull() != null

    override fun getCurrentUser(): User? =
        remoteSource.currentUserOrNull()?.toDomainUser()

    // ── Mapping ───────────────────────────────────────────────────────────────

    /**
     * Maps a Supabase [UserInfo] to the [User] domain model.
     *
     * The display name is read from `user_metadata`, where it was stored during
     * sign-up. If absent, falls back to the email address so the app always has
     * a non-null display name to show.
     */
    private fun UserInfo.toDomainUser(): User = User(
        id = id,
        email = email ?: "",
        displayName = userMetadata
            ?.get("display_name")
            ?.jsonPrimitive
            ?.contentOrNull
            ?.removeSurrounding("\"")
            ?: email
            ?: id
    )

    // ── Error handling ────────────────────────────────────────────────────────

    /**
     * Converts raw Supabase/network exceptions into user-facing messages.
     *
     * Matching is done on substrings of [Throwable.message] because the
     * Supabase Kotlin SDK wraps HTTP errors as plain exceptions with the
     * server's JSON message embedded in the string.
     */
    private fun toFriendlyMessage(e: Throwable): String {
        Log.e("AUTH", "Auth error: ${e.message}", e)
        return when {
        e.message?.contains("already registered", ignoreCase = true) == true ||
        e.message?.contains("already exists", ignoreCase = true) == true ->
            "This email is already registered"

        e.message?.contains("invalid", ignoreCase = true) == true &&
        e.message?.contains("credentials", ignoreCase = true) == true ||
        e.message?.contains("password", ignoreCase = true) == true ->
            "Incorrect email or password"

        e.message?.contains("verify", ignoreCase = true) == true ||
        e.message?.contains("confirmed", ignoreCase = true) == true ->
            "Please verify your email before signing in"

        e.message?.contains("network", ignoreCase = true) == true ||
        e.message?.contains("timeout", ignoreCase = true) == true ->
            "Check your internet connection and try again"

        else -> "Something went wrong. Please try again"
        }
    }
}
