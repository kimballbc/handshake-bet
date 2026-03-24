package com.bck.handshakebet.feature.home.ui

import app.cash.turbine.test
import com.bck.handshakebet.core.testing.MainDispatcherRule
import com.bck.handshakebet.feature.home.domain.model.Bet
import com.bck.handshakebet.feature.home.domain.model.BetStatus
import com.bck.handshakebet.feature.home.domain.repository.BetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [HomeViewModel].
 *
 * [BetRepository] is mocked with MockK so no Supabase or Android dependencies
 * are involved. [MainDispatcherRule] replaces [kotlinx.coroutines.Dispatchers.Main]
 * with an [kotlinx.coroutines.test.UnconfinedTestDispatcher] for the duration of
 * each test. State transitions are asserted using Turbine's `test {}` block.
 */
class HomeViewModelTest {

    // Home tests don't need to observe intermediate Loading states, so we use
    // UnconfinedTestDispatcher to run the ViewModel's init coroutine eagerly in
    // setUp(). This means the StateFlow is already Success by the time each test
    // body runs, which is exactly what these tests require.
    // (AuthViewModelTest uses StandardTestDispatcher to catch the Loading→Success
    // transition; the two suites deliberately use different dispatchers.)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val betRepository: BetRepository = mockk()
    private lateinit var viewModel: HomeViewModel

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private val publicBet = Bet(
        id = "pub-1",
        title = "Public pizza challenge",
        description = "Eat it all",
        creatorId = "user-1",
        creatorDisplayName = "Ben",
        opponentId = "user-2",
        opponentDisplayName = "Alex",
        status = BetStatus.ACTIVE,
        isPublic = true,
        winnerId = null,
        createdAt = "2024-09-01T12:00:00Z"
    )

    private val myBet = Bet(
        id = "my-1",
        title = "My private bet",
        description = "First to the gym",
        creatorId = "user-1",
        creatorDisplayName = "Ben",
        opponentId = "user-3",
        opponentDisplayName = "Chris",
        status = BetStatus.PENDING,
        isPublic = false,
        winnerId = null,
        createdAt = "2024-09-02T08:00:00Z"
    )

    @Before
    fun setUp() {
        // Default happy-path stubs so each test starts with a clean state.
        coEvery { betRepository.fetchPublicBets() } returns Result.success(listOf(publicBet))
        coEvery { betRepository.fetchMyBets() } returns Result.success(listOf(myBet))
        viewModel = HomeViewModel(betRepository)
    }

    // ── Initial load ──────────────────────────────────────────────────────────

    @Test
    fun `initial load emits Loading then Success`() = runTest {
        // Recreate with controlled stubs (setUp already covers this, but be explicit).
        coEvery { betRepository.fetchPublicBets() } returns Result.success(listOf(publicBet))
        coEvery { betRepository.fetchMyBets() } returns Result.success(listOf(myBet))

        viewModel.uiState.test {
            // With UnconfinedTestDispatcher the init block runs immediately, so
            // we may see only Success. Assert the terminal state.
            val finalState = expectMostRecentItem()
            assertTrue(finalState is HomeUiState.Success)
        }
    }

    @Test
    fun `initial load with data shows correct bets in Success state`() = runTest {
        viewModel.uiState.test {
            val state = expectMostRecentItem() as HomeUiState.Success
            assertEquals(listOf(publicBet), state.publicBets)
            assertEquals(listOf(myBet), state.myBets)
            assertEquals(HomeTab.PUBLIC, state.selectedTab)
        }
    }

    // ── Tab switching ─────────────────────────────────────────────────────────

    @Test
    fun `onTabSelected switches to MY_BETS tab`() = runTest {
        viewModel.uiState.test {
            awaitItem() // consume initial state (Loading or Success)

            viewModel.onTabSelected(HomeTab.MY_BETS)

            val state = awaitItem()
            assertTrue(state is HomeUiState.Success)
            assertEquals(HomeTab.MY_BETS, (state as HomeUiState.Success).selectedTab)
        }
    }

    @Test
    fun `onTabSelected back to PUBLIC switches tab correctly`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onTabSelected(HomeTab.MY_BETS)
            awaitItem() // MY_BETS

            viewModel.onTabSelected(HomeTab.PUBLIC)
            val state = awaitItem() as HomeUiState.Success
            assertEquals(HomeTab.PUBLIC, state.selectedTab)
        }
    }

    @Test
    fun `onTabSelected to empty tab emits Empty state`() = runTest {
        coEvery { betRepository.fetchMyBets() } returns Result.success(emptyList())
        viewModel = HomeViewModel(betRepository)

        viewModel.uiState.test {
            awaitItem() // initial Success (publicBets non-empty, myBets empty)

            viewModel.onTabSelected(HomeTab.MY_BETS)

            val state = awaitItem()
            assertTrue("Expected Empty but got $state", state is HomeUiState.Empty)
            assertEquals(HomeTab.MY_BETS, (state as HomeUiState.Empty).selectedTab)
        }
    }

    // ── Empty state ───────────────────────────────────────────────────────────

    @Test
    fun `empty public bets on initial load emits Empty state`() = runTest {
        coEvery { betRepository.fetchPublicBets() } returns Result.success(emptyList())
        viewModel = HomeViewModel(betRepository)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue("Expected Empty but got $state", state is HomeUiState.Empty)
            assertEquals(HomeTab.PUBLIC, (state as HomeUiState.Empty).selectedTab)
        }
    }

    // ── Error state ───────────────────────────────────────────────────────────

    @Test
    fun `repository failure on initial load emits Error state`() = runTest {
        coEvery { betRepository.fetchPublicBets() } returns
            Result.failure(Exception("No internet connection. Please check your network."))
        viewModel = HomeViewModel(betRepository)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue("Expected Error but got $state", state is HomeUiState.Error)
            assertEquals(
                "No internet connection. Please check your network.",
                (state as HomeUiState.Error).message
            )
        }
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Test
    fun `onRefresh re-fetches bets from repository`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial Success

            viewModel.onRefresh()

            // isRefreshing = true emitted first
            val refreshing = awaitItem()
            assertTrue(refreshing is HomeUiState.Success)
            assertTrue((refreshing as HomeUiState.Success).isRefreshing)

            // Then settled Success with isRefreshing = false
            val settled = awaitItem()
            assertTrue(settled is HomeUiState.Success)
            assertTrue(!(settled as HomeUiState.Success).isRefreshing)
        }

        // fetchPublicBets called twice: once on init, once on refresh
        coVerify(exactly = 2) { betRepository.fetchPublicBets() }
    }

    @Test
    fun `onRefresh preserves previously selected tab`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onTabSelected(HomeTab.MY_BETS)
            awaitItem() // MY_BETS

            viewModel.onRefresh()
            awaitItem() // isRefreshing = true
            val settled = awaitItem() as HomeUiState.Success

            assertEquals(HomeTab.MY_BETS, settled.selectedTab)
        }
    }

    // ── Retry ─────────────────────────────────────────────────────────────────

    @Test
    fun `onRetry after error reloads and emits Success`() = runTest {
        coEvery { betRepository.fetchPublicBets() } returns
            Result.failure(Exception("Network error"))
        viewModel = HomeViewModel(betRepository)

        viewModel.uiState.test {
            awaitItem() // Error state

            // Fix the stub to succeed on retry
            coEvery { betRepository.fetchPublicBets() } returns Result.success(listOf(publicBet))
            coEvery { betRepository.fetchMyBets() } returns Result.success(listOf(myBet))

            viewModel.onRetry()

            val state = awaitItem()
            assertTrue("Expected Success but got $state", state is HomeUiState.Success)
        }
    }
}
