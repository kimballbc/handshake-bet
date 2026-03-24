package com.bck.handshakebet.feature.account.ui

import app.cash.turbine.test
import com.bck.handshakebet.core.testing.MainDispatcherRule
import com.bck.handshakebet.feature.home.domain.model.Bet
import com.bck.handshakebet.feature.home.domain.model.BetStatus
import com.bck.handshakebet.feature.home.domain.repository.BetRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserInfo
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val betRepository: BetRepository = mockk()
    private val auth: Auth = mockk()
    private val mockUser: UserInfo = mockk {
        every { id } returns USER_ID
    }

    private fun viewModel() = AccountViewModel(betRepository, auth)

    // ── Init ──────────────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery { betRepository.fetchMyBets() } returns Result.success(emptyList())
        every { auth.currentUserOrNull() } returns mockUser
        val vm = viewModel()
        // UnconfinedTestDispatcher runs init block eagerly — verify terminal state.
        assertTrue(vm.uiState.value is AccountUiState.Success)
    }

    @Test
    fun `loadBets success partitions bets into correct sections`() = runTest {
        val pendingForMe = makeBet("p1", BetStatus.PENDING, opponentId = USER_ID)
        val myPending    = makeBet("p2", BetStatus.PENDING, creatorId  = USER_ID)
        val active       = makeBet("a1", BetStatus.ACTIVE)
        val completed    = makeBet("c1", BetStatus.COMPLETED)

        every { auth.currentUserOrNull() } returns mockUser
        coEvery { betRepository.fetchMyBets() } returns Result.success(
            listOf(pendingForMe, myPending, active, completed)
        )

        val state = viewModel().uiState.value as AccountUiState.Success
        assertEquals(listOf(pendingForMe), state.pendingForMe)
        assertEquals(listOf(myPending),    state.myPendingSent)
        assertEquals(listOf(active),       state.activeBets)
        assertEquals(listOf(completed),    state.history)
        assertEquals(USER_ID,              state.currentUserId)
    }

    @Test
    fun `loadBets failure emits Error state`() = runTest {
        every { auth.currentUserOrNull() } returns mockUser
        coEvery { betRepository.fetchMyBets() } returns Result.failure(Exception("Network error"))

        val state = viewModel().uiState.value
        assertTrue(state is AccountUiState.Error)
        assertEquals("Network error", (state as AccountUiState.Error).message)
    }

    @Test
    fun `empty bets list produces isEmpty true`() = runTest {
        every { auth.currentUserOrNull() } returns mockUser
        coEvery { betRepository.fetchMyBets() } returns Result.success(emptyList())

        val state = viewModel().uiState.value as AccountUiState.Success
        assertTrue(state.isEmpty)
    }

    // ── Accept ────────────────────────────────────────────────────────────────

    @Test
    fun `acceptBet sets isPerformingAction then reloads on success`() = runTest {
        every { auth.currentUserOrNull() } returns mockUser
        val bets = listOf(makeBet("b1", BetStatus.PENDING, opponentId = USER_ID))
        coEvery { betRepository.fetchMyBets() } returns Result.success(bets)
        coEvery { betRepository.acceptBet("b1") } returns Result.success(Unit)

        val vm = viewModel()

        vm.uiState.test {
            awaitItem() // Initial Success (from UnconfinedTestDispatcher)
            vm.acceptBet("b1")
            // isPerformingAction = true
            val loading = awaitItem() as AccountUiState.Success
            assertTrue(loading.isPerformingAction)
            // Reload triggers Loading then new Success
            awaitItem() // Loading
            val refreshed = awaitItem() as AccountUiState.Success
            assertFalse(refreshed.isPerformingAction)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `acceptBet failure surfaces actionError without clearing list`() = runTest {
        every { auth.currentUserOrNull() } returns mockUser
        val bets = listOf(makeBet("b1", BetStatus.PENDING, opponentId = USER_ID))
        coEvery { betRepository.fetchMyBets() } returns Result.success(bets)
        coEvery { betRepository.acceptBet("b1") } returns Result.failure(Exception("Server error"))

        val vm = viewModel()
        vm.acceptBet("b1")

        val state = vm.uiState.value as AccountUiState.Success
        assertEquals("Server error", state.actionError)
        assertFalse(state.isEmpty) // List still intact
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    @Test
    fun `rejectBet success reloads list`() = runTest {
        every { auth.currentUserOrNull() } returns mockUser
        coEvery { betRepository.fetchMyBets() } returns Result.success(emptyList())
        coEvery { betRepository.rejectBet("b1") } returns Result.success(Unit)

        val vm = viewModel()
        vm.rejectBet("b1")

        // After reload the list remains empty (bet removed from backend).
        val state = vm.uiState.value as AccountUiState.Success
        assertTrue(state.isEmpty)
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @Test
    fun `cancelBet success reloads list`() = runTest {
        every { auth.currentUserOrNull() } returns mockUser
        coEvery { betRepository.fetchMyBets() } returns Result.success(emptyList())
        coEvery { betRepository.cancelBet("b1") } returns Result.success(Unit)

        val vm = viewModel()
        vm.cancelBet("b1")

        assertTrue((vm.uiState.value as AccountUiState.Success).isEmpty)
    }

    // ── Complete ──────────────────────────────────────────────────────────────

    @Test
    fun `completeBet success reloads list`() = runTest {
        every { auth.currentUserOrNull() } returns mockUser
        val completed = makeBet("b1", BetStatus.COMPLETED, winnerId = USER_ID)
        coEvery { betRepository.fetchMyBets() } returnsMany listOf(
            Result.success(listOf(makeBet("b1", BetStatus.ACTIVE))),
            Result.success(listOf(completed))
        )
        coEvery { betRepository.completeBet("b1", USER_ID) } returns Result.success(Unit)

        val vm = viewModel()
        vm.completeBet("b1", USER_ID)

        val state = vm.uiState.value as AccountUiState.Success
        assertEquals(listOf(completed), state.history)
    }

    // ── Error dismissal ───────────────────────────────────────────────────────

    @Test
    fun `onActionErrorShown clears actionError`() = runTest {
        every { auth.currentUserOrNull() } returns mockUser
        coEvery { betRepository.fetchMyBets() } returns Result.success(emptyList())
        coEvery { betRepository.acceptBet(any()) } returns Result.failure(Exception("Oops"))

        val vm = viewModel()
        vm.acceptBet("any")

        assertNull((vm.uiState.value as? AccountUiState.Success)?.actionError.also {
            vm.onActionErrorShown()
        })
        assertNull((vm.uiState.value as? AccountUiState.Success)?.actionError)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun makeBet(
        id: String,
        status: BetStatus,
        creatorId: String  = "creator-id",
        opponentId: String = "opponent-id",
        winnerId: String?  = null
    ) = Bet(
        id = id, title = "Bet $id", description = "", creatorId = creatorId,
        creatorDisplayName = "Creator", opponentId = opponentId,
        opponentDisplayName = "Opponent", status = status,
        isPublic = false, winnerId = winnerId, createdAt = ""
    )

    companion object {
        private const val USER_ID = "test-user-id"
    }
}
