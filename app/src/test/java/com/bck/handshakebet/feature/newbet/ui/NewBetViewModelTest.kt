package com.bck.handshakebet.feature.newbet.ui

import app.cash.turbine.test
import com.bck.handshakebet.core.testing.MainDispatcherRule
import com.bck.handshakebet.feature.home.domain.model.UserSummary
import com.bck.handshakebet.feature.home.domain.repository.BetRepository
import com.bck.handshakebet.feature.home.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NewBetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val betRepository: BetRepository = mockk()
    private val userRepository: UserRepository = mockk()

    private fun viewModel() = NewBetViewModel(betRepository, userRepository)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has empty form and no errors`() {
        val state = viewModel().uiState.value
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertFalse(state.isPublic)
        assertNull(state.selectedOpponent)
        assertFalse(state.isSubmitting)
        assertFalse(state.isSuccess)
        assertNull(state.errorMessage)
    }

    // ── Form field updates ────────────────────────────────────────────────────

    @Test
    fun `onTitleChanged updates title`() {
        val vm = viewModel()
        vm.onTitleChanged("Coffee bet")
        assertEquals("Coffee bet", vm.uiState.value.title)
    }

    @Test
    fun `onDescriptionChanged updates description`() {
        val vm = viewModel()
        vm.onDescriptionChanged("Loser buys coffee")
        assertEquals("Loser buys coffee", vm.uiState.value.description)
    }

    @Test
    fun `onVisibilityChanged toggles isPublic`() {
        val vm = viewModel()
        assertFalse(vm.uiState.value.isPublic)
        vm.onVisibilityChanged(true)
        assertTrue(vm.uiState.value.isPublic)
    }

    // ── canSubmit validation ──────────────────────────────────────────────────

    @Test
    fun `canSubmit is false when title is blank`() {
        val vm = viewModel()
        vm.onOpponentSelected(UserSummary("id", "Alex"))
        vm.onWagerAmountChanged("10")
        assertFalse(vm.uiState.value.canSubmit)
    }

    @Test
    fun `canSubmit is false when no opponent selected`() {
        val vm = viewModel()
        vm.onTitleChanged("My bet")
        vm.onWagerAmountChanged("10")
        assertFalse(vm.uiState.value.canSubmit)
    }

    @Test
    fun `canSubmit is false when wager is blank`() {
        val vm = viewModel()
        vm.onTitleChanged("My bet")
        vm.onOpponentSelected(UserSummary("id", "Alex"))
        assertFalse(vm.uiState.value.canSubmit)
    }

    @Test
    fun `canSubmit is false when wager is out of range`() {
        val vm = viewModel()
        vm.onTitleChanged("My bet")
        vm.onOpponentSelected(UserSummary("id", "Alex"))
        vm.onWagerAmountChanged("101")
        assertFalse(vm.uiState.value.canSubmit)
    }

    @Test
    fun `canSubmit is true when title, opponent, and valid wager are set`() {
        val vm = viewModel()
        vm.onTitleChanged("My bet")
        vm.onOpponentSelected(UserSummary("id", "Alex"))
        vm.onWagerAmountChanged("10")
        assertTrue(vm.uiState.value.canSubmit)
    }

    // ── Opponent selection ────────────────────────────────────────────────────

    @Test
    fun `onOpponentSelected sets selectedOpponent and clears results`() {
        val vm = viewModel()
        val user = UserSummary("u1", "Alex")
        vm.onOpponentSelected(user)
        assertEquals(user, vm.uiState.value.selectedOpponent)
        assertTrue(vm.uiState.value.searchResults.isEmpty())
    }

    @Test
    fun `onOpponentCleared resets opponent and search query`() {
        val vm = viewModel()
        vm.onOpponentSelected(UserSummary("u1", "Alex"))
        vm.onOpponentCleared()
        assertNull(vm.uiState.value.selectedOpponent)
        assertEquals("", vm.uiState.value.searchQuery)
    }

    @Test
    fun `onSearchQueryChanged clears selectedOpponent`() {
        val vm = viewModel()
        vm.onOpponentSelected(UserSummary("u1", "Alex"))
        vm.onSearchQueryChanged("Ben")
        assertNull(vm.uiState.value.selectedOpponent)
    }

    // ── Debounced search ──────────────────────────────────────────────────────

    @Test
    fun `search fires after 400ms debounce for queries of 2+ chars`() = runTest {
        val users = listOf(UserSummary("u1", "Alex"))
        coEvery { userRepository.searchUsers("Al") } returns Result.success(users)

        val vm = viewModel()
        vm.onSearchQueryChanged("Al")
        advanceTimeBy(401)

        assertEquals(users, vm.uiState.value.searchResults)
    }

    @Test
    fun `search does not fire for queries shorter than 2 chars`() = runTest {
        val vm = viewModel()
        vm.onSearchQueryChanged("A")
        advanceTimeBy(500)

        assertTrue(vm.uiState.value.searchResults.isEmpty())
        assertFalse(vm.uiState.value.isSearching)
    }

    @Test
    fun `search results are cleared when query drops below 2 chars`() = runTest {
        val users = listOf(UserSummary("u1", "Alex"))
        coEvery { userRepository.searchUsers("Al") } returns Result.success(users)

        val vm = viewModel()
        vm.onSearchQueryChanged("Al")
        advanceTimeBy(401)
        assertEquals(users, vm.uiState.value.searchResults)

        vm.onSearchQueryChanged("A")
        advanceTimeBy(200)
        assertTrue(vm.uiState.value.searchResults.isEmpty())
    }

    // ── createBet ─────────────────────────────────────────────────────────────

    @Test
    fun `createBet sets isSubmitting then isSuccess on success`() = runTest {
        coEvery {
            betRepository.createBet(any(), any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        val vm = viewModel()
        vm.onTitleChanged("Coffee bet")
        vm.onOpponentSelected(UserSummary("opp-id", "Alex"))
        vm.onWagerAmountChanged("10")

        vm.uiState.test {
            awaitItem() // current state
            vm.createBet()
            val submitting = awaitItem()
            assertTrue(submitting.isSubmitting)
            val success = awaitItem()
            assertTrue(success.isSuccess)
            assertFalse(success.isSubmitting)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createBet sets errorMessage on failure`() = runTest {
        coEvery {
            betRepository.createBet(any(), any(), any(), any(), any(), any())
        } returns Result.failure(Exception("No internet"))

        val vm = viewModel()
        vm.onTitleChanged("Coffee bet")
        vm.onOpponentSelected(UserSummary("opp-id", "Alex"))
        vm.onWagerAmountChanged("10")
        vm.createBet()

        // Advance until idle so the coroutine completes
        val state = vm.uiState.value
        assertFalse(state.isSuccess)
        assertNotNull(state.errorMessage)
        assertEquals("No internet", state.errorMessage)
    }

    @Test
    fun `createBet increments sliderResetKey on failure`() = runTest {
        coEvery {
            betRepository.createBet(any(), any(), any(), any(), any(), any())
        } returns Result.failure(Exception("Oops"))

        val vm = viewModel()
        vm.onTitleChanged("Coffee bet")
        vm.onOpponentSelected(UserSummary("opp-id", "Alex"))
        vm.onWagerAmountChanged("10")
        val keyBefore = vm.uiState.value.sliderResetKey
        vm.createBet()

        assertEquals(keyBefore + 1, vm.uiState.value.sliderResetKey)
    }

    @Test
    fun `createBet is a no-op when canSubmit is false`() = runTest {
        val vm = viewModel()
        // No title, no opponent, no wager
        vm.createBet()
        assertFalse(vm.uiState.value.isSubmitting)
        assertFalse(vm.uiState.value.isSuccess)
    }

    @Test
    fun `onErrorShown clears errorMessage`() = runTest {
        coEvery {
            betRepository.createBet(any(), any(), any(), any(), any(), any())
        } returns Result.failure(Exception("Oops"))

        val vm = viewModel()
        vm.onTitleChanged("Bet")
        vm.onOpponentSelected(UserSummary("id", "Alex"))
        vm.onWagerAmountChanged("10")
        vm.createBet()

        assertNotNull(vm.uiState.value.errorMessage)
        vm.onErrorShown()
        assertNull(vm.uiState.value.errorMessage)
    }

}
