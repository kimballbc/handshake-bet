package com.bck.handshakebet.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bck.handshakebet.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the Profile screen.
 *
 * @property displayName  The current user's display name, or `null` before it loads.
 * @property isSigningOut `true` while a sign-out request is in flight.
 * @property errorMessage A one-shot error to show the user, then clear.
 */
data class ProfileUiState(
    val displayName: String?  = null,
    val isSigningOut: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for the Profile screen.
 *
 * Loads the current user's display name from the Auth session and handles
 * sign-out, navigating back to Login on success via [onSignedOut].
 *
 * @property authRepository Auth domain contract.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(displayName = authRepository.getCurrentUser()?.displayName) }
    }

    /**
     * Signs the current user out and invokes [onSignedOut] on success.
     *
     * Any network error is surfaced as [ProfileUiState.errorMessage].
     */
    fun onSignOutClicked(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = true, errorMessage = null) }
            authRepository.signOut()
                .onSuccess { onSignedOut() }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSigningOut = false,
                            errorMessage = e.message ?: "Sign out failed. Please try again."
                        )
                    }
                }
        }
    }

    /** Clears the transient error after the UI has shown it. */
    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
