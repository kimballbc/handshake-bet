package com.bck.handshakebet.feature.auth.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * Stateless password input field with a show/hide visibility toggle.
 *
 * Visibility state is managed internally with [rememberSaveable] so it
 * survives recomposition and configuration changes without being hoisted.
 *
 * @param value Current text field value.
 * @param onValueChange Called on every keystroke with the updated value.
 * @param onDone Called when the user presses the "Done" IME action.
 * @param errorMessage Inline error to show below the field, or null for no error.
 * @param label Label text shown above the field. Defaults to "Password".
 * @param imeAction The IME action for the keyboard. Defaults to [ImeAction.Done].
 * @param modifier Optional [Modifier].
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    errorMessage: String? = null,
    label: String = "Password",
    imeAction: ImeAction = ImeAction.Done,
    modifier: Modifier = Modifier
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
            val description = if (passwordVisible) "Hide password" else "Show password"
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(icon, contentDescription = description)
            }
        },
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        isError = errorMessage != null,
        supportingText = errorMessage?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
private fun PasswordFieldPreview() {
    HandshakeBetTheme {
        PasswordField(value = "secret123", onValueChange = {}, onDone = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PasswordFieldErrorPreview() {
    HandshakeBetTheme {
        PasswordField(
            value = "abc",
            onValueChange = {},
            onDone = {},
            errorMessage = "Password must be at least 6 characters"
        )
    }
}
