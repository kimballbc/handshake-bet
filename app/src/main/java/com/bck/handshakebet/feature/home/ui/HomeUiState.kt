package com.bck.handshakebet.feature.home.ui

import com.bck.handshakebet.feature.home.domain.model.Bet

/**
 * Represents the two tab destinations on the Home screen.
 *
 * - [PUBLIC]  — Feed of all publicly visible, active bets.
 * - [MY_BETS] — Bets where the current user is the creator or opponent.
 */
enum class HomeTab { PUBLIC, MY_BETS }

/**
 * Exhaustive state for the Home screen.
 *
 * Sealed to ensure the UI handles every possible state. The screen observes a
 * single [kotlinx.coroutines.flow.StateFlow]<[HomeUiState]> from [HomeViewModel]
 * and renders accordingly.
 */
sealed interface HomeUiState {

    /** Initial state while bets are being fetched for the first time. */
    data object Loading : HomeUiState

    /**
     * Bets loaded successfully.
     *
     * @property publicBets   Current page of public bets.
     * @property myBets       Bets involving the signed-in user.
     * @property selectedTab  Which tab is currently active.
     * @property isRefreshing `true` while a pull-to-refresh is in progress.
     */
    data class Success(
        val publicBets: List<Bet>,
        val myBets: List<Bet>,
        val selectedTab: HomeTab = HomeTab.PUBLIC,
        val isRefreshing: Boolean = false
    ) : HomeUiState

    /**
     * The active tab has no bets to display.
     *
     * @property selectedTab The tab that is currently empty.
     */
    data class Empty(val selectedTab: HomeTab) : HomeUiState

    /**
     * A network or server error prevented bets from loading.
     *
     * @property message User-friendly error description.
     */
    data class Error(val message: String) : HomeUiState
}
