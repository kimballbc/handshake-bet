package com.bck.handshakebet.feature.newbet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bck.handshakebet.feature.friends.domain.repository.FriendshipRepository
import com.bck.handshakebet.feature.home.domain.model.UserSummary
import com.bck.handshakebet.feature.home.domain.repository.BetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the New Bet creation screen.
 *
 * Manages the form state, debounced opponent search (400 ms, minimum 2 chars),
 * and bet submission. On success [NewBetUiState.isSuccess] is set to `true` so
 * the screen can navigate back to the Account screen.
 *
 * Opponent search is scoped to accepted friends only via [FriendshipRepository.searchFriends].
 *
 * @property betRepository        Used to create the bet once the form is submitted.
 * @property friendshipRepository Used to search accepted friends for the opponent picker.
 */
@HiltViewModel
class NewBetViewModel @Inject constructor(
    private val betRepository: BetRepository,
    private val friendshipRepository: FriendshipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewBetUiState())
    val uiState: StateFlow<NewBetUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        observeSearchQuery()
    }

    // ── Form events ───────────────────────────────────────────────────────────

    /** Called on every keystroke in the title field. */
    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    /** Called on every keystroke in the description field. */
    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    /** Called when the public/private toggle changes. */
    fun onVisibilityChanged(isPublic: Boolean) {
        _uiState.update { it.copy(isPublic = isPublic) }
    }

    /**
     * Called on every keystroke in the pride-wagered field.
     *
     * Validates the input inline: must be a whole number in the range 1–100.
     * An empty field clears the error so the user is not scolded before they
     * have had a chance to type.
     */
    fun onWagerAmountChanged(amount: String) {
        val error = when {
            amount.isBlank() -> null
            amount.toIntOrNull() == null -> "Must be a whole number"
            amount.toInt() !in 1..100 -> "Must be between 1 and 100"
            else -> null
        }
        _uiState.update { it.copy(wagerAmount = amount, wagerError = error) }
    }

    /**
     * Called on every keystroke in the opponent search field.
     * Clears [NewBetUiState.selectedOpponent] when the query changes so the
     * user cannot accidentally keep a stale selection.
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, selectedOpponent = null, searchResults = emptyList()) }
        _searchQuery.value = query
    }

    /** Called when the user taps a result in the opponent search list. */
    fun onOpponentSelected(user: UserSummary) {
        _uiState.update { it.copy(selectedOpponent = user, searchQuery = user.displayName, searchResults = emptyList()) }
    }

    /** Clears the selected opponent and resets the search field. */
    fun onOpponentCleared() {
        _uiState.update { it.copy(selectedOpponent = null, searchQuery = "", searchResults = emptyList()) }
        _searchQuery.value = ""
    }

    // ── Submission ────────────────────────────────────────────────────────────

    /**
     * Submits the form and creates the bet.
     *
     * Guards against double-submission by checking [NewBetUiState.canSubmit].
     * Sets [NewBetUiState.isSuccess] to `true` on success so the screen
     * navigates away.
     */
    fun createBet() {
        val state = _uiState.value
        if (!state.canSubmit) return
        val opponent    = state.selectedOpponent ?: return
        val prideWagered = state.parsedWager ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            betRepository.createBet(
                title               = state.title.trim(),
                description         = state.description.trim(),
                isPublic            = state.isPublic,
                opponentId          = opponent.id,
                opponentDisplayName = opponent.displayName,
                prideWagered        = prideWagered
            )
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { s ->
                        s.copy(
                            isSubmitting   = false,
                            errorMessage   = e.message ?: "Could not create bet. Please try again.",
                            sliderResetKey = s.sliderResetKey + 1
                        )
                    }
                }
        }
    }

    /** Clears a transient error after the UI has displayed it. */
    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ── Search ────────────────────────────────────────────────────────────────

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .drop(1)                      // Skip initial empty value
                .debounce(400)
                .distinctUntilChanged()
                .filter { it.length >= 2 }    // Don't search on very short strings
                .collectLatest { query ->
                    _uiState.update { it.copy(isSearching = true) }
                    friendshipRepository.searchFriends(query)
                        .onSuccess { results ->
                            _uiState.update { it.copy(searchResults = results, isSearching = false) }
                        }
                        .onFailure {
                            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                        }
                }
        }

        // Clear results when query drops below threshold
        viewModelScope.launch {
            _searchQuery
                .drop(1)
                .debounce(100)
                .filter { it.length < 2 }
                .collectLatest {
                    _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                }
        }
    }
}
