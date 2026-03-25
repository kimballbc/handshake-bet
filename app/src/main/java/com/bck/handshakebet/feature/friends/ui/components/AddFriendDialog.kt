package com.bck.handshakebet.feature.friends.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.feature.home.domain.model.UserSummary

/**
 * Modal dialog for searching and adding a new friend.
 *
 * Displays a debounced search field (populated by [FriendsViewModel]) and a
 * list of matching users. Tapping a result sends a friend request via [onSend].
 *
 * @param searchQuery    Current text in the search field.
 * @param searchResults  Users matching the current query.
 * @param isSearching    `true` while a search is in flight; shows a spinner.
 * @param isActionBusy   `true` while a send-request call is in flight.
 * @param onQueryChanged Called on every keystroke.
 * @param onSend         Called when the user taps a result to send a request.
 * @param onDismiss      Called when the user taps Cancel or outside the dialog.
 */
@Composable
fun AddFriendDialog(
    searchQuery: String,
    searchResults: List<UserSummary>,
    isSearching: Boolean,
    isActionBusy: Boolean,
    onQueryChanged: (String) -> Unit,
    onSend: (userId: String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
        },
        title = { Text("Add Friend") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = onQueryChanged,
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text("Search by display name") },
                    leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    enabled       = !isActionBusy
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    isSearching -> {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    searchResults.isNotEmpty() -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(searchResults, key = { it.id }) { user ->
                                ListItem(
                                    headlineContent = { Text(user.displayName) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isActionBusy) { onSend(user.id) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                    searchQuery.length >= 2 && !isSearching -> {
                        Text(
                            text  = "No users found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    searchQuery.isBlank() -> {
                        Text(
                            text  = "Type a name to search",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
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
