package com.bck.handshakebet.feature.auth.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Stateless email input field for the login and sign-up forms.
 *
 * Displays an inline error message when [errorMessage] is non-null.
 * The keyboard type is set to [KeyboardType.Email] for appropriate
 * suggestions and layout on device.
 *
 * @param value Current text field value.
 * @param onValueChange Called on every keystroke with the updated value.
 * @param onNext Called when the user presses the "Next" IME action.
 * @param errorMessage Inline error to show below the field, or null for no error.
 * @param modifier Optional [Modifier].
 */
@Composable
fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    onNext: () -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        isError = errorMessage != null,
        supportingText = errorMessage?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { onNext() }),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
private fun EmailFieldPreview() {
    HandshakeBetTheme {
        EmailField(value = "user@example.com", onValueChange = {}, onNext = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailFieldErrorPreview() {
    HandshakeBetTheme {
        EmailField(
            value = "not-an-email",
            onValueChange = {},
            onNext = {},
            errorMessage = "Please enter a valid email address"
        )
    }
}
