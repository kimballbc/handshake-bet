package com.bck.handshakebet.feature.auth.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject

/**
 * Wraps all direct Supabase Auth SDK calls for the authentication feature.
 *
 * Keeping raw SDK calls isolated here means [AuthRepositoryImpl] never
 * imports Supabase types directly, and tests for the repository can mock
 * this class rather than the SDK itself.
 *
 * @property auth The [Auth] plugin instance provided by [NetworkModule].
 */
class AuthRemoteSource @Inject constructor(
    private val auth: Auth
) {

    /**
     * Signs in with email and password.
     *
     * On success, the Supabase SDK updates the internal session automatically.
     * Throws a [io.github.jan.supabase.exceptions.RestException] on invalid
     * credentials or an unverified email.
     *
     * @return The [UserInfo] for the newly authenticated user.
     * @throws Exception if sign-in fails for any reason.
     */
    suspend fun signIn(email: String, password: String): UserInfo {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return auth.currentUserOrNull()
            ?: throw Exception("Sign in succeeded but no session was created")
    }

    /**
     * Registers a new user with email, password, and display name.
     *
     * The display name is stored in the user's metadata and later read back
     * when mapping to the [com.bck.handshakebet.core.domain.model.User] domain model.
     *
     * @return [UserInfo] if the user is immediately confirmed, or null if
     *   email verification is required before the account is active.
     * @throws Exception if the email is already registered or the request fails.
     */
    suspend fun signUp(email: String, password: String, displayName: String): UserInfo? {
        return auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject {
                put("display_name", JsonPrimitive(displayName))
            }
        }
    }

    /**
     * Signs out the current user and clears the local session.
     *
     * @throws Exception if the sign-out request fails.
     */
    suspend fun signOut() = auth.signOut()

    /**
     * Returns the currently authenticated [UserInfo], or null if no session exists.
     * This is a synchronous, local read — no network request is made.
     */
    fun currentUserOrNull(): UserInfo? = auth.currentUserOrNull()

    /**
     * Returns the current session, or null if the user is not signed in.
     * This is a synchronous, local read — no network request is made.
     */
    fun currentSessionOrNull() = auth.currentSessionOrNull()
}
