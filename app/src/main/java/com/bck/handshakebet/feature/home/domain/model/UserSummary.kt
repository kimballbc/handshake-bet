package com.bck.handshakebet.feature.home.domain.model

/**
 * Lightweight domain model used when referencing another user as a bet opponent.
 *
 * Intentionally minimal — only the fields required for opponent selection and
 * denormalised bet storage. The full user profile lives in [com.bck.handshakebet.core.domain.model.User].
 *
 * Phase 5 note: [UserRepository.searchUsers] currently searches all registered
 * users. Once friends are introduced in Phase 5, search should be scoped to the
 * current user's friends list.
 *
 * @property id          Supabase Auth UUID of the user.
 * @property displayName The user's chosen display name.
 */
data class UserSummary(
    val id: String,
    val displayName: String
)
