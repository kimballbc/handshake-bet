package com.bck.handshakebet.feature.home.domain.model

/**
 * Represents the lifecycle state of a [Bet].
 *
 * - [PENDING]   — Created by the initiator; awaiting the opponent's response.
 * - [ACTIVE]    — Accepted by both parties; the bet is in progress.
 * - [COMPLETED] — One party has been declared the winner.
 * - [REJECTED]  — The opponent declined the bet.
 * - [CANCELLED] — The initiator withdrew the bet before acceptance.
 */
enum class BetStatus {
    PENDING,
    ACTIVE,
    COMPLETED,
    REJECTED,
    CANCELLED;

    companion object {
        /**
         * Converts a raw Supabase string value to a [BetStatus], defaulting to
         * [PENDING] if the value is unrecognised.
         */
        fun fromString(value: String): BetStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
    }
}
