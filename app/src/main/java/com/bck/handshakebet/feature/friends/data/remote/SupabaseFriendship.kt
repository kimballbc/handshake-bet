package com.bck.handshakebet.feature.friends.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object mapping the `public.friendships` table in Supabase.
 *
 * Each row represents a directed friendship request from [requesterId] to
 * [recipientId]. The [status] field tracks the lifecycle:
 * - `"pending"`  — request sent, awaiting response.
 * - `"accepted"` — both parties are friends.
 * - `"rejected"` — the recipient declined the request.
 *
 * Converted to [com.bck.handshakebet.feature.friends.domain.model.Friend] or
 * [com.bck.handshakebet.feature.friends.domain.model.FriendRequest] by
 * [com.bck.handshakebet.feature.friends.data.repository.FriendshipRepositoryImpl].
 *
 * @property id          UUID primary key of the friendship row.
 * @property requesterId UUID of the user who sent the request.
 * @property recipientId UUID of the user who received the request.
 * @property status      Current status: `"pending"`, `"accepted"`, or `"rejected"`.
 */
@Serializable
data class SupabaseFriendship(
    @SerialName("id")           val id: String,
    @SerialName("requester_id") val requesterId: String,
    @SerialName("recipient_id") val recipientId: String,
    @SerialName("status")       val status: String
)
