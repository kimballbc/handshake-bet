package com.bck.handshakebet.feature.records.domain.repository

import com.bck.handshakebet.feature.records.domain.model.RecordsData

/**
 * Contract for fetching the current user's records and bet history.
 *
 * The implementation computes wins/draws/losses directly from the `bets`
 * table rather than relying on the (currently empty) `user_records` table.
 */
interface RecordsRepository {

    /**
     * Loads all completed bets for the current user and computes their
     * win/draw/loss record and pride balance.
     *
     * @return [Result.success] with [RecordsData] on success,
     *   or [Result.failure] with a user-friendly message on error.
     */
    suspend fun loadRecords(): Result<RecordsData>
}
