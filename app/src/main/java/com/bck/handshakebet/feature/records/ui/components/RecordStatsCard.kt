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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Summary card showing the user's win / draw / loss record and pride balance.
 */
@Composable
fun RecordStatsCard(
    wins: Int,
    draws: Int,
    losses: Int,
    prideBalance: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text  = "Your Record",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // W / D / L triptych
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(label = "Wins",   value = wins.toString(),   positive = true)
                StatColumn(label = "Draws",  value = draws.toString(),  positive = null)
                StatColumn(label = "Losses", value = losses.toString(), positive = false)
            }

            // Pride / Shame balance: negative pride is displayed as positive Shame
            val balanceColor = when {
                prideBalance > 0 -> MaterialTheme.colorScheme.primary
                prideBalance < 0 -> MaterialTheme.colorScheme.error
                else             -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val balanceLabel = when {
                prideBalance > 0 -> "${prideBalance} Pride"
                prideBalance < 0 -> "${-prideBalance} Shame"
                else             -> "0 Pride"
            }
            Text(
                text      = balanceLabel,
                style     = MaterialTheme.typography.bodyMedium,
                color     = balanceColor,
                fontWeight = FontWeight.SemiBold,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    positive: Boolean?,   // true = win color, false = loss color, null = neutral
    modifier: Modifier = Modifier
) {
    val color = when (positive) {
        true  -> MaterialTheme.colorScheme.primary
        false -> MaterialTheme.colorScheme.error
        null  -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.headlineMedium,
            color      = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordStatsCardPreview() {
    HandshakeBetTheme {
        RecordStatsCard(
            wins         = 5,
            draws        = 1,
            losses       = 3,
            prideBalance = 20,
            modifier     = Modifier.padding(16.dp)
        )
    }
}
