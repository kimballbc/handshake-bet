package com.bck.handshakebet.feature.auth.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Primary action button for the auth screen.
 *
 * Renders either "Log In" or "Sign Up" depending on [isSignUpMode], and
 * replaces the label with a [CircularProgressIndicator] while [isLoading]
 * is true. The button is automatically disabled while loading to prevent
 * duplicate submissions.
 *
 * @param isSignUpMode Whether the screen is in sign-up mode.
 * @param isLoading Whether an auth operation is currently in progress.
 * @param onClick Called when the button is tapped (not called while loading).
 * @param modifier Optional [Modifier].
 */
@Composable
fun ActionButton(
    isSignUpMode: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(if (isSignUpMode) "Sign Up" else "Log In")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonLoginPreview() {
    HandshakeBetTheme {
        ActionButton(isSignUpMode = false, isLoading = false, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonSignUpPreview() {
    HandshakeBetTheme {
        ActionButton(isSignUpMode = true, isLoading = false, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonLoadingPreview() {
    HandshakeBetTheme {
        ActionButton(isSignUpMode = false, isLoading = true, onClick = {})
    }
}
