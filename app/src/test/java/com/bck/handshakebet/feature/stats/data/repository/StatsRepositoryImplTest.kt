package com.bck.handshakebet.feature.stats.data.repository

import com.bck.handshakebet.feature.home.data.remote.SupabaseBet
import com.bck.handshakebet.feature.records.data.remote.RecordsRemoteSource
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserInfo
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StatsRepositoryImplTest {

    companion object {
        private const val USER_ID     = "user-abc"
        private const val OPPONENT_ID = "user-xyz"
        private const val OPPONENT_NAME = "Alex"
    }

    private val remoteSource: RecordsRemoteSource = mockk()
    private val auth: Auth = mockk()
    private lateinit var repository: StatsRepositoryImpl

    @Before
    fun setUp() {
        val userInfo: UserInfo = mockk { every { id } returns USER_ID }
        every { auth.currentUserOrNull() } returns userInfo
        repository = StatsRepositoryImpl(remoteSource, auth)
    }

    // ── Empty state ───────────────────────────────────────────────────────────

    @Test
    fun `empty bet list returns all-zero StatsData`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns emptyList()

        val data = repository.loadStats().getOrThrow()

        assertEquals(0, data.totalCompleted)
        assertNull(data.winRate)
        assertEquals(0, data.currentStreak)
        assertEquals(0, data.bestWinStreak)
        assertNull(data.averagePrideWagered)
        assertNull(data.topOpponent)
    }

    // ── Win rate ──────────────────────────────────────────────────────────────

    @Test
    fun `win rate is wins divided by wins plus losses`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            win(prideWagered = 10),
            win(prideWagered = 10),
            loss(prideWagered = 10)
        )

        val data = repository.loadStats().getOrThrow()

        assertEquals(2f / 3f, data.winRate!!, 0.001f)
    }

    @Test
    fun `draws are excluded from win rate denominator`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            win(),
            draw(),
            draw()
        )

        val data = repository.loadStats().getOrThrow()

        // 1 win, 0 losses → 100% (draws ignored)
        assertEquals(1.0f, data.winRate!!, 0.001f)
    }

    @Test
    fun `all draws produces null win rate`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(draw(), draw())

        val data = repository.loadStats().getOrThrow()

        assertNull(data.winRate)
    }

    // ── Streaks ───────────────────────────────────────────────────────────────

    @Test
    fun `consecutive wins produce positive current streak`() = runTest {
        // Stored newest-first; reversed inside impl for streak calc
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            win(id = "b3"),  // most recent
            win(id = "b2"),
            win(id = "b1")   // oldest
        )

        val data = repository.loadStats().getOrThrow()

        assertEquals(3, data.currentStreak)
        assertEquals(3, data.bestWinStreak)
    }

    @Test
    fun `consecutive losses produce negative current streak`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            loss(id = "b3"),
            loss(id = "b2"),
            loss(id = "b1")
        )

        val data = repository.loadStats().getOrThrow()

        assertEquals(-3, data.currentStreak)
        assertEquals(0, data.bestWinStreak)
    }

    @Test
    fun `draw resets streak to zero`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            win(id = "b3"),   // most recent
            draw(id = "b2"),  // resets
            win(id = "b1")
        )

        val data = repository.loadStats().getOrThrow()

        assertEquals(1, data.currentStreak)
        assertEquals(1, data.bestWinStreak)  // best was 1 before draw
    }

    @Test
    fun `loss after wins ends win streak and current streak is negative`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            loss(id = "b4"),  // most recent — ends streak
            win(id  = "b3"),
            win(id  = "b2"),
            win(id  = "b1")
        )

        val data = repository.loadStats().getOrThrow()

        assertEquals(-1, data.currentStreak)
        assertEquals(3, data.bestWinStreak)
    }

    @Test
    fun `best win streak is tracked independently of current streak`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            loss(id = "b6"),  // most recent
            loss(id = "b5"),
            win(id  = "b4"),
            win(id  = "b3"),
            win(id  = "b2"),
            win(id  = "b1")   // oldest
        )

        val data = repository.loadStats().getOrThrow()

        assertEquals(-2, data.currentStreak)
        assertEquals(4,  data.bestWinStreak)
    }

    // ── Average wager ─────────────────────────────────────────────────────────

    @Test
    fun `average pride wagered is correct`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            win(prideWagered = 10),
            loss(prideWagered = 20),
            draw(prideWagered = 30)
        )

        val data = repository.loadStats().getOrThrow()

        assertEquals(20f, data.averagePrideWagered!!, 0.001f)
    }

    // ── Top opponent ──────────────────────────────────────────────────────────

    @Test
    fun `top opponent is the one with most completed bets`() = runTest {
        val other = "other-id"
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            win(id = "b1", opponentId = OPPONENT_ID, opponentName = OPPONENT_NAME),
            win(id = "b2", opponentId = OPPONENT_ID, opponentName = OPPONENT_NAME),
            loss(id = "b3", opponentId = other, opponentName = "Bob")
        )

        val data = repository.loadStats().getOrThrow()

        assertEquals(OPPONENT_NAME, data.topOpponent?.displayName)
        assertEquals(2, data.topOpponent?.totalBets)
    }

    @Test
    fun `top opponent win and loss counts are correct`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } returns listOf(
            win(id  = "b1", opponentId = OPPONENT_ID, opponentName = OPPONENT_NAME),
            win(id  = "b2", opponentId = OPPONENT_ID, opponentName = OPPONENT_NAME),
            loss(id = "b3", opponentId = OPPONENT_ID, opponentName = OPPONENT_NAME)
        )

        val data = repository.loadStats().getOrThrow()

        assertEquals(2, data.topOpponent?.wins)
        assertEquals(1, data.topOpponent?.losses)
    }

    // ── Error handling ────────────────────────────────────────────────────────

    @Test
    fun `remote source exception produces user-friendly failure`() = runTest {
        coEvery { remoteSource.fetchCompletedBets() } throws RuntimeException("timeout")

        val result = repository.loadStats()

        assertTrue(result.isFailure)
        assertEquals("Could not load your stats. Please try again.", result.exceptionOrNull()?.message)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun win(
        id           : String = "bet-w",
        opponentId   : String = OPPONENT_ID,
        opponentName : String = OPPONENT_NAME,
        prideWagered : Int    = 10
    ) = makeBet(id, opponentId, opponentName, winnerId = USER_ID, prideWagered = prideWagered)

    private fun loss(
        id           : String = "bet-l",
        opponentId   : String = OPPONENT_ID,
        opponentName : String = OPPONENT_NAME,
        prideWagered : Int    = 10
    ) = makeBet(id, opponentId, opponentName, winnerId = opponentId, prideWagered = prideWagered)

    private fun draw(
        id           : String = "bet-d",
        opponentId   : String = OPPONENT_ID,
        opponentName : String = OPPONENT_NAME,
        prideWagered : Int    = 10
    ) = makeBet(id, opponentId, opponentName, winnerId = "draw", prideWagered = prideWagered)

    private fun makeBet(
        id           : String,
        opponentId   : String,
        opponentName : String,
        winnerId     : String,
        prideWagered : Int
    ) = SupabaseBet(
        id                   = id,
        title                = "Test bet",
        creatorId            = USER_ID,
        creatorDisplayName   = "Me",
        opponentId           = opponentId,
        opponentDisplayName  = opponentName,
        status               = "completed",
        isPublic             = false,
        prideWagered         = prideWagered,
        winnerId             = winnerId,
        createdAt            = "2024-01-01T00:00:00Z"
    )
}
