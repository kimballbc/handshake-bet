package com.bck.handshakebet.feature.records.ui

import com.bck.handshakebet.core.testing.MainDispatcherRule
import com.bck.handshakebet.feature.records.domain.model.BetOutcome
import com.bck.handshakebet.feature.records.domain.model.CompletedBet
import com.bck.handshakebet.feature.records.domain.model.RecordsData
import com.bck.handshakebet.feature.records.domain.repository.RecordsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RecordsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val recordsRepository: RecordsRepository = mockk()

    private val emptyData = RecordsData(
        wins          = 0,
        draws         = 0,
        losses        = 0,
        prideBalance  = 0,
        completedBets = emptyList()
    )

    private fun viewModel(): RecordsViewModel {
        coEvery { recordsRepository.loadRecords() } returns Result.success(emptyData)
        return RecordsViewModel(recordsRepository)
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() {
        coEvery { recordsRepository.loadRecords() } returns Result.success(emptyData)
        val vm = RecordsViewModel(recordsRepository)
        // Before coroutines run the state is Loading
        assertTrue(vm.uiState.value is RecordsUiState.Loading)
    }

    @Test
    fun `init triggers loadRecords`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        coVerify(exactly = 1) { recordsRepository.loadRecords() }
        assertTrue(vm.uiState.value is RecordsUiState.Success)
    }

    // ── Success state ─────────────────────────────────────────────────────────

    @Test
    fun `loadRecords maps wins draws losses and prideBalance correctly`() = runTest {
        val completedBets = listOf(
            CompletedBet("b1", "Bet 1", "Alice", 10, BetOutcome.WIN,  "2024-01-01"),
            CompletedBet("b2", "Bet 2", "Bob",   5,  BetOutcome.DRAW, "2024-01-02"),
            CompletedBet("b3", "Bet 3", "Carol", 20, BetOutcome.LOSS, "2024-01-03")
        )
        coEvery { recordsRepository.loadRecords() } returns Result.success(
            RecordsData(wins = 1, draws = 1, losses = 1, prideBalance = -10, completedBets = completedBets)
        )

        val vm = RecordsViewModel(recordsRepository)
        advanceUntilIdle()

        val state = vm.uiState.value as RecordsUiState.Success
        assertEquals(1,  state.wins)
        assertEquals(1,  state.draws)
        assertEquals(1,  state.losses)
        assertEquals(-10, state.prideBalance)
        assertEquals(completedBets, state.completedBets)
    }

    @Test
    fun `loadRecords with empty history produces zero stats`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value as RecordsUiState.Success
        assertEquals(0, state.wins)
        assertEquals(0, state.draws)
        assertEquals(0, state.losses)
        assertEquals(0, state.prideBalance)
        assertTrue(state.completedBets.isEmpty())
    }

    // ── Error state ───────────────────────────────────────────────────────────

    @Test
    fun `loadRecords transitions to Error on failure`() = runTest {
        coEvery { recordsRepository.loadRecords() } returns
            Result.failure(Exception("Network error"))

        val vm = RecordsViewModel(recordsRepository)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is RecordsUiState.Error)
        assertEquals("Network error", (state as RecordsUiState.Error).message)
    }

    @Test
    fun `loadRecords uses fallback message when exception has no message`() = runTest {
        coEvery { recordsRepository.loadRecords() } returns
            Result.failure(Exception())

        val vm = RecordsViewModel(recordsRepository)
        advanceUntilIdle()

        val state = vm.uiState.value as RecordsUiState.Error
        assertTrue(state.message.isNotBlank())
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Test
    fun `refresh resets to Loading then reloads`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        // Confirm we're in success state first
        assertTrue(vm.uiState.value is RecordsUiState.Success)

        // Refresh
        vm.refresh()
        // State should immediately return to Loading
        assertTrue(vm.uiState.value is RecordsUiState.Loading)

        advanceUntilIdle()
        // Then back to Success
        assertTrue(vm.uiState.value is RecordsUiState.Success)
        // Two total calls: one from init, one from refresh
        coVerify(exactly = 2) { recordsRepository.loadRecords() }
    }

    @Test
    fun `refresh after error can recover to Success`() = runTest {
        coEvery { recordsRepository.loadRecords() } returns
            Result.failure(Exception("Offline"))

        val vm = RecordsViewModel(recordsRepository)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is RecordsUiState.Error)

        // Now succeed on retry
        coEvery { recordsRepository.loadRecords() } returns Result.success(emptyData)
        vm.refresh()
        advanceUntilIdle()

        assertTrue(vm.uiState.value is RecordsUiState.Success)
    }
}
