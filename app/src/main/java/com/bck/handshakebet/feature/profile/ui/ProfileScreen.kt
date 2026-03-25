package com.bck.handshakebet.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Profile screen showing the current user's display name, a Friends shortcut,
 * and a Sign Out button.
 *
 * Acts as the entry point to the Friends graph — tapping "Friends" navigates to
 * [com.bck.handshakebet.feature.friends.ui.FriendsScreen].
 *
 * @param onNavigateToFriends Called when the user taps the Friends button.
 * @param onSignedOut         Called after a successful sign-out; navigate to Login.
 * @param viewModel           Injected automatically by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToFriends: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier              = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Avatar placeholder ────────────────────────────────────────
            Icon(
                imageVector        = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier           = Modifier.size(96.dp),
                tint               = MaterialTheme.colorScheme.primary
            )

            // ── Display name ──────────────────────────────────────────────
            Text(
                text      = uiState.displayName ?: "—",
                style     = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Friends shortcut ──────────────────────────────────────────
            OutlinedButton(
                onClick  = onNavigateToFriends,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector        = Icons.Default.Group,
                    contentDescription = null,
                    modifier           = Modifier.padding(end = 8.dp)
                )
                Text("Friends")
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Sign Out ──────────────────────────────────────────────────
            Button(
                onClick  = { viewModel.onSignOutClicked(onSignedOut) },
                enabled  = !uiState.isSigningOut,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor   = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                if (uiState.isSigningOut) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Default.Logout,
                        contentDescription = null,
                        modifier           = Modifier.padding(end = 8.dp)
                    )
                    Text("Sign Out")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
