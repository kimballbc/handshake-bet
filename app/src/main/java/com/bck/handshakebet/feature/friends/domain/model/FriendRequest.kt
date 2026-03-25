package com.bck.handshakebet.feature.friends.domain.model

/**
 * Domain model representing a pending or rejected friendship request.
 *
 * A row in `friendships` whose `status` is `"pending"` maps to a [FriendRequest].
 * [isIncoming] distinguishes the two directions:
 * - `true`  → the other user sent the request (the current user is the addressee/recipient).
 * - `false` → the current user sent the request (and is waiting for a response).
 *
 * Only the addressee may accept or reject an incoming request; only the requester
 * may withdraw a sent request.
 *
 * @property friendshipId        UUID of the `friendships` row.
 * @property otherUserId         Supabase Auth UUID of the other party.
 * @property otherUserDisplayName Display name of the other party.
 * @property isIncoming          `true` when the current user is the recipient.
 */
data class FriendRequest(
    val friendshipId: String,
    val otherUserId: String,
    val otherUserDisplayName: String,
    val isIncoming: Boolean
)
