package com.bck.handshakebet.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bck.handshakebet.feature.auth.domain.model.SignUpOutcome
import com.bck.handshakebet.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the authentication screen.
 *
 * Owns all auth presentation logic including input validation, calling the
 * [AuthRepository], and translating outcomes into [AuthUiState] transitions.
 * The screen composable observes [uiState] and delegates every user action
 * back here — it contains no business logic of its own.
 *
 * Validation runs client-side before any network call is made so the user
 * gets instant feedback without a round trip.
 *
 * @property authRepository The domain repository for authentication operations.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    /**
     * The current UI state. Collected by [LoginScreen] via
     * `collectAsStateWithLifecycle()`.
     */
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Public events ─────────────────────────────────────────────────────────

    /**
     * Called when the user submits the login form.
     *
     * Validates [email] and [password] locally first. If validation passes,
     * emits [AuthUiState.Loading] then calls the repository. On success,
     * emits [AuthUiState.Success]; on failure, emits [AuthUiState.Error].
     *
     * @param email The raw email string from the text field (trimmed internally).
     * @param password The raw password string from the text field.
     */
    fun onLoginClicked(email: String, password: String) {
        val trimmedEmail = email.trim()
        val emailError = validateEmail(trimmedEmail)
        val passwordError = validatePasswordForLogin(password)

        if (emailError != null || passwordError != null) {
            _uiState.value = AuthUiState.Error(emailError ?: passwordError ?: "Invalid input")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signIn(trimmedEmail, password)
                .onSuccess { user -> _uiState.value = AuthUiState.Success(user) }
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: "Sign in failed") }
        }
    }

    /**
     * Called when the user submits the sign-up form.
     *
     * Validates all three fields locally first. If validation passes, emits
     * [AuthUiState.Loading] then calls the repository. On success, emits either
     * [AuthUiState.Success] (immediate sign-in) or [AuthUiState.EmailVerificationSent]
     * (email confirmation required). On failure, emits [AuthUiState.Error].
     *
     * @param email The raw email string from the text field (trimmed internally).
     * @param password The raw password string from the text field.
     * @param displayName The raw display name string (trimmed internally).
     */
    fun onSignUpClicked(email: String, password: String, displayName: String) {
        val trimmedEmail = email.trim()
        val trimmedName = displayName.trim()

        val emailError = validateEmail(trimmedEmail)
        val passwordError = validatePasswordForSignUp(password)
        val nameError = validateDisplayName(trimmedName)

        if (emailError != null || passwordError != null || nameError != null) {
            _uiState.value = AuthUiState.Error(
                emailError ?: passwordError ?: nameError ?: "Invalid input"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signUp(trimmedEmail, password, trimmedName)
                .onSuccess { outcome ->
                    _uiState.value = when (outcome) {
                        is SignUpOutcome.Success -> AuthUiState.Success(outcome.user)
                        SignUpOutcome.EmailVerificationRequired -> AuthUiState.EmailVerificationSent
                    }
                }
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: "Sign up failed") }
        }
    }

    /**
     * Resets the state back to [AuthUiState.Idle].
     * Called after the screen has consumed an error or navigation event.
     */
    fun onStateConsumed() {
        _uiState.value = AuthUiState.Idle
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Validates an email address using a standard RFC 5322 simplified regex.
     * Returns a user-facing error string, or null if the email is valid.
     */
    internal fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email cannot be empty"
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(email)) return "Please enter a valid email address"
        return null
    }

    /**
     * Validates a password for login — only checks that it is non-blank.
     * Returns a user-facing error string, or null if valid.
     */
    internal fun validatePasswordForLogin(password: String): String? {
        if (password.isBlank()) return "Password cannot be empty"
        return null
    }

    /**
     * Validates a password for sign-up — requires at least 6 characters.
     * Returns a user-facing error string, or null if valid.
     */
    internal fun validatePasswordForSignUp(password: String): String? {
        if (password.isBlank()) return "Password cannot be empty"
        if (password.length < 6) return "Password must be at least 6 characters"
        return null
    }

    /**
     * Validates a display name — requires at least 2 non-blank characters.
     * Returns a user-facing error string, or null if valid.
     */
    internal fun validateDisplayName(name: String): String? {
        if (name.isBlank()) return "Display name cannot be empty"
        if (name.length < 2) return "Display name must be at least 2 characters"
        return null
    }
}
