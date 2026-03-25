package com.bck.handshakebet.feature.friends.domain.model

/**
 * Aggregates all friendship-related data loaded in a single pass.
 *
 * The repository fetches all rows from `friendships` where the current user
 * appears as either requester or recipient, then partitions them into the
 * three lists below. This avoids three separate network requests.
 *
 * @property friends          Accepted friends of the current user.
 * @property incomingRequests Pending requests addressed to the current user.
 * @property sentRequests     Pending requests sent by the current user.
 */
data class FriendshipsData(
    val friends: List<Friend>,
    val incomingRequests: List<FriendRequest>,
    val sentRequests: List<FriendRequest>
)
