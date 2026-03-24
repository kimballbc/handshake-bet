package com.bck.handshakebet.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.feature.home.domain.model.Bet
import com.bck.handshakebet.feature.home.domain.model.BetStatus
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Card displaying a single public bet in the feed.
 *
 * This composable is stateless — it receives a [Bet] and an optional click
 * handler. All layout decisions live here; no business logic.
 *
 * @param bet     The public bet to display.
 * @param onClick Invoked when the user taps the card. Navigates to BetDetail.
 * @param modifier Optional modifier for the card container.
 */
@Composable
fun PublicBetCard(
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
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PublicBetCardPreview() {
    HandshakeBetTheme {
        PublicBetCard(
            bet = Bet(
                id = "1",
                title = "I bet you can't finish the whole pizza",
                description = "Large 18\" pepperoni — no help allowed",
                creatorId = "user-1",
                creatorDisplayName = "Ben",
                opponentId = "user-2",
                opponentDisplayName = "Alex",
                status = BetStatus.ACTIVE,
                isPublic = true,
                winnerId = null,
                createdAt = "2024-09-01T12:00:00Z"
            ),
            onClick = {}
        )
    }
}
