package com.bck.handshakebet.feature.home.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object mapping the `bets` table in Supabase.
 *
 * Field names use `snake_case` via [@SerialName] to match Supabase column names.
 * This class must remain a pure data container — no business logic belongs here.
 * Use [com.bck.handshakebet.feature.home.data.repository.BetRepositoryImpl.toDomain]
 * to convert to the [com.bck.handshakebet.feature.home.domain.model.Bet] domain model.
 *
 * @property id                  Supabase UUID primary key.
 * @property title               Short wager description.
 * @property description         Full terms of the bet.
 * @property creatorId           UUID of the user who created the bet.
 * @property creatorDisplayName  Denormalised display name of the creator.
 * @property opponentId          UUID of the challenged user, or `null`.
 * @property opponentDisplayName Denormalised display name of the opponent, or `null`.
 * @property status              Raw status string (e.g. "pending", "active").
 * @property isPublic            Whether the bet is visible in the public feed.
 * @property winnerId            UUID of the winner, or `null` if undecided.
 * @property createdAt           ISO-8601 creation timestamp.
 */
@Serializable
data class SupabaseBet(
    @SerialName("id")                    val id: String,
    @SerialName("title")                 val title: String,
    @SerialName("description")           val description: String = "",
    @SerialName("creator_id")            val creatorId: String,
    @SerialName("creator_display_name")  val creatorDisplayName: String,
    @SerialName("opponent_id")           val opponentId: String? = null,
    @SerialName("opponent_display_name") val opponentDisplayName: String? = null,
    @SerialName("status")                val status: String = "pending",
    @SerialName("is_public")             val isPublic: Boolean = false,
    @SerialName("winner_id")             val winnerId: String? = null,
    @SerialName("created_at")            val createdAt: String = ""
)
