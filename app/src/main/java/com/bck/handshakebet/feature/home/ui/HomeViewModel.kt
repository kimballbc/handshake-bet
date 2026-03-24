package com.bck.handshakebet.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bck.handshakebet.feature.home.domain.model.Bet
import com.bck.handshakebet.feature.home.domain.repository.BetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 *
 * Manages the two-tab bet feed (Public / My Bets), pull-to-refresh, and error
 * recovery. All data fetches run in [viewModelScope] so they are automatically
 * cancelled when the screen leaves composition.
 *
 * This class has no Compose imports — all UI concerns are handled by [HomeScreen].
 *
 * @property betRepository Source of truth for bet data.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val betRepository: BetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    /** Observable state for the Home screen. */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Last successfully loaded public bets. Preserved so that switching away
     * from [HomeUiState.Empty] and back to the PUBLIC tab can still render data.
     */
    private var lastPublicBets: List<Bet> = emptyList()

    /**
     * Last successfully loaded personal bets. Preserved so that switching away
     * from [HomeUiState.Empty] and back to the MY_BETS tab can still render data.
     */
    private var lastMyBets: List<Bet> = emptyList()

    init {
        loadBets()
    }

    // ── Public actions ────────────────────────────────────────────────────────

    /**
     * Switches the active tab and re-evaluates whether to show the empty state
     * for the newly selected tab.
     *
     * Handles the case where the current state is [HomeUiState.Empty] by
     * reconstructing a [HomeUiState.Success] from the last known bet lists,
     * preventing the tab bar from becoming permanently unresponsive.
     *
     * @param tab The tab to make active.
     */
    fun onTabSelected(tab: HomeTab) {
        // Reconstruct Success from backing fields when in Empty state so tab
        // switches work even when the active tab has no content.
        val current = when (val s = _uiState.value) {
            is HomeUiState.Success -> s
            is HomeUiState.Empty   -> HomeUiState.Success(
                publicBets  = lastPublicBets,
                myBets      = lastMyBets,
                selectedTab = s.selectedTab
            )
            else -> return
        }
        val newState = current.copy(selectedTab = tab)
        _uiState.value = if (newState.isActiveTabEmpty()) {
            HomeUiState.Empty(tab)
        } else {
            newState
        }
    }

    /**
     * Triggers a pull-to-refresh reload without resetting to the full [HomeUiState.Loading]
     * state, so existing content remains visible while new data is fetched.
     */
    fun onRefresh() {
        val current = _uiState.value
        // Only allow refresh when data has already loaded (not during first load or error).
        if (current !is HomeUiState.Success) {
            loadBets()
            return
        }
        _uiState.value = current.copy(isRefreshing = true)
        loadBets(isRefresh = true)
    }

    /**
     * Retries a failed load from the [HomeUiState.Error] state.
     */
    fun onRetry() = loadBets()

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Fetches both public bets and the user's own bets in parallel.
     *
     * On success, transitions to [HomeUiState.Success] (or [HomeUiState.Empty]
     * if the active tab has no bets). On failure, transitions to [HomeUiState.Error].
     *
     * @param isRefresh `true` when called from [onRefresh]; keeps the existing
     *   selected tab after reload.
     */
    private fun loadBets(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val previousTab = (_uiState.value as? HomeUiState.Success)?.selectedTab
                ?: HomeTab.PUBLIC

            val publicDeferred = async { betRepository.fetchPublicBets() }
            val myDeferred = async { betRepository.fetchMyBets() }

            val publicResult = publicDeferred.await()
            val myResult = myDeferred.await()

            if (publicResult.isFailure) {
                _uiState.value = HomeUiState.Error(
                    publicResult.exceptionOrNull()?.message ?: "Unable to load bets."
                )
                return@launch
            }

            val publicBets = publicResult.getOrElse { emptyList() }
            val myBets = myResult.getOrElse { emptyList() }

            // Preserve for tab switching out of Empty state.
            lastPublicBets = publicBets
            lastMyBets = myBets

            val selectedTab = if (isRefresh) previousTab else HomeTab.PUBLIC

            val success = HomeUiState.Success(
                publicBets = publicBets,
                myBets = myBets,
                selectedTab = selectedTab,
                isRefreshing = false
            )

            _uiState.value = if (success.isActiveTabEmpty()) {
                HomeUiState.Empty(selectedTab)
            } else {
                success
            }
        }
    }

    /**
     * Returns `true` when the list for the currently selected tab is empty.
     */
    private fun HomeUiState.Success.isActiveTabEmpty(): Boolean =
        when (selectedTab) {
            HomeTab.PUBLIC  -> publicBets.isEmpty()
            HomeTab.MY_BETS -> myBets.isEmpty()
        }
}
