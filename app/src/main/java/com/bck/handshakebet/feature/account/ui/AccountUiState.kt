package com.bck.handshakebet.feature.account.ui

import com.bck.handshakebet.feature.home.domain.model.Bet

/**
 * UI state for the Account (Home) screen.
 *
 * Bets are pre-bucketed into four sections so the screen composable contains
 * no filtering logic:
 * - [pendingForMe]   — Bets where I am the opponent and status is PENDING.
 *                      These require an immediate response (accept / reject).
 * - [myPendingSent]  — Bets I created that are awaiting the opponent's response.
 *                      I can cancel these.
 * - [activeBets]     — Bets with ACTIVE status involving me. I can complete these.
 * - [history]        — COMPLETED, REJECTED, and CANCELLED bets for reference.
 *
 * [isPerformingAction] is `true` while an accept / reject / complete / cancel
 * network call is in-flight. It disables all action controls to prevent double-taps.
 */
sealed interface AccountUiState {

    /** Initial load in progress. */
    data object Loading : AccountUiState

    /** Initial load failed. */
    data class Error(val message: String) : AccountUiState

    /** Bets loaded successfully. */
    data class Success(
        val currentUserId: String,
        val pendingForMe: List<Bet>,
        val myPendingSent: List<Bet>,
        val activeBets: List<Bet>,
        val history: List<Bet>,
        val isPerformingAction: Boolean = false,
        val actionError: String? = null
    ) : AccountUiState {

        /** `true` when all four sections are empty. */
        val isEmpty: Boolean get() =
            pendingForMe.isEmpty() &&
            myPendingSent.isEmpty() &&
            activeBets.isEmpty() &&
            history.isEmpty()
    }
}
