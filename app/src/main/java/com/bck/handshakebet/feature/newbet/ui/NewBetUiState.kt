package com.bck.handshakebet.feature.newbet.ui

import com.bck.handshakebet.feature.home.domain.model.UserSummary

/**
 * UI state for the New Bet creation screen.
 *
 * Unlike most screens in the app, this screen has a rich form with multiple
 * concurrent async states (debounced search, submission), so the state is
 * modelled as a single data class rather than a sealed interface.
 *
 * @property title            Current value of the bet title field.
 * @property description      Current value of the optional description field.
 * @property isPublic         Whether the bet should appear in the public feed.
 * @property wagerAmount      Current text in the pride wagered field.
 * @property wagerError       Inline validation error for the wager field, or `null`.
 * @property searchQuery      Current text in the opponent search field.
 * @property searchResults    Most recent list of matching users.
 * @property selectedOpponent The user the current user wants to challenge, or `null`.
 * @property isSearching      `true` while the user-search network call is in-flight.
 * @property isSubmitting     `true` while the create-bet network call is in-flight.
 * @property errorMessage     A transient error to show once then clear.
 * @property isSuccess        `true` after the bet is created — triggers navigation away.
 * @property sliderResetKey   Incremented on submission failure to force the
 *                            [HandshakeSlider][com.bck.handshakebet.core.ui.components.HandshakeSlider]
 *                            to reset its internal drag state via `key()`.
 */
data class NewBetUiState(
    val title: String = "",
    val description: String = "",
    val isPublic: Boolean = false,
    val wagerAmount: String = "",
    val wagerError: String? = null,
    val searchQuery: String = "",
    val searchResults: List<UserSummary> = emptyList(),
    val selectedOpponent: UserSummary? = null,
    val isSearching: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val sliderResetKey: Int = 0
) {
    /** Parsed wager, or `null` when the field is blank or non-numeric. */
    val parsedWager: Int? get() = wagerAmount.trim().toIntOrNull()

    /** `true` when the wager is a whole number in the range 1–100. */
    val isWagerValid: Boolean get() = parsedWager?.let { it in 1..100 } ?: false

    /**
     * `true` when all required fields are valid and no request is in-flight.
     * The [HandshakeSlider][com.bck.handshakebet.core.ui.components.HandshakeSlider]
     * is only enabled when this is `true`.
     */
    val canSubmit: Boolean get() =
        title.isNotBlank() && selectedOpponent != null && isWagerValid && !isSubmitting
}
