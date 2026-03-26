package com.bck.handshakebet.feature.records.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.feature.records.domain.model.BetOutcome
import com.bck.handshakebet.feature.records.domain.model.CompletedBet
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Card displaying a single completed bet with its outcome badge.
 */
@Composable
fun CompletedBetCard(
    bet: CompletedBet,
    modifier: Modifier = Modifier
) {
    val (outcomeLabel, outcomeColor) = when (bet.outcome) {
        BetOutcome.WIN  -> "WIN"  to MaterialTheme.colorScheme.primary
        BetOutcome.DRAW -> "DRAW" to MaterialTheme.colorScheme.onSurfaceVariant
        BetOutcome.LOSS -> "LOSS" to MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text       = bet.title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 2
                )
                Text(
                    text  = "vs ${bet.opponentDisplayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = "${bet.prideWagered} pride",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text       = outcomeLabel,
                style      = MaterialTheme.typography.labelLarge,
                color      = outcomeColor,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompletedBetCardWinPreview() {
    HandshakeBetTheme {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompletedBetCard(
                bet = CompletedBet("1", "Ben can run a 5k in 30 mins", "Alex", 10, BetOutcome.WIN,  "2024-01-01")
            )
            CompletedBetCard(
                bet = CompletedBet("2", "First to 100 push-ups",       "Sam",  5,  BetOutcome.DRAW, "2024-01-02")
            )
            CompletedBetCard(
                bet = CompletedBet("3", "Who finishes first?",          "Jo",   20, BetOutcome.LOSS, "2024-01-03")
            )
        }
    }
}
