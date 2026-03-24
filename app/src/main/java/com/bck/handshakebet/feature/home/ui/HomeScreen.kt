package com.bck.handshakebet.feature.home.ui

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bck.handshakebet.feature.home.domain.model.Bet
import com.bck.handshakebet.feature.home.domain.model.BetStatus
import com.bck.handshakebet.feature.home.ui.components.FriendBetCard
import com.bck.handshakebet.feature.home.ui.components.PublicBetCard
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Home screen showing the public bet feed and the user's own bets across two tabs.
 *
 * This composable is intentionally kept thin:
 * - All data fetching and tab logic lives in [HomeViewModel].
 * - Card components are stateless composables in `components/`.
 * - Navigation callbacks are lambdas; no NavController reference here.
 *
 * @param onNavigateToBetDetail Called when a bet card is tapped. Provides the bet ID.
 * @param viewModel             The [HomeViewModel] — injected by Hilt via [hiltViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBetDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onTabSelected = viewModel::onTabSelected,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onBetClick = onNavigateToBetDetail
    )
}

/**
 * Stateless inner composable for [HomeScreen].
 *
 * Separating the content from the ViewModel-connected composable makes it
 * straightforward to preview and test in isolation.
 *
 * @param uiState      Current screen state from [HomeViewModel].
 * @param onTabSelected Callback when the user switches tabs.
 * @param onRefresh    Callback for pull-to-refresh.
 * @param onRetry      Callback when the user taps "Retry" on the error state.
 * @param onBetClick   Callback when a bet card is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onTabSelected: (HomeTab) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onBetClick: (String) -> Unit
) {
    val selectedTab = when (uiState) {
        is HomeUiState.Success -> uiState.selectedTab
        is HomeUiState.Empty   -> uiState.selectedTab
        else                   -> HomeTab.PUBLIC
    }
    val isRefreshing = (uiState as? HomeUiState.Success)?.isRefreshing == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HandshakeBet",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // ── Tab row ───────────────────────────────────────────────────────
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == HomeTab.PUBLIC,
                    onClick = { onTabSelected(HomeTab.PUBLIC) },
                    text = { Text("Public") }
                )
                Tab(
                    selected = selectedTab == HomeTab.MY_BETS,
                    onClick = { onTabSelected(HomeTab.MY_BETS) },
                    text = { Text("My Bets") }
                )
            }

            // ── Content area ─────────────────────────────────────────────────
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when (uiState) {
                    HomeUiState.Loading -> LoadingContent()
                    is HomeUiState.Error -> ErrorContent(
                        message = uiState.message,
                        onRetry = onRetry
                    )
                    is HomeUiState.Empty -> EmptyContent(tab = uiState.selectedTab)
                    is HomeUiState.Success -> BetList(
                        bets = when (uiState.selectedTab) {
                            HomeTab.PUBLIC  -> uiState.publicBets
                            HomeTab.MY_BETS -> uiState.myBets
                        },
                        tab = uiState.selectedTab,
                        onBetClick = onBetClick
                    )
                }
            }
        }
    }
}

// ── Content slots ─────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}

@Composable
private fun EmptyContent(tab: HomeTab) {
    val message = when (tab) {
        HomeTab.PUBLIC  -> "No public bets right now.\nBe the first to make one!"
        HomeTab.MY_BETS -> "You haven't made any bets yet.\nTap + to challenge a friend!"
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BetList(
    bets: List<Bet>,
    tab: HomeTab,
    onBetClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = bets, key = { it.id }) { bet ->
            when (tab) {
                HomeTab.PUBLIC  -> PublicBetCard(bet = bet, onClick = { onBetClick(bet.id) })
                HomeTab.MY_BETS -> FriendBetCard(bet = bet, onClick = { onBetClick(bet.id) })
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewBets = listOf(
    Bet("1", "Pizza challenge", "Finish it all", "u1", "Ben", "u2", "Alex",
        BetStatus.ACTIVE, true, null, "2024-09-01T12:00:00Z"),
    Bet("2", "First to the gym", "Before 7 AM Monday", "u2", "Alex", "u1", "Ben",
        BetStatus.ACTIVE, true, null, "2024-09-02T08:00:00Z")
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenSuccessPreview() {
    HandshakeBetTheme {
        HomeScreenContent(
            uiState = HomeUiState.Success(
                publicBets = previewBets,
                myBets = emptyList(),
                selectedTab = HomeTab.PUBLIC
            ),
            onTabSelected = {},
            onRefresh = {},
            onRetry = {},
            onBetClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    HandshakeBetTheme {
        HomeScreenContent(
            uiState = HomeUiState.Loading,
            onTabSelected = {},
            onRefresh = {},
            onRetry = {},
            onBetClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    HandshakeBetTheme {
        HomeScreenContent(
            uiState = HomeUiState.Empty(HomeTab.PUBLIC),
            onTabSelected = {},
            onRefresh = {},
            onRetry = {},
            onBetClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenErrorPreview() {
    HandshakeBetTheme {
        HomeScreenContent(
            uiState = HomeUiState.Error("No internet connection. Please check your network."),
            onTabSelected = {},
            onRefresh = {},
            onRetry = {},
            onBetClick = {}
        )
    }
}
