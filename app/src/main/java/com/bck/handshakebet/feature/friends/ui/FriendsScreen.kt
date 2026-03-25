package com.bck.handshakebet.feature.friends.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bck.handshakebet.feature.friends.ui.components.AddFriendDialog
import com.bck.handshakebet.feature.friends.ui.components.FriendCard
import com.bck.handshakebet.feature.friends.ui.components.FriendRequestCard

/**
 * Full-screen Friends surface, reachable from the Profile screen.
 *
 * Displays three sections in a single scrollable list:
 * 1. Incoming friend requests — accept or reject.
 * 2. Accepted friends — with remove (unfriend) action.
 * 3. Sent (pending) requests — with withdraw action.
 *
 * The FAB opens the Add Friend dialog, which searches all registered users
 * and lets the current user send a friend request.
 *
 * @param onNavigateUp Called when the user taps the back arrow.
 * @param viewModel    Injected automatically by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onNavigateUp: () -> Unit,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show transient errors in a Snackbar.
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onErrorShown()
        }
    }

    if (uiState.showAddFriendDialog) {
        AddFriendDialog(
            searchQuery    = uiState.searchQuery,
            searchResults  = uiState.searchResults,
            isSearching    = uiState.isSearching,
            isActionBusy   = uiState.isPerformingAction,
            onQueryChanged = viewModel::onSearchQueryChanged,
            onSend         = viewModel::onSendFriendRequest,
            onDismiss      = viewModel::onDismissAddFriendDialog
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onAddFriendClick) {
                Icon(
                    imageVector        = Icons.Default.PersonAdd,
                    contentDescription = "Add friend"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        when {
            uiState.isLoading -> {
                Box(
                    modifier          = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment  = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.friends.isEmpty() &&
            uiState.incomingRequests.isEmpty() &&
            uiState.sentRequests.isEmpty() -> {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = "No friends yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text  = "Tap + to send a friend request",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                FriendsContent(
                    uiState       = uiState,
                    onAccept      = viewModel::onAcceptRequest,
                    onReject      = viewModel::onRejectRequest,
                    onWithdraw    = viewModel::onRemoveFriendship,
                    onRemove      = viewModel::onRemoveFriendship,
                    modifier      = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun FriendsContent(
    uiState: FriendsUiState,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onWithdraw: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier         = modifier.fillMaxWidth(),
        contentPadding   = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Incoming requests ──────────────────────────────────────────────
        if (uiState.incomingRequests.isNotEmpty()) {
            item {
                SectionHeader(text = "Friend Requests")
            }
            items(uiState.incomingRequests, key = { it.friendshipId }) { request ->
                FriendRequestCard(
                    request      = request,
                    onAccept     = onAccept,
                    onReject     = onReject,
                    onWithdraw   = onWithdraw,
                    isActionBusy = uiState.isPerformingAction
                )
            }
        }

        // ── Accepted friends ───────────────────────────────────────────────
        if (uiState.friends.isNotEmpty()) {
            item {
                SectionHeader(text = "Friends  (${uiState.friends.size})")
            }
            items(uiState.friends, key = { it.friendshipId }) { friend ->
                FriendCard(
                    friend       = friend,
                    onRemove     = onRemove,
                    isActionBusy = uiState.isPerformingAction
                )
            }
        }

        // ── Sent requests ──────────────────────────────────────────────────
        if (uiState.sentRequests.isNotEmpty()) {
            item {
                SectionHeader(text = "Sent Requests")
            }
            items(uiState.sentRequests, key = { it.friendshipId }) { request ->
                FriendRequestCard(
                    request      = request,
                    onAccept     = onAccept,
                    onReject     = onReject,
                    onWithdraw   = onWithdraw,
                    isActionBusy = uiState.isPerformingAction
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.titleSmall,
        color    = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}
