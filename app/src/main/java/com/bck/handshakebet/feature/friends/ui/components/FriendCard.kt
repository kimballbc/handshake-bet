package com.bck.handshakebet.feature.friends.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.feature.friends.domain.model.Friend
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Card displaying an accepted friend with a remove (unfriend) action.
 *
 * @param friend       The [Friend] to display.
 * @param onRemove     Called when the user taps the remove button.
 * @param isActionBusy `true` while any action is in flight; disables the remove button.
 * @param modifier     Optional modifier.
 */
@Composable
fun FriendCard(
    friend: Friend,
    onRemove: (friendshipId: String) -> Unit,
    isActionBusy: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary
                )
                Text(
                    text  = friend.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            IconButton(
                onClick  = { onRemove(friend.friendshipId) },
                enabled  = !isActionBusy
            ) {
                Icon(
                    imageVector        = Icons.Default.PersonRemove,
                    contentDescription = "Remove friend",
                    tint               = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendCardPreview() {
    HandshakeBetTheme {
        FriendCard(
            friend = Friend(friendshipId = "1", userId = "u1", displayName = "Alice"),
            onRemove = {},
            isActionBusy = false
        )
    }
}
