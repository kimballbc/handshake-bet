package com.bck.handshakebet.feature.friends.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.feature.friends.domain.model.FriendRequest
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Card for a pending friend request.
 *
 * For *incoming* requests ([FriendRequest.isIncoming] == `true`) the card
 * shows Accept + Reject icon buttons. For *sent* requests it shows only a
 * "Withdraw" text button.
 *
 * @param request      The [FriendRequest] to display.
 * @param onAccept     Called when the user accepts an incoming request.
 * @param onReject     Called when the user rejects an incoming request.
 * @param onWithdraw   Called when the user withdraws a sent request.
 * @param isActionBusy `true` while any action is in flight; disables all buttons.
 * @param modifier     Optional modifier.
 */
@Composable
fun FriendRequestCard(
    request: FriendRequest,
    onAccept: (friendshipId: String) -> Unit,
    onReject: (friendshipId: String) -> Unit,
    onWithdraw: (friendshipId: String) -> Unit,
    isActionBusy: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Name + subtitle
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector        = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.secondary
                )
                Column {
                    Text(
                        text  = request.otherUserDisplayName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text  = if (request.isIncoming) "Wants to be your friend" else "Request sent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action buttons
            if (request.isIncoming) {
                Row {
                    IconButton(
                        onClick = { onAccept(request.friendshipId) },
                        enabled = !isActionBusy
                    ) {
                        Icon(
                            imageVector        = Icons.Default.CheckCircle,
                            contentDescription = "Accept",
                            tint               = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { onReject(request.friendshipId) },
                        enabled = !isActionBusy
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Cancel,
                            contentDescription = "Reject",
                            tint               = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                TextButton(
                    onClick  = { onWithdraw(request.friendshipId) },
                    enabled  = !isActionBusy
                ) {
                    Text("Withdraw")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendRequestCardIncomingPreview() {
    HandshakeBetTheme {
        FriendRequestCard(
            request = FriendRequest(
                friendshipId         = "1",
                otherUserId          = "u1",
                otherUserDisplayName = "Bob",
                isIncoming           = true
            ),
            onAccept     = {},
            onReject     = {},
            onWithdraw   = {},
            isActionBusy = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendRequestCardSentPreview() {
    HandshakeBetTheme {
        FriendRequestCard(
            request = FriendRequest(
                friendshipId         = "2",
                otherUserId          = "u2",
                otherUserDisplayName = "Carol",
                isIncoming           = false
            ),
            onAccept     = {},
            onReject     = {},
            onWithdraw   = {},
            isActionBusy = false
        )
    }
}
