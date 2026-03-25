package com.bck.handshakebet.feature.friends.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bck.handshakebet.feature.friends.domain.repository.FriendshipRepository
import com.bck.handshakebet.feature.home.domain.repository.UserRepository
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
 * ViewModel for the Friends screen.
 *
 * Manages:
 * - Loading and refreshing the friends / request lists.
 * - Debounced user search (400 ms, minimum 2 chars) for the Add Friend dialog.
 * - Accept / reject / remove actions, each guarded against concurrent operations.
 *
 * [userRepository] is used for the "Add Friend" search (all registered users).
 * [friendshipRepository] handles all friendship mutations and reads.
 *
 * @property friendshipRepository Friendship CRUD operations.
 * @property userRepository       User search for the Add Friend dialog.
 */
@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadFriendships()
        observeSearchQuery()
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /** Loads (or refreshes) the full friends / requests data. */
    fun loadFriendships() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            friendshipRepository.loadFriendships()
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isLoading        = false,
                            friends          = data.friends,
                            incomingRequests = data.incomingRequests,
                            sentRequests     = data.sentRequests
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    // ── Add Friend dialog ──────────────────────────────────────────────────────

    /** Opens the Add Friend dialog. */
    fun onAddFriendClick() {
        _uiState.update { it.copy(showAddFriendDialog = true) }
    }

    /** Dismisses the Add Friend dialog and clears search state. */
    fun onDismissAddFriendDialog() {
        _uiState.update {
            it.copy(
                showAddFriendDialog = false,
                searchQuery         = "",
                searchResults       = emptyList()
            )
        }
        _searchQuery.value = ""
    }

    /** Called on every keystroke in the Add Friend search field. */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, searchResults = emptyList()) }
        _searchQuery.value = query
    }

    // ── Friend request actions ─────────────────────────────────────────────────

    /**
     * Sends a friend request to [recipientId].
     *
     * Dismisses the dialog on success and reloads so the sent list updates.
     */
    fun onSendFriendRequest(recipientId: String) {
        if (_uiState.value.isPerformingAction) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true, errorMessage = null) }
            friendshipRepository.sendFriendRequest(recipientId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isPerformingAction  = false,
                            showAddFriendDialog = false,
                            searchQuery         = "",
                            searchResults       = emptyList()
                        )
                    }
                    _searchQuery.value = ""
                    loadFriendships()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isPerformingAction = false, errorMessage = e.message) }
                }
        }
    }

    /** Accepts the incoming friend request with [friendshipId]. */
    fun onAcceptRequest(friendshipId: String) {
        if (_uiState.value.isPerformingAction) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true, errorMessage = null) }
            friendshipRepository.acceptFriendRequest(friendshipId)
                .onSuccess {
                    _uiState.update { it.copy(isPerformingAction = false) }
                    loadFriendships()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isPerformingAction = false, errorMessage = e.message) }
                }
        }
    }

    /** Rejects the incoming friend request with [friendshipId]. */
    fun onRejectRequest(friendshipId: String) {
        if (_uiState.value.isPerformingAction) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true, errorMessage = null) }
            friendshipRepository.rejectFriendRequest(friendshipId)
                .onSuccess {
                    _uiState.update { it.copy(isPerformingAction = false) }
                    loadFriendships()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isPerformingAction = false, errorMessage = e.message) }
                }
        }
    }

    /** Removes an existing friend or withdraws a sent request. */
    fun onRemoveFriendship(friendshipId: String) {
        if (_uiState.value.isPerformingAction) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true, errorMessage = null) }
            friendshipRepository.removeFriendship(friendshipId)
                .onSuccess {
                    _uiState.update { it.copy(isPerformingAction = false) }
                    loadFriendships()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isPerformingAction = false, errorMessage = e.message) }
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
                .drop(1)               // Skip initial empty value
                .debounce(400)
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collectLatest { query ->
                    _uiState.update { it.copy(isSearching = true) }
                    userRepository.searchUsers(query)
                        .onSuccess { results ->
                            _uiState.update { it.copy(searchResults = results, isSearching = false) }
                        }
                        .onFailure {
                            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                        }
                }
        }

        // Clear results when query drops below threshold.
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
