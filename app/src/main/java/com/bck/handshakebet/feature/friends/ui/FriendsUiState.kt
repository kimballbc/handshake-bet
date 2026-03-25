package com.bck.handshakebet.feature.friends.ui

import com.bck.handshakebet.feature.friends.domain.model.Friend
import com.bck.handshakebet.feature.friends.domain.model.FriendRequest
import com.bck.handshakebet.feature.home.domain.model.UserSummary

/**
 * Immutable UI state for the Friends screen.
 *
 * All mutation goes through [FriendsViewModel] — the composable only reads
 * this snapshot and fires event callbacks.
 *
 * @property isLoading           `true` while the initial friendship list is loading.
 * @property friends             Accepted friends, sorted alphabetically.
 * @property incomingRequests    Pending requests addressed to the current user.
 * @property sentRequests        Pending requests sent by the current user.
 * @property searchQuery         Current text in the "Add friend" search field.
 * @property searchResults       Users matching [searchQuery]; populated after debounce.
 * @property isSearching         `true` while a search request is in flight.
 * @property showAddFriendDialog `true` when the Add Friend dialog is visible.
 * @property isPerformingAction  `true` while an accept/reject/remove/send is in flight.
 * @property errorMessage        A one-shot error to surface to the user, then clear.
 */
data class FriendsUiState(
    val isLoading: Boolean           = false,
    val friends: List<Friend>        = emptyList(),
    val incomingRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest>     = emptyList(),
    val searchQuery: String          = "",
    val searchResults: List<UserSummary>      = emptyList(),
    val isSearching: Boolean         = false,
    val showAddFriendDialog: Boolean = false,
    val isPerformingAction: Boolean  = false,
    val errorMessage: String?        = null
)
