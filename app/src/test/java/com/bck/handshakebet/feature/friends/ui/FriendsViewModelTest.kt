package com.bck.handshakebet.feature.friends.ui

import app.cash.turbine.test
import com.bck.handshakebet.core.testing.MainDispatcherRule
import com.bck.handshakebet.feature.friends.domain.model.Friend
import com.bck.handshakebet.feature.friends.domain.model.FriendRequest
import com.bck.handshakebet.feature.friends.domain.model.FriendshipsData
import com.bck.handshakebet.feature.friends.domain.repository.FriendshipRepository
import com.bck.handshakebet.feature.home.domain.model.UserSummary
import com.bck.handshakebet.feature.home.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FriendsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val friendshipRepository: FriendshipRepository = mockk()
    private val userRepository: UserRepository = mockk()

    private val emptyData = FriendshipsData(
        friends          = emptyList(),
        incomingRequests = emptyList(),
        sentRequests     = emptyList()
    )

    private fun viewModel(): FriendsViewModel {
        coEvery { friendshipRepository.loadFriendships() } returns Result.success(emptyData)
        return FriendsViewModel(friendshipRepository, userRepository)
    }

    // ── Initial load ──────────────────────────────────────────────────────────

    @Test
    fun `init triggers loadFriendships`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        coVerify(exactly = 1) { friendshipRepository.loadFriendships() }
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadFriendships populates friends and requests on success`() = runTest {
        val friends = listOf(Friend("f1", "u1", "Alice"))
        val incoming = listOf(
            FriendRequest("f2", "u2", "Bob", isIncoming = true)
        )
        val sent = listOf(
            FriendRequest("f3", "u3", "Carol", isIncoming = false)
        )
        coEvery { friendshipRepository.loadFriendships() } returns Result.success(
            FriendshipsData(friends, incoming, sent)
        )

        val vm = FriendsViewModel(friendshipRepository, userRepository)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(friends, state.friends)
        assertEquals(incoming, state.incomingRequests)
        assertEquals(sent, state.sentRequests)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadFriendships sets errorMessage on failure`() = runTest {
        coEvery { friendshipRepository.loadFriendships() } returns
            Result.failure(Exception("Network error"))

        val vm = FriendsViewModel(friendshipRepository, userRepository)
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        assertEquals("Network error", vm.uiState.value.errorMessage)
    }

    // ── Add Friend dialog ──────────────────────────────────────────────────────

    @Test
    fun `onAddFriendClick sets showAddFriendDialog to true`() = runTest {
        val vm = viewModel()
        assertFalse(vm.uiState.value.showAddFriendDialog)
        vm.onAddFriendClick()
        assertTrue(vm.uiState.value.showAddFriendDialog)
    }

    @Test
    fun `onDismissAddFriendDialog closes dialog and clears search`() = runTest {
        val vm = viewModel()
        vm.onAddFriendClick()
        vm.onSearchQueryChanged("Al")
        vm.onDismissAddFriendDialog()

        val state = vm.uiState.value
        assertFalse(state.showAddFriendDialog)
        assertEquals("", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
    }

    // ── sendFriendRequest ──────────────────────────────────────────────────────

    @Test
    fun `onSendFriendRequest closes dialog and reloads on success`() = runTest {
        coEvery { friendshipRepository.sendFriendRequest("u99") } returns Result.success(Unit)
        val vm = viewModel()
        vm.onAddFriendClick()

        vm.uiState.test {
            awaitItem() // current state
            vm.onSendFriendRequest("u99")
            // isPerformingAction = true
            val performing = awaitItem()
            assertTrue(performing.isPerformingAction)
            // dialog dismissed + action cleared
            val closed = awaitItem()
            assertFalse(closed.showAddFriendDialog)
            assertFalse(closed.isPerformingAction)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSendFriendRequest sets errorMessage on failure`() = runTest {
        coEvery { friendshipRepository.sendFriendRequest(any()) } returns
            Result.failure(Exception("Duplicate request"))
        val vm = viewModel()
        vm.onAddFriendClick()
        vm.onSendFriendRequest("u99")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNotNull(state.errorMessage)
        assertFalse(state.isPerformingAction)
    }

    @Test
    fun `onSendFriendRequest is guarded against concurrent calls`() = runTest {
        coEvery { friendshipRepository.sendFriendRequest(any()) } returns Result.success(Unit)
        val vm = viewModel()

        // Trigger without advancing so the first call stays in_progress
        vm.onSendFriendRequest("u1")
        vm.onSendFriendRequest("u2") // should be ignored
        advanceUntilIdle()

        // Only one call should have been made
        coVerify(exactly = 1) { friendshipRepository.sendFriendRequest(any()) }
    }

    // ── acceptFriendRequest ────────────────────────────────────────────────────

    @Test
    fun `onAcceptRequest sets isPerformingAction then reloads on success`() = runTest {
        coEvery { friendshipRepository.acceptFriendRequest("f1") } returns Result.success(Unit)
        val vm = viewModel()

        vm.uiState.test {
            awaitItem() // current
            vm.onAcceptRequest("f1")
            val performing = awaitItem()
            assertTrue(performing.isPerformingAction)
            // Action cleared (reload may produce more items)
            val next = awaitItem()
            val cleared: FriendsUiState = if (next.isPerformingAction) awaitItem() else next
            assertFalse(cleared.isPerformingAction)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onAcceptRequest sets errorMessage on failure`() = runTest {
        coEvery { friendshipRepository.acceptFriendRequest(any()) } returns
            Result.failure(Exception("Server error"))
        val vm = viewModel()
        vm.onAcceptRequest("f1")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isPerformingAction)
    }

    // ── rejectFriendRequest ────────────────────────────────────────────────────

    @Test
    fun `onRejectRequest sets isPerformingAction then reloads on success`() = runTest {
        coEvery { friendshipRepository.rejectFriendRequest("f1") } returns Result.success(Unit)
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onRejectRequest("f1")
            val performing = awaitItem()
            assertTrue(performing.isPerformingAction)
            val next = awaitItem()
            val cleared = if (next.isPerformingAction) awaitItem() else next
            assertFalse(cleared.isPerformingAction)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRejectRequest sets errorMessage on failure`() = runTest {
        coEvery { friendshipRepository.rejectFriendRequest(any()) } returns
            Result.failure(Exception("Oops"))
        val vm = viewModel()
        vm.onRejectRequest("f1")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isPerformingAction)
    }

    // ── removeFriendship ───────────────────────────────────────────────────────

    @Test
    fun `onRemoveFriendship sets isPerformingAction then reloads on success`() = runTest {
        coEvery { friendshipRepository.removeFriendship("f1") } returns Result.success(Unit)
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onRemoveFriendship("f1")
            val performing = awaitItem()
            assertTrue(performing.isPerformingAction)
            val next = awaitItem()
            val cleared = if (next.isPerformingAction) awaitItem() else next
            assertFalse(cleared.isPerformingAction)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRemoveFriendship sets errorMessage on failure`() = runTest {
        coEvery { friendshipRepository.removeFriendship(any()) } returns
            Result.failure(Exception("Could not remove"))
        val vm = viewModel()
        vm.onRemoveFriendship("f1")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isPerformingAction)
    }

    // ── onErrorShown ───────────────────────────────────────────────────────────

    @Test
    fun `onErrorShown clears errorMessage`() = runTest {
        coEvery { friendshipRepository.loadFriendships() } returns
            Result.failure(Exception("Boom"))
        val vm = FriendsViewModel(friendshipRepository, userRepository)
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        vm.onErrorShown()
        assertNull(vm.uiState.value.errorMessage)
    }

    // ── Debounced search (Add Friend dialog) ───────────────────────────────────

    @Test
    fun `search fires after 400ms debounce for queries of 2+ chars`() = runTest {
        val results = listOf(UserSummary("u1", "Alex"))
        coEvery { userRepository.searchUsers("Al") } returns Result.success(results)

        val vm = viewModel()
        runCurrent() // prime the collector before pushing the query

        vm.onSearchQueryChanged("Al")
        advanceTimeBy(401)
        runCurrent() // complete the search coroutine

        assertEquals(results, vm.uiState.value.searchResults)
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
        val results = listOf(UserSummary("u1", "Alex"))
        coEvery { userRepository.searchUsers("Al") } returns Result.success(results)

        val vm = viewModel()
        runCurrent()

        vm.onSearchQueryChanged("Al")
        advanceTimeBy(401)
        runCurrent()
        assertEquals(results, vm.uiState.value.searchResults)

        vm.onSearchQueryChanged("A")
        advanceTimeBy(200)
        runCurrent()
        assertTrue(vm.uiState.value.searchResults.isEmpty())
    }
}
