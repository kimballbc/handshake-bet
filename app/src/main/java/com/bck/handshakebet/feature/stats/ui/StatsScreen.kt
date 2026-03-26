package com.bck.handshakebet.feature.stats.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bck.handshakebet.feature.stats.ui.components.StatCard
import com.bck.handshakebet.feature.stats.ui.components.TopOpponentCard
import kotlin.math.absoluteValue

/**
 * Stats screen showing computed metrics derived from the user's bet history.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Stats") }) }
    ) { innerPadding ->

        when (val state = uiState) {

            is StatsUiState.Loading -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is StatsUiState.Error -> {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text      = state.message,
                            style     = MaterialTheme.typography.bodyLarge,
                            color     = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = viewModel::refresh) { Text("Retry") }
                    }
                }
            }

            is StatsUiState.Success -> {
                if (state.totalCompleted == 0) {
                    Box(
                        modifier         = Modifier.fillMaxSize().padding(innerPadding),
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
                        modifier        = Modifier.fillMaxSize().padding(innerPadding),
                        contentPadding  = PaddingValues(16.dp),
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

                        // Top rival card
                        state.topOpponent?.let { rival ->
                            item {
                                TopOpponentCard(opponent = rival)
                            }
                        }
                    }
                }
            }
        }
    }
}
