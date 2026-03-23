package com.bck.handshakebet.feature.auth.domain.model

import com.bck.handshakebet.core.domain.model.User

/**
 * Represents the two possible success outcomes of a sign-up operation.
 *
 * Sign-up is distinct from sign-in in that a successful API call does not
 * always mean the user can immediately access the app — Supabase may require
 * email verification first.
 *
 * Both cases are modelled as success (not errors) because the operation
 * itself succeeded; the caller decides how to handle each outcome.
 */
sealed interface SignUpOutcome {

    /**
     * The user was created and is immediately signed in.
     * This occurs when email confirmation is disabled in the Supabase project.
     *
     * @property user The newly created and authenticated [User].
     */
    data class Success(val user: User) : SignUpOutcome

    /**
     * The user was created but must verify their email before signing in.
     * The app should prompt the user to check their inbox and switch to login mode.
     */
    data object EmailVerificationRequired : SignUpOutcome
}
