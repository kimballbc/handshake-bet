package com.bck.handshakebet.feature.stats.ui

import com.bck.handshakebet.core.testing.MainDispatcherRule
import com.bck.handshakebet.feature.stats.domain.model.OpponentStats
import com.bck.handshakebet.feature.stats.domain.model.StatsData
import com.bck.handshakebet.feature.stats.domain.repository.StatsRepository
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

class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val statsRepository: StatsRepository = mockk()

    private val emptyStats = StatsData(
        totalCompleted      = 0,
        winRate             = null,
        currentStreak       = 0,
        bestWinStreak       = 0,
        averagePrideWagered = null,
        topOpponent         = null
    )

    private fun viewModel(): StatsViewModel {
        coEvery { statsRepository.loadStats() } returns Result.success(emptyStats)
        return StatsViewModel(statsRepository)
    }

    @Test
    fun `initial state is Loading`() {
        coEvery { statsRepository.loadStats() } returns Result.success(emptyStats)
        val vm = StatsViewModel(statsRepository)
        assertTrue(vm.uiState.value is StatsUiState.Loading)
    }

    @Test
    fun `init triggers loadStats`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        coVerify(exactly = 1) { statsRepository.loadStats() }
        assertTrue(vm.uiState.value is StatsUiState.Success)
    }

    @Test
    fun `success state maps all fields correctly`() = runTest {
        val rival = OpponentStats("Alex", 5, 3, 2)
        coEvery { statsRepository.loadStats() } returns Result.success(
            StatsData(
                totalCompleted      = 10,
                winRate             = 0.6f,
                currentStreak       = 3,
                bestWinStreak       = 5,
                averagePrideWagered = 12.5f,
                topOpponent         = rival
            )
        )

        val vm = StatsViewModel(statsRepository)
        advanceUntilIdle()

        val state = vm.uiState.value as StatsUiState.Success
        assertEquals(10,    state.totalCompleted)
        assertEquals(0.6f,  state.winRate)
        assertEquals(3,     state.currentStreak)
        assertEquals(5,     state.bestWinStreak)
        assertEquals(12.5f, state.averagePrideWagered)
        assertEquals(rival, state.topOpponent)
    }

    @Test
    fun `empty stats produce Success with null nullable fields`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value as StatsUiState.Success
        assertEquals(0,    state.totalCompleted)
        assertEquals(null, state.winRate)
        assertEquals(null, state.averagePrideWagered)
        assertEquals(null, state.topOpponent)
    }

    @Test
    fun `failure transitions to Error`() = runTest {
        coEvery { statsRepository.loadStats() } returns
            Result.failure(Exception("Stats unavailable"))

        val vm = StatsViewModel(statsRepository)
        advanceUntilIdle()

        val state = vm.uiState.value as StatsUiState.Error
        assertEquals("Stats unavailable", state.message)
    }

    @Test
    fun `refresh resets to Loading then reloads`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is StatsUiState.Success)

        vm.refresh()
        assertTrue(vm.uiState.value is StatsUiState.Loading)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is StatsUiState.Success)
        coVerify(exactly = 2) { statsRepository.loadStats() }
    }

    @Test
    fun `refresh after error can recover`() = runTest {
        coEvery { statsRepository.loadStats() } returns
            Result.failure(Exception("Offline"))

        val vm = StatsViewModel(statsRepository)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is StatsUiState.Error)

        coEvery { statsRepository.loadStats() } returns Result.success(emptyStats)
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is StatsUiState.Success)
    }
}
