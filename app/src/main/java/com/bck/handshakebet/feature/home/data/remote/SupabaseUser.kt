package com.bck.handshakebet.feature.home.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object mapping the `public.users` table in Supabase.
 *
 * This table is populated automatically via the `on_auth_user_created` trigger
 * whenever a new user signs up. It provides a searchable, public-facing view of
 * registered users for bet opponent selection.
 *
 * Converted to [com.bck.handshakebet.feature.home.domain.model.UserSummary] by
 * [com.bck.handshakebet.feature.home.data.repository.UserRepositoryImpl].
 *
 * @property id          Supabase Auth UUID — mirrors `auth.users.id`.
 * @property displayName The user's chosen display name.
 */
@Serializable
data class SupabaseUser(
    @SerialName("id")           val id: String,
    @SerialName("display_name") val displayName: String
)
