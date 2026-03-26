package com.bck.handshakebet.feature.records.domain.model

/**
 * Outcome of a completed bet from the current user's perspective.
 */
enum class BetOutcome {
    /** The current user was recorded as the winner. */
    WIN,
    /** The bet was declared a draw (winner_id == "draw"). */
    DRAW,
    /** Another participant was recorded as the winner. */
    LOSS
}
