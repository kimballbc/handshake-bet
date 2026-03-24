package com.bck.handshakebet.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.feature.home.domain.model.Bet
import com.bck.handshakebet.feature.home.domain.model.BetStatus
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Card displaying a bet from the signed-in user's personal feed (My Bets tab).
 *
 * Includes a status [Badge] so users can quickly identify bets awaiting their
 * action (e.g. [BetStatus.PENDING] bets needing acceptance).
 *
 * This composable is stateless — no business logic lives here.
 *
 * @param bet     The bet to display.
 * @param onClick Invoked when the user taps the card. Navigates to BetDetail.
 * @param modifier Optional modifier for the card container.
 */
@Composable
fun FriendBetCard(
    bet: Bet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bet.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = bet.status)
            }

            if (bet.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bet.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = bet.creatorDisplayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (bet.opponentDisplayName != null) {
                    Text(
                        text = "vs ${bet.opponentDisplayName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "🤝 ${bet.prideWagered} pride",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

/**
 * Small badge showing the current [BetStatus] with appropriate colour coding.
 *
 * @param status The status to display.
 */
@Composable
private fun StatusBadge(status: BetStatus) {
    val label = when (status) {
        BetStatus.PENDING   -> "Pending"
        BetStatus.ACTIVE    -> "Active"
        BetStatus.COMPLETED -> "Done"
        BetStatus.REJECTED  -> "Rejected"
        BetStatus.CANCELLED -> "Cancelled"
    }
    val containerColor = when (status) {
        BetStatus.PENDING   -> MaterialTheme.colorScheme.tertiaryContainer
        BetStatus.ACTIVE    -> MaterialTheme.colorScheme.primaryContainer
        BetStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
        BetStatus.REJECTED,
        BetStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
    }
    Badge(containerColor = containerColor) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendBetCardPendingPreview() {
    HandshakeBetTheme {
        FriendBetCard(
            bet = Bet(
                id = "1",
                title = "First one to the gym wins",
                description = "Must check in before 7 AM on Monday",
                creatorId = "user-1",
                creatorDisplayName = "Ben",
                opponentId = "user-2",
                opponentDisplayName = "Chris",
                status = BetStatus.PENDING,
                isPublic = false,
                winnerId = null,
                createdAt = "2024-09-01T12:00:00Z"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendBetCardActivePreview() {
    HandshakeBetTheme {
        FriendBetCard(
            bet = Bet(
                id = "2",
                title = "10 push-ups challenge",
                description = "",
                creatorId = "user-2",
                creatorDisplayName = "Chris",
                opponentId = "user-1",
                opponentDisplayName = "Ben",
                status = BetStatus.ACTIVE,
                isPublic = false,
                winnerId = null,
                createdAt = "2024-09-02T08:00:00Z"
            ),
            onClick = {}
        )
    }
}
