package com.bck.handshakebet.feature.friends.data.repository

import com.bck.handshakebet.feature.friends.data.remote.FriendshipRemoteSource
import com.bck.handshakebet.feature.friends.domain.model.Friend
import com.bck.handshakebet.feature.friends.domain.model.FriendRequest
import com.bck.handshakebet.feature.friends.domain.model.FriendshipsData
import com.bck.handshakebet.feature.friends.domain.repository.FriendshipRepository
import com.bck.handshakebet.feature.home.domain.model.UserSummary
import javax.inject.Inject

/**
 * Production implementation of [FriendshipRepository].
 *
 * Delegates network operations to [FriendshipRemoteSource] and maps raw DTOs
 * to domain models. Exceptions are translated into user-friendly messages
 * wrapped in [Result.failure].
 *
 * @property remoteSource The data source wrapping Supabase friendship queries.
 */
class FriendshipRepositoryImpl @Inject constructor(
    private val remoteSource: FriendshipRemoteSource
) : FriendshipRepository {

    override suspend fun loadFriendships(): Result<FriendshipsData> = runCatching {
        val currentUserId = remoteSource.currentUserId()
            ?: error("No active session")

        val (friendships, displayNameById) = remoteSource.fetchAllFriendships()

        val friends         = mutableListOf<Friend>()
        val incomingRequests = mutableListOf<FriendRequest>()
        val sentRequests    = mutableListOf<FriendRequest>()

        for (row in friendships) {
            val isRequester = row.requesterId == currentUserId
            val otherPartyId = if (isRequester) row.recipientId else row.requesterId
            val otherName    = displayNameById[otherPartyId] ?: continue // skip if user not found

            when (row.status) {
                "accepted" -> friends.add(
                    Friend(
                        friendshipId = row.id,
                        userId       = otherPartyId,
                        displayName  = otherName
                    )
                )
                "pending"  -> {
                    val request = FriendRequest(
                        friendshipId         = row.id,
                        otherUserId          = otherPartyId,
                        otherUserDisplayName = otherName,
                        isIncoming           = !isRequester
                    )
                    if (isRequester) sentRequests.add(request) else incomingRequests.add(request)
                }
                // "rejected" rows are silently ignored — neither party needs to see them.
            }
        }

        FriendshipsData(
            friends          = friends.sortedBy { it.displayName },
            incomingRequests = incomingRequests,
            sentRequests     = sentRequests
        )
    }.recoverCatching { cause ->
        throw Exception(toFriendlyMessage(cause), cause)
    }

    override suspend fun sendFriendRequest(recipientId: String): Result<Unit> = runCatching {
        remoteSource.sendFriendRequest(recipientId)
    }.recoverCatching { cause ->
        throw Exception(toFriendlyMessage(cause), cause)
    }

    override suspend fun acceptFriendRequest(friendshipId: String): Result<Unit> = runCatching {
        remoteSource.acceptFriendRequest(friendshipId)
    }.recoverCatching { cause ->
        throw Exception(toFriendlyMessage(cause), cause)
    }

    override suspend fun rejectFriendRequest(friendshipId: String): Result<Unit> = runCatching {
        remoteSource.rejectFriendRequest(friendshipId)
    }.recoverCatching { cause ->
        throw Exception(toFriendlyMessage(cause), cause)
    }

    override suspend fun removeFriendship(friendshipId: String): Result<Unit> = runCatching {
        remoteSource.removeFriendship(friendshipId)
    }.recoverCatching { cause ->
        throw Exception(toFriendlyMessage(cause), cause)
    }

    override suspend fun searchFriends(query: String): Result<List<UserSummary>> = runCatching {
        remoteSource.searchFriends(query).map { UserSummary(id = it.id, displayName = it.displayName) }
    }.recoverCatching { cause ->
        throw Exception(toFriendlyMessage(cause), cause)
    }

    // ── Error handling ────────────────────────────────────────────────────────

    private fun toFriendlyMessage(cause: Throwable): String {
        val msg = cause.message?.lowercase() ?: return "Something went wrong. Please try again."
        return when {
            "network" in msg || "connect" in msg  -> "No internet connection. Please check your network."
            "timeout" in msg                      -> "Request timed out. Please try again."
            "jwt" in msg || "unauthori" in msg    -> "Your session has expired. Please sign in again."
            "duplicate" in msg || "unique" in msg -> "A friend request already exists with this user."
            else                                  -> "Something went wrong. Please try again."
        }
    }
}
