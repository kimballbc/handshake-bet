package com.bck.handshakebet.feature.auth.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Text button that toggles between login and sign-up modes.
 *
 * Disabled while [isLoading] to prevent mode switching mid-request.
 *
 * @param isSignUpMode Whether the screen is currently in sign-up mode.
 * @param isLoading Whether an auth operation is in progress.
 * @param onClick Called when the button is tapped.
 * @param modifier Optional [Modifier].
 */
@Composable
fun ToggleModeButton(
    isSignUpMode: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
    ) {
        Text(
            if (isSignUpMode) "Already have an account? Log In"
            else "Don't have an account? Sign Up"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ToggleModeLoginPreview() {
    HandshakeBetTheme {
        ToggleModeButton(isSignUpMode = false, isLoading = false, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ToggleModeSignUpPreview() {
    HandshakeBetTheme {
        ToggleModeButton(isSignUpMode = true, isLoading = false, onClick = {})
    }
}
