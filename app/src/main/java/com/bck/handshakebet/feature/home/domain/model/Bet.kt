package com.bck.handshakebet.feature.home.domain.model

/**
 * Domain representation of a bet between two users.
 *
 * This model is feature-agnostic and is shared across the Home, Account, and
 * BetDetail features. It is mapped from [com.bck.handshakebet.feature.home.data.remote.SupabaseBet]
 * inside `BetRepositoryImpl` and must never reference any Supabase SDK types.
 *
 * @property id                    Unique bet identifier (UUID from Supabase).
 * @property title                 Short title describing the wager.
 * @property description           Optional longer description of the bet terms.
 * @property creatorId             User ID of the bet's initiator.
 * @property creatorDisplayName    Display name of the initiator (denormalised for display).
 * @property opponentId            User ID of the challenged party, or `null` if unset.
 * @property opponentDisplayName   Display name of the opponent, or `null` if unset.
 * @property status                Current lifecycle state of the bet.
 * @property isPublic              Whether the bet appears in the public feed.
 * @property winnerId              User ID of the winner, or `null` if not yet determined.
 * @property createdAt             ISO-8601 timestamp of creation.
 */
data class Bet(
    val id: String,
    val title: String,
    val description: String,
    val creatorId: String,
    val creatorDisplayName: String,
    val opponentId: String?,
    val opponentDisplayName: String?,
    val status: BetStatus,
    val isPublic: Boolean,
    val winnerId: String?,
    val createdAt: String
)
