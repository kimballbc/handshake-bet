package com.bck.handshakebet.feature.newbet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bck.handshakebet.core.ui.components.HandshakeSlider
import com.bck.handshakebet.feature.home.domain.model.UserSummary

/**
 * Screen for creating a new bet.
 *
 * The form collects a title, optional description, a public/private toggle,
 * and an opponent selected via debounced search. A [HandshakeSlider] replaces
 * the conventional submit button — the user must drag to confirm, guarding
 * against accidental submissions.
 *
 * @param onBetCreated  Called after a successful submission — typically pops
 *                      the screen and navigates back to the Account screen.
 * @param onNavigateUp  Called when the user taps the back arrow.
 * @param viewModel     Hilt-injected [NewBetViewModel]; can be overridden in tests.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBetScreen(
    onBetCreated: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: NewBetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate away on success.
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBetCreated()
    }

    // Show transient error in a Snackbar.
    LaunchedEffect(uiState.errorMessage) {
        val msg = uiState.errorMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Bet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Title ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                label = { Text("What's the bet? *") },
                placeholder = { Text("e.g. I can run 5k in under 30 minutes") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Description ────────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Details (optional)") },
                placeholder = { Text("Terms, stakes, deadline…") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Wager amount ────────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.wagerAmount,
                onValueChange = viewModel::onWagerAmountChanged,
                label = { Text("Pride wagered *") },
                placeholder = { Text("1 – 100") },
                singleLine = true,
                isError = uiState.wagerError != null,
                supportingText = {
                    val msg = uiState.wagerError
                        ?: if (uiState.wagerAmount.isBlank()) "How much pride is on the line?"
                           else "Pride wagered: ${uiState.wagerAmount}"
                    Text(msg)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // ── Visibility toggle ──────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Public bet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (uiState.isPublic) "Visible in the Feed" else "Only visible to you and your opponent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isPublic,
                    onCheckedChange = viewModel::onVisibilityChanged
                )
            }

            HorizontalDivider()

            // ── Opponent selection ─────────────────────────────────────────────
            Text(
                text = "Challenge *",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.selectedOpponent != null) {
                // Show selected chip with a clear button.
                InputChip(
                    selected = true,
                    onClick = viewModel::onOpponentCleared,
                    label = { Text(uiState.selectedOpponent!!.displayName) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = "Remove",
                            modifier = Modifier.size(18.dp))
                    }
                )
            } else {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    label = { Text("Search by display name") },
                    placeholder = { Text("Type 2+ characters…") },
                    singleLine = true,
                    leadingIcon = {
                        if (uiState.isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = viewModel::onOpponentCleared) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Search results
                if (uiState.searchResults.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.searchResults.forEachIndexed { index, user ->
                            UserResultRow(
                                user = user,
                                onClick = { viewModel.onOpponentSelected(user) }
                            )
                            if (index < uiState.searchResults.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }

                // TODO(Phase 5): Restrict search to friends list.
                Text(
                    text = "Showing all registered users. Friends-only search coming in a future update.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Submit slider ──────────────────────────────────────────────────
            // key() forces HandshakeSlider to reset its internal drag state when
            // sliderResetKey increments (i.e. after a submission failure).
            key(uiState.sliderResetKey) {
                HandshakeSlider(
                    label = if (uiState.isSubmitting) "Creating…" else "Slide to send the challenge",
                    onConfirmed = viewModel::createBet,
                    enabled = uiState.canSubmit
                )
            }
        }
    }
}

@Composable
private fun UserResultRow(
    user: UserSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
