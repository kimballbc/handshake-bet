package com.bck.handshakebet.feature.account.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.feature.home.domain.model.Bet
import com.bck.handshakebet.feature.home.domain.model.BetStatus
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Dialog for declaring the winner of an active bet.
 *
 * Presents two mutually exclusive options — one per participant — so the user
 * can select who won without free-form input. Either party may declare the
 * winner in Phase 3; agreement logic can be layered on in a future phase.
 *
 * @param bet            The active [Bet] being completed.
 * @param onWinnerSelected Callback invoked with the winning user's ID.
 * @param onDismiss      Invoked when the user cancels without selecting a winner.
 */
@Composable
fun OutcomeDialog(
    bet: Bet,
    onWinnerSelected: (winnerId: String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Who won?",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "\"${bet.title}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Creator option
                    Button(
                        onClick = { onWinnerSelected(bet.creatorId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = bet.creatorDisplayName,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                    // Opponent option
                    OutlinedButton(
                        onClick = { onWinnerSelected(bet.opponentId ?: return@OutlinedButton) },
                        modifier = Modifier.weight(1f),
                        enabled = bet.opponentId != null
                    ) {
                        Text(
                            text = bet.opponentDisplayName ?: "Opponent",
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview
@Composable
private fun OutcomeDialogPreview() {
    HandshakeBetTheme {
        OutcomeDialog(
            bet = Bet(
                id = "1",
                title = "Ben can run a 5k in under 30 minutes",
                description = "",
                creatorId = "user-1",
                creatorDisplayName = "Ben",
                opponentId = "user-2",
                opponentDisplayName = "Alex",
                status = BetStatus.ACTIVE,
                isPublic = true,
                winnerId = null,
                createdAt = ""
            ),
            onWinnerSelected = {},
            onDismiss = {}
        )
    }
}
