package com.bck.handshakebet.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bck.handshakebet.feature.auth.ui.components.ActionButton
import com.bck.handshakebet.feature.auth.ui.components.DisplayNameField
import com.bck.handshakebet.feature.auth.ui.components.EmailField
import com.bck.handshakebet.feature.auth.ui.components.ErrorMessage
import com.bck.handshakebet.feature.auth.ui.components.PasswordField
import com.bck.handshakebet.feature.auth.ui.components.ToggleModeButton
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Authentication screen handling both login and sign-up in a single view.
 *
 * This composable is intentionally kept thin:
 * - All business logic (validation, API calls) lives in [AuthViewModel].
 * - All form field components are stateless composables in `components/`.
 * - Navigation is triggered by [LaunchedEffect] reacting to [AuthUiState],
 *   never by direct callbacks into a NavController.
 *
 * Local UI state (field values, mode toggle) is managed with [rememberSaveable]
 * so it survives configuration changes without being hoisted into the ViewModel.
 *
 * @param onNavigateToHome Called when authentication succeeds. The nav graph
 *   wires this to the Home destination.
 * @param viewModel The [AuthViewModel] — injected by Hilt via [hiltViewModel].
 */
@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Local form state — survives recomposition and configuration changes.
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var isSignUpMode by rememberSaveable { mutableStateOf(false) }

    // Derive display values from uiState.
    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = when (val s = uiState) {
        is AuthUiState.Error -> s.message
        else -> null
    }
    val infoMessage = when (uiState) {
        AuthUiState.EmailVerificationSent ->
            "Check your inbox to verify your email, then log in."
        else -> null
    }

    // React to terminal states — navigate or reset form fields.
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                onNavigateToHome()
                viewModel.onStateConsumed()
            }
            AuthUiState.EmailVerificationSent -> {
                isSignUpMode = false
                password = ""
                displayName = ""
            }
            else -> Unit
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "HandshakeBet",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isSignUpMode) "Create your account" else "Welcome back",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form fields
            EmailField(
                value = email,
                onValueChange = { email = it },
                onNext = {}
            )

            if (isSignUpMode) {
                Spacer(modifier = Modifier.height(8.dp))
                DisplayNameField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    onNext = {}
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            PasswordField(
                value = password,
                onValueChange = { password = it },
                onDone = {
                    if (!isSignUpMode) viewModel.onLoginClicked(email, password)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            ActionButton(
                isSignUpMode = isSignUpMode,
                isLoading = isLoading,
                onClick = {
                    if (isSignUpMode) {
                        viewModel.onSignUpClicked(email, password, displayName)
                    } else {
                        viewModel.onLoginClicked(email, password)
                    }
                }
            )

            ToggleModeButton(
                isSignUpMode = isSignUpMode,
                isLoading = isLoading,
                onClick = {
                    isSignUpMode = !isSignUpMode
                    viewModel.onStateConsumed()
                }
            )

            // Error and info messages
            ErrorMessage(message = errorMessage, isError = true)
            ErrorMessage(message = infoMessage, isError = false)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenLoginModePreview() {
    HandshakeBetTheme {
        // Preview without ViewModel — shows static layout only.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "HandshakeBet",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
                EmailField(value = "", onValueChange = {}, onNext = {})
                Spacer(modifier = Modifier.height(8.dp))
                PasswordField(value = "", onValueChange = {}, onDone = {})
                Spacer(modifier = Modifier.height(24.dp))
                ActionButton(isSignUpMode = false, isLoading = false, onClick = {})
                ToggleModeButton(isSignUpMode = false, isLoading = false, onClick = {})
            }
        }
    }
}
