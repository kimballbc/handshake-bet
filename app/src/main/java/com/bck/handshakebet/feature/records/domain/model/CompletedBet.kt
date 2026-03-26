package com.bck.handshakebet.feature.records.domain.model

/**
 * A completed bet as seen from the current user's perspective.
 *
 * @property id                  Unique bet identifier.
 * @property title               Short description of the wager.
 * @property opponentDisplayName Display name of the other participant.
 * @property prideWagered        Amount of pride that was on the line.
 * @property outcome             Whether the current user won, drew, or lost.
 * @property createdAt           ISO-8601 timestamp of creation.
 */
data class CompletedBet(
    val id: String,
    val title: String,
    val opponentDisplayName: String,
    val prideWagered: Int,
    val outcome: BetOutcome,
    val createdAt: String
)
