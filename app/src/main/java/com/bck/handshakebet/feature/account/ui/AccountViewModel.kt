package com.bck.handshakebet.feature.account.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bck.handshakebet.feature.home.domain.model.Bet
import com.bck.handshakebet.feature.home.domain.model.BetStatus
import com.bck.handshakebet.feature.home.domain.repository.BetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Account (Home) screen.
 *
 * Loads all of the current user's bets on creation and buckets them into
 * [AccountUiState.Success] sections. Exposes action functions for accepting,
 * rejecting, cancelling, and completing bets; each wraps the repository call
 * in a loading-flag so the UI can disable controls during the in-flight request.
 *
 * @property betRepository Source of truth for bet data.
 * @property auth          Provides the current user's ID for role-based bucketing.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val betRepository: BetRepository,
    private val auth: Auth
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadBets()
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Fetches the current user's bets and partitions them into the four
     * sections exposed by [AccountUiState.Success].
     */
    fun loadBets() {
        viewModelScope.launch {
            _uiState.value = AccountUiState.Loading
            betRepository.fetchMyBets()
                .onSuccess { bets -> _uiState.value = bets.toSuccessState() }
                .onFailure { e  -> _uiState.value = AccountUiState.Error(
                    e.message ?: "Could not load your bets."
                )}
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Accepts a pending bet directed at the current user.
     * On success the bet list is refreshed.
     */
    fun acceptBet(betId: String) = performAction {
        betRepository.acceptBet(betId)
    }

    /**
     * Rejects a pending bet directed at the current user.
     * On success the bet list is refreshed.
     */
    fun rejectBet(betId: String) = performAction {
        betRepository.rejectBet(betId)
    }

    /**
     * Cancels a pending bet created by the current user.
     * On success the bet list is refreshed.
     */
    fun cancelBet(betId: String) = performAction {
        betRepository.cancelBet(betId)
    }

    /**
     * Completes an active bet and records [winnerId] as the winner.
     * On success the bet list is refreshed.
     */
    fun completeBet(betId: String, winnerId: String) = performAction {
        betRepository.completeBet(betId, winnerId)
    }

    /** Clears a transient action error shown in the UI. */
    fun onActionErrorShown() {
        (_uiState.value as? AccountUiState.Success)?.let { current ->
            _uiState.value = current.copy(actionError = null)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Wraps an action network call with loading-flag management.
     *
     * Sets [AccountUiState.Success.isPerformingAction] to `true` before the
     * call, then either refreshes the list on success or surfaces the error
     * message without clobbering the existing bet list.
     */
    private fun performAction(action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            setActionLoading(true)
            action()
                .onSuccess { loadBets() }
                .onFailure { e ->
                    setActionLoading(false)
                    (_uiState.value as? AccountUiState.Success)?.let { current ->
                        _uiState.value = current.copy(
                            isPerformingAction = false,
                            actionError = e.message ?: "Action failed. Please try again."
                        )
                    }
                }
        }
    }

    private fun setActionLoading(loading: Boolean) {
        (_uiState.value as? AccountUiState.Success)?.let { current ->
            _uiState.update { current.copy(isPerformingAction = loading) }
        }
    }

    /**
     * Partitions a flat list of [Bet]s into the four [AccountUiState.Success] sections
     * relative to the current user.
     */
    private fun List<Bet>.toSuccessState(): AccountUiState.Success {
        val userId = auth.currentUserOrNull()?.id ?: ""
        return AccountUiState.Success(
            currentUserId = userId,
            pendingForMe  = filter { it.status == BetStatus.PENDING  && it.opponentId == userId },
            myPendingSent = filter { it.status == BetStatus.PENDING  && it.creatorId  == userId },
            activeBets    = filter { it.status == BetStatus.ACTIVE },
            history       = filter { it.status in setOf(
                BetStatus.COMPLETED, BetStatus.REJECTED, BetStatus.CANCELLED
            )}
        )
    }
}
