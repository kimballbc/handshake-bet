package com.bck.handshakebet.feature.friends.domain.model

/**
 * Domain model representing an accepted friend of the current user.
 *
 * Extracted from a [com.bck.handshakebet.feature.friends.data.remote.SupabaseFriendship]
 * whose `status` is `"accepted"`. Surfaces only the data the UI needs —
 * the friendship row ID (for removal) and the friend's public profile.
 *
 * @property friendshipId The UUID of the `friendships` row (used for removal).
 * @property userId       The friend's Supabase Auth UUID.
 * @property displayName  The friend's chosen display name.
 */
data class Friend(
    val friendshipId: String,
    val userId: String,
    val displayName: String
)
