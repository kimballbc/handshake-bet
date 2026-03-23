package com.bck.handshakebet.feature.auth.ui

import com.bck.handshakebet.core.domain.model.User

/**
 * Represents every possible state of the authentication screen.
 *
 * The screen collects this as a [kotlinx.coroutines.flow.StateFlow] from
 * [AuthViewModel] and renders accordingly. State transitions are always
 * driven by the ViewModel — the screen never mutates state directly.
 */
sealed interface AuthUiState {

    /** Initial state. No operation is in flight and no result has been received. */
    data object Idle : AuthUiState

    /** An auth operation (sign-in or sign-up) is in progress. Buttons should be disabled. */
    data object Loading : AuthUiState

    /**
     * Authentication completed successfully. The screen should navigate to Home.
     *
     * @property user The authenticated [User].
     */
    data class Success(val user: User) : AuthUiState

    /**
     * Sign-up succeeded but the user must verify their email before signing in.
     * The screen should switch to login mode and display a prompt to check inbox.
     */
    data object EmailVerificationSent : AuthUiState

    /**
     * An auth operation failed.
     *
     * @property message A user-friendly description of the failure, safe to
     *   display directly in the UI.
     */
    data class Error(val message: String) : AuthUiState
}
