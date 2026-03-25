package com.bck.handshakebet.feature.account.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.core.ui.components.HandshakeSlider
import com.bck.handshakebet.feature.account.ui.components.OutcomeDialog
import com.bck.handshakebet.feature.home.domain.model.Bet
import com.bck.handshakebet.feature.home.domain.model.BetStatus
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Card that displays a [Bet] with context-sensitive action controls.
 *
 * The actions shown depend on the bet's [BetStatus] and the current user's role:
 *
 * - **Pending (you are opponent)** — [HandshakeSlider] to accept + [OutlinedButton] to reject.
 * - **Pending (you are creator)** — [OutlinedButton] to cancel.
 * - **Active**                    — [HandshakeSlider] to complete; tapping opens [OutcomeDialog].
 * - **History**                   — Read-only; shows the outcome badge.
 *
 * [isActionEnabled] should be `false` while any action network call is in-flight
 * to prevent duplicate requests.
 *
 * @param bet              The [Bet] to display.
 * @param currentUserId    The signed-in user's ID — used to determine role.
 * @param isActionEnabled  Whether action controls are interactive.
 * @param onAccept         Invoked when the accept slider confirms.
 * @param onReject         Invoked when the reject button is tapped.
 * @param onCancel         Invoked when the cancel button is tapped.
 * @param onComplete       Invoked when the complete slider confirms with the selected [winnerId].
 * @param modifier         Optional modifier applied to the root [Card].
 */
@Composable
fun BetActionCard(
    bet: Bet,
    currentUserId: String,
    isActionEnabled: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
    onComplete: (winnerId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOutcomeDialog by remember { mutableStateOf(false) }
    val isCreator  = bet.creatorId  == currentUserId
    val isOpponent = bet.opponentId == currentUserId

    if (showOutcomeDialog) {
        OutcomeDialog(
            bet = bet,
            onWinnerSelected = { winnerId ->
                showOutcomeDialog = false
                onComplete(winnerId)
            },
            onDismiss = { showOutcomeDialog = false }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ────────────────────────────────────────────────────────
            Text(
                text = bet.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (bet.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bet.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── Participants ──────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = bet.creatorDisplayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "vs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = bet.opponentDisplayName ?: "—",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // ── Wager ─────────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🤝 ${bet.prideWagered} pride",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )

            // ── Outcome badge (history only) ──────────────────────────────────
            if (bet.status == BetStatus.COMPLETED && bet.winnerId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val winnerName = when (bet.winnerId) {
                    bet.creatorId  -> bet.creatorDisplayName
                    bet.opponentId -> bet.opponentDisplayName ?: "Opponent"
                    else           -> "Unknown"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "$winnerName won",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            if (bet.status == BetStatus.REJECTED) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Rejected",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (bet.status == BetStatus.CANCELLED) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Cancelled",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Actions ───────────────────────────────────────────────────────
            when {
                bet.status == BetStatus.PENDING && isOpponent -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    HandshakeSlider(
                        label = "Slide to accept",
                        onConfirmed = onAccept,
                        enabled = isActionEnabled
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onReject,
                        enabled = isActionEnabled,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Text(text = "  Reject", style = MaterialTheme.typography.labelLarge)
                    }
                }
                bet.status == BetStatus.PENDING && isCreator -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onCancel,
                        enabled = isActionEnabled,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Bet")
                    }
                }
                bet.status == BetStatus.ACTIVE -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    HandshakeSlider(
                        label = "Slide to complete",
                        onConfirmed = { showOutcomeDialog = true },
                        enabled = isActionEnabled
                    )
                }
                else -> { /* History — no actions */ }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BetActionCardPendingForMePreview() {
    HandshakeBetTheme {
        BetActionCard(
            bet = Bet("1", "Ben can run 5k < 30min", "", "creator-1", "Ben",
                "me", "Alex", BetStatus.PENDING, false, null, ""),
            currentUserId = "me",
            isActionEnabled = true,
            onAccept = {}, onReject = {}, onCancel = {}, onComplete = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BetActionCardActivePreview() {
    HandshakeBetTheme {
        BetActionCard(
            bet = Bet("2", "Coffee on the next match", "", "me", "Alex",
                "user-2", "Ben", BetStatus.ACTIVE, false, null, ""),
            currentUserId = "me",
            isActionEnabled = true,
            onAccept = {}, onReject = {}, onCancel = {}, onComplete = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
