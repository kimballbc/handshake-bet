package com.bck.handshakebet.feature.account.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bck.handshakebet.feature.account.ui.components.BetActionCard

/**
 * Account / Home screen — the signed-in user's personal bet inbox.
 *
 * Shows four sections:
 * 1. **Action needed** — pending bets the user has been challenged to.
 * 2. **Awaiting response** — pending bets the user sent, awaiting the opponent.
 * 3. **In progress** — active bets underway.
 * 4. **History** — completed, rejected, and cancelled bets.
 *
 * Sections are omitted when empty. A full-screen empty state is shown when
 * all four sections are empty.
 *
 * @param viewModel Hilt-injected [AccountViewModel]; can be overridden in tests.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show transient action errors in a Snackbar.
    val actionError = (uiState as? AccountUiState.Success)?.actionError
    LaunchedEffect(actionError) {
        if (actionError != null) {
            snackbarHostState.showSnackbar(actionError)
            viewModel.onActionErrorShown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is AccountUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is AccountUiState.Error -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = viewModel::loadBets) { Text("Retry") }
                }
            }

            is AccountUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = state.isPerformingAction,
                    onRefresh = viewModel::loadBets,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.isEmpty) {
                        EmptyAccountState(modifier = Modifier.fillMaxSize())
                    } else {
                        AccountBetList(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun AccountBetList(
    state: AccountUiState.Success,
    viewModel: AccountViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // ── Action needed ──────────────────────────────────────────────────────
        if (state.pendingForMe.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Action Needed",
                    count = state.pendingForMe.size
                )
            }
            items(state.pendingForMe, key = { it.id }) { bet ->
                BetActionCard(
                    bet = bet,
                    currentUserId = state.currentUserId,
                    isActionEnabled = !state.isPerformingAction,
                    onAccept  = { viewModel.acceptBet(bet.id) },
                    onReject  = { viewModel.rejectBet(bet.id) },
                    onCancel  = {},
                    onComplete = { winnerId -> viewModel.completeBet(bet.id, winnerId) }
                )
            }
        }

        // ── Awaiting response ──────────────────────────────────────────────────
        if (state.myPendingSent.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                SectionHeader(
                    title = "Awaiting Response",
                    count = state.myPendingSent.size
                )
            }
            items(state.myPendingSent, key = { it.id }) { bet ->
                BetActionCard(
                    bet = bet,
                    currentUserId = state.currentUserId,
                    isActionEnabled = !state.isPerformingAction,
                    onAccept  = {},
                    onReject  = {},
                    onCancel  = { viewModel.cancelBet(bet.id) },
                    onComplete = {}
                )
            }
        }

        // ── In progress ────────────────────────────────────────────────────────
        if (state.activeBets.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item { SectionHeader(title = "In Progress", count = state.activeBets.size) }
            items(state.activeBets, key = { it.id }) { bet ->
                BetActionCard(
                    bet = bet,
                    currentUserId = state.currentUserId,
                    isActionEnabled = !state.isPerformingAction,
                    onAccept  = {},
                    onReject  = {},
                    onCancel  = {},
                    onComplete = { winnerId -> viewModel.completeBet(bet.id, winnerId) }
                )
            }
        }

        // ── History ────────────────────────────────────────────────────────────
        if (state.history.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item { SectionHeader(title = "History", count = null) }
            items(state.history, key = { it.id }) { bet ->
                BetActionCard(
                    bet = bet,
                    currentUserId = state.currentUserId,
                    isActionEnabled = false,
                    onAccept  = {},
                    onReject  = {},
                    onCancel  = {},
                    onComplete = {}
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int?, modifier: Modifier = Modifier) {
    Text(
        text = if (count != null) "$title ($count)" else title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
private fun EmptyAccountState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No bets yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to challenge someone.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
