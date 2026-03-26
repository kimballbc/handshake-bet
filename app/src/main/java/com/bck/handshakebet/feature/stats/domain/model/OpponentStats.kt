package com.bck.handshakebet.feature.stats.domain.model

/**
 * Win/loss record against a single opponent.
 *
 * @property displayName Display name of the opponent.
 * @property totalBets   Total completed bets between the two users.
 * @property wins        Bets the current user won against this opponent.
 * @property losses      Bets the current user lost against this opponent.
 */
data class OpponentStats(
    val displayName: String,
    val totalBets: Int,
    val wins: Int,
    val losses: Int
)
