package com.bck.handshakebet.feature.records.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bck.handshakebet.feature.records.ui.components.CompletedBetCard
import com.bck.handshakebet.feature.records.ui.components.RecordStatsCard
import com.bck.handshakebet.feature.stats.ui.StatsUiState
import com.bck.handshakebet.feature.stats.ui.StatsViewModel
import com.bck.handshakebet.feature.stats.ui.components.StatCard
import com.bck.handshakebet.feature.stats.ui.components.TopOpponentCard
import kotlin.math.absoluteValue

/**
 * Records screen with two tabs — Records (W/D/L history) and Stats (computed metrics).
 *
 * Both ViewModels are injected independently so each tab loads in parallel and
 * can be refreshed independently via Retry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    recordsViewModel: RecordsViewModel = hiltViewModel(),
    statsViewModel: StatsViewModel     = hiltViewModel()
) {
    val recordsState by recordsViewModel.uiState.collectAsStateWithLifecycle()
    val statsState   by statsViewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Records") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0 },
                    text     = { Text("Records") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1 },
                    text     = { Text("Stats") }
                )
            }

            when (selectedTab) {
                0 -> RecordsTabContent(
                    state   = recordsState,
                    onRetry = recordsViewModel::refresh
                )
                1 -> StatsTabContent(
                    state   = statsState,
                    onRetry = statsViewModel::refresh
                )
            }
        }
    }
}

// ── Records tab ───────────────────────────────────────────────────────────────

@Composable
private fun RecordsTabContent(
    state: RecordsUiState,
    onRetry: () -> Unit
) {
    when (state) {
        is RecordsUiState.Loading -> CentredLoading()

        is RecordsUiState.Error -> CentredError(message = state.message, onRetry = onRetry)

        is RecordsUiState.Success -> {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    RecordStatsCard(
                        wins         = state.wins,
                        draws        = state.draws,
                        losses       = state.losses,
                        prideBalance = state.prideBalance
                    )
                }

                if (state.completedBets.isNotEmpty()) {
                    item {
                        Text(
                            text     = "History",
                            style    = MaterialTheme.typography.titleSmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(state.completedBets, key = { it.id }) { bet ->
                        CompletedBetCard(bet = bet)
                    }
                } else {
                    item {
                        Text(
                            text      = "No completed bets yet.",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Stats tab ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsTabContent(
    state: StatsUiState,
    onRetry: () -> Unit
) {
    when (state) {
        is StatsUiState.Loading -> CentredLoading()

        is StatsUiState.Error -> CentredError(message = state.message, onRetry = onRetry)

        is StatsUiState.Success -> {
            if (state.totalCompleted == 0) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text      = "Complete some bets to see your stats.",
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        val streakText = when {
                            state.currentStreak > 0 -> "🔥 ${state.currentStreak}W"
                            state.currentStreak < 0 -> "❄️ ${state.currentStreak.absoluteValue}L"
                            else                    -> "—"
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StatCard(
                                    label    = "Bets completed",
                                    value    = state.totalCompleted.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label    = "Win rate",
                                    value    = state.winRate?.let { "${(it * 100).toInt()}%" } ?: "—",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StatCard(
                                    label    = "Current streak",
                                    value    = streakText,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label    = "Best win streak",
                                    value    = if (state.bestWinStreak > 0) "${state.bestWinStreak}W" else "—",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                StatCard(
                                    label    = "Avg pride wagered",
                                    value    = state.averagePrideWagered?.let { "%.1f".format(it) } ?: "—",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    state.topOpponent?.let { rival ->
                        item { TopOpponentCard(opponent = rival) }
                    }
                }
            }
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun CentredLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CentredError(message: String, onRetry: () -> Unit) {
    Box(
        modifier         = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text      = message,
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}
