package com.bck.handshakebet.feature.records.data.repository

import com.bck.handshakebet.feature.home.data.remote.SupabaseBet
import com.bck.handshakebet.feature.records.data.remote.RecordsRemoteSource
import com.bck.handshakebet.feature.records.domain.model.BetOutcome
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserInfo
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the outcome-classification and pride-balance math in
 * [RecordsRepositoryImpl].
 *
 * The Auth mock always reports currentUserId = USER_ID so tests can reason
 * clearly about who "won" each fabricated bet.
 */
class RecordsRepositoryImplTest {

    companion object {
        private const val USER_ID    = "user-abc"
        private const val OPPONENT_ID = "user-xyz"
    }

    private val remoteSource: RecordsRemoteSource = mockk()
    private val auth: Auth = mockk()

    private lateinit var repository: RecordsRepositoryImpl

    @Before
    fun setUp() {
        val userInfo: UserInfo = mockk { every { id } returns USER_ID }
        every { auth.currentUserOrNull() } returns userInfo
        repository = RecordsRepositoryImpl(remoteSource, auth)
    }

    // ── Outcome classification ────────────────────────────────────────────────

    @Test
    fun `bet where winner_id equals current user is WIN`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(creatorId = USER_ID, opponentId = OPPONENT_ID, winnerId = USER_ID)
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals(1, data.wins)
        assertEquals(0, data.draws)
        assertEquals(0, data.losses)
        assertEquals(BetOutcome.WIN, data.completedBets.first().outcome)
    }

    @Test
    fun `bet where winner_id is draw sentinel is DRAW`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(creatorId = USER_ID, opponentId = OPPONENT_ID, winnerId = "draw")
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals(0, data.wins)
        assertEquals(1, data.draws)
        assertEquals(0, data.losses)
        assertEquals(BetOutcome.DRAW, data.completedBets.first().outcome)
    }

    @Test
    fun `bet where winner_id is opponent is LOSS`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(creatorId = USER_ID, opponentId = OPPONENT_ID, winnerId = OPPONENT_ID)
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals(0, data.wins)
        assertEquals(0, data.draws)
        assertEquals(1, data.losses)
        assertEquals(BetOutcome.LOSS, data.completedBets.first().outcome)
    }

    @Test
    fun `user as opponent who wins is classified as WIN`() = runTest {
        // Current user is the opponent, not the creator
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(creatorId = OPPONENT_ID, opponentId = USER_ID, winnerId = USER_ID)
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals(1, data.wins)
        assertEquals(BetOutcome.WIN, data.completedBets.first().outcome)
    }

    @Test
    fun `user as opponent who loses is classified as LOSS`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(creatorId = OPPONENT_ID, opponentId = USER_ID, winnerId = OPPONENT_ID)
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals(1, data.losses)
        assertEquals(BetOutcome.LOSS, data.completedBets.first().outcome)
    }

    // ── Pride balance math ────────────────────────────────────────────────────

    @Test
    fun `single win adds prideWagered to balance`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(winnerId = USER_ID, prideWagered = 15)
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals(15, data.prideBalance)
    }

    @Test
    fun `single loss subtracts prideWagered from balance`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(winnerId = OPPONENT_ID, prideWagered = 15)
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals(-15, data.prideBalance)
    }

    @Test
    fun `draw does not affect pride balance`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(winnerId = "draw", prideWagered = 10)
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals(0, data.prideBalance)
    }

    @Test
    fun `mixed results compute correct net balance`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(id = "b1", winnerId = USER_ID,    prideWagered = 10),  // +10
            makeBet(id = "b2", winnerId = USER_ID,    prideWagered = 20),  // +20
            makeBet(id = "b3", winnerId = "draw",     prideWagered = 5),   //   0
            makeBet(id = "b4", winnerId = OPPONENT_ID, prideWagered = 8),  //  -8
            makeBet(id = "b5", winnerId = OPPONENT_ID, prideWagered = 12)  // -12
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals(2, data.wins)
        assertEquals(1, data.draws)
        assertEquals(2, data.losses)
        assertEquals(10, data.prideBalance)   // 30 − 20 = 10
    }

    @Test
    fun `all wins yields fully positive balance`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(id = "b1", winnerId = USER_ID, prideWagered = 5),
            makeBet(id = "b2", winnerId = USER_ID, prideWagered = 5),
            makeBet(id = "b3", winnerId = USER_ID, prideWagered = 5)
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals(3, data.wins)
        assertEquals(0, data.losses)
        assertEquals(15, data.prideBalance)
    }

    @Test
    fun `all losses yields fully negative balance`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(id = "b1", winnerId = OPPONENT_ID, prideWagered = 7),
            makeBet(id = "b2", winnerId = OPPONENT_ID, prideWagered = 3)
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals(0, data.wins)
        assertEquals(2, data.losses)
        assertEquals(-10, data.prideBalance)
    }

    @Test
    fun `empty bet list produces all zeros`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns emptyList()

        val data = repository.loadRecords().getOrThrow()

        assertEquals(0, data.wins)
        assertEquals(0, data.draws)
        assertEquals(0, data.losses)
        assertEquals(0, data.prideBalance)
        assertTrue(data.completedBets.isEmpty())
    }

    // ── Opponent name resolution ──────────────────────────────────────────────

    @Test
    fun `when current user is creator opponent name comes from opponentDisplayName`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(
                creatorId            = USER_ID,
                creatorDisplayName   = "Me",
                opponentId           = OPPONENT_ID,
                opponentDisplayName  = "Alex",
                winnerId             = USER_ID
            )
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals("Alex", data.completedBets.first().opponentDisplayName)
    }

    @Test
    fun `when current user is opponent opponent name comes from creatorDisplayName`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            makeBet(
                creatorId            = OPPONENT_ID,
                creatorDisplayName   = "Sam",
                opponentId           = USER_ID,
                opponentDisplayName  = "Me",
                winnerId             = USER_ID
            )
        )

        val data = repository.loadRecords().getOrThrow()

        assertEquals("Sam", data.completedBets.first().opponentDisplayName)
    }

    // ── Error handling ────────────────────────────────────────────────────────

    @Test
    fun `remote source exception is translated to user-friendly Result failure`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } throws RuntimeException("timeout")

        val result = repository.loadRecords()

        assertTrue(result.isFailure)
        assertEquals("Could not load your records. Please try again.", result.exceptionOrNull()?.message)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun makeBet(
        id                  : String  = "bet-1",
        creatorId           : String  = USER_ID,
        creatorDisplayName  : String  = "Me",
        opponentId          : String  = OPPONENT_ID,
        opponentDisplayName : String  = "Opponent",
        winnerId            : String  = USER_ID,
        prideWagered        : Int     = 10
    ) = SupabaseBet(
        id                   = id,
        title                = "Test bet",
        description          = "",
        creatorId            = creatorId,
        creatorDisplayName   = creatorDisplayName,
        opponentId           = opponentId,
        opponentDisplayName  = opponentDisplayName,
        status               = "completed",
        isPublic             = false,
        prideWagered         = prideWagered,
        winnerId             = winnerId,
        createdAt            = "2024-01-01T00:00:00Z"
    )
}
