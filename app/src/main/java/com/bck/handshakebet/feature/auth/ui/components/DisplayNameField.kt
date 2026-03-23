package com.bck.handshakebet.feature.auth.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Stateless display name input field shown only during sign-up.
 *
 * Capitalises words automatically via [KeyboardCapitalization.Words] so the
 * user's name is formatted correctly without manual effort.
 *
 * @param value Current text field value.
 * @param onValueChange Called on every keystroke with the updated value.
 * @param onNext Called when the user presses the "Next" IME action.
 * @param errorMessage Inline error to show below the field, or null for no error.
 * @param modifier Optional [Modifier].
 */
@Composable
fun DisplayNameField(
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
        label = { Text("Display Name") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        isError = errorMessage != null,
        supportingText = errorMessage?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { onNext() }),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
private fun DisplayNameFieldPreview() {
    HandshakeBetTheme {
        DisplayNameField(value = "Ben Kimball", onValueChange = {}, onNext = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DisplayNameFieldErrorPreview() {
    HandshakeBetTheme {
        DisplayNameField(
            value = "B",
            onValueChange = {},
            onNext = {},
            errorMessage = "Display name must be at least 2 characters"
        )
    }
}
