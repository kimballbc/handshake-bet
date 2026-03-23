package com.bck.handshakebet.feature.auth.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Animated error or info message displayed below the auth form.
 *
 * Fades in when [message] is non-null and fades out when it becomes null,
 * providing a subtle visual cue without jarring layout shifts. The text
 * colour uses [MaterialTheme.colorScheme.error] to signal problems, but can
 * be overridden via [isError] to show informational messages (e.g. the
 * email verification prompt) in the primary colour instead.
 *
 * @param message The message to display, or null to hide the component.
 * @param isError If true (default), renders in error colour. If false,
 *   renders in primary colour for informational messages.
 * @param modifier Optional [Modifier].
 */
@Composable
fun ErrorMessage(
    message: String?,
    isError: Boolean = true,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Text(
            text = message.orEmpty(),
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorMessagePreview() {
    HandshakeBetTheme {
        ErrorMessage(message = "Incorrect email or password")
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoMessagePreview() {
    HandshakeBetTheme {
        ErrorMessage(
            message = "Please check your email to verify your account",
            isError = false
        )
    }
}
