package com.bck.handshakebet.feature.auth.domain.repository

import com.bck.handshakebet.core.domain.model.User
import com.bck.handshakebet.feature.auth.domain.model.SignUpOutcome

/**
 * Contract for all authentication operations.
 *
 * Implementations live in the data layer ([com.bck.handshakebet.feature.auth.data.repository]).
 * Callers (ViewModels) depend only on this interface, enabling straightforward
 * mocking in unit tests without touching Supabase or Android.
 *
 * All suspend functions return [Result] so errors are always explicit and
 * handled at the call site rather than propagated as unchecked exceptions.
 */
interface AuthRepository {

    /**
     * Authenticates a user with email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @return [Result.success] containing the signed-in [User], or
     *   [Result.failure] with a user-friendly error message.
     */
    suspend fun signIn(email: String, password: String): Result<User>

    /**
     * Registers a new user with email, password, and a display name.
     *
     * @param email The user's email address.
     * @param password The user's chosen password.
     * @param displayName The name to show across the app.
     * @return [Result.success] containing a [SignUpOutcome] (either immediate
     *   success or email verification required), or [Result.failure] with a
     *   user-friendly error message.
     */
    suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<SignUpOutcome>

    /**
     * Signs out the currently authenticated user and clears their local session.
     *
     * @return [Result.success] on success, or [Result.failure] if the request failed.
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Returns whether a valid session currently exists.
     *
     * This is a fast, local check — it does not make a network request.
     */
    suspend fun isSignedIn(): Boolean

    /**
     * Returns the currently authenticated [User], or null if no session exists.
     *
     * This is a synchronous, local check — it does not make a network request.
     */
    fun getCurrentUser(): User?
}
