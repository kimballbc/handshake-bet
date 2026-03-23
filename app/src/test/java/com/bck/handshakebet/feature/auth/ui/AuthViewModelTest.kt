package com.bck.handshakebet.feature.auth.ui

import app.cash.turbine.test
import com.bck.handshakebet.core.domain.model.User
import com.bck.handshakebet.core.testing.MainDispatcherRule
import com.bck.handshakebet.feature.auth.domain.model.SignUpOutcome
import com.bck.handshakebet.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [AuthViewModel].
 *
 * The [AuthRepository] is mocked with MockK so no Supabase or Android
 * dependencies are involved. [MainDispatcherRule] replaces [kotlinx.coroutines.Dispatchers.Main]
 * with an [kotlinx.coroutines.test.UnconfinedTestDispatcher] for the duration
 * of each test. State transitions are asserted using Turbine's `test {}` block.
 */
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private lateinit var viewModel: AuthViewModel

    private val testUser = User(
        id = "user-123",
        email = "test@example.com",
        displayName = "Test User"
    )

    @Before
    fun setUp() {
        viewModel = AuthViewModel(authRepository)
    }

    // ── onLoginClicked ────────────────────────────────────────────────────────

    @Test
    fun `login with valid credentials emits Loading then Success`() = runTest {
        coEvery { authRepository.signIn(any(), any()) } returns Result.success(testUser)

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.onLoginClicked("test@example.com", "password123")

            assertEquals(AuthUiState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is AuthUiState.Success)
            assertEquals(testUser, (success as AuthUiState.Success).user)
        }
    }

    @Test
    fun `login with invalid credentials emits Loading then Error`() = runTest {
        coEvery { authRepository.signIn(any(), any()) } returns
            Result.failure(Exception("Incorrect email or password"))

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.onLoginClicked("test@example.com", "wrongpassword")

            assertEquals(AuthUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is AuthUiState.Error)
            assertEquals("Incorrect email or password", (error as AuthUiState.Error).message)
        }
    }

    @Test
    fun `login with blank email emits Error without Loading`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.onLoginClicked("", "password123")

            val error = awaitItem()
            assertTrue(error is AuthUiState.Error)
            assertEquals("Email cannot be empty", (error as AuthUiState.Error).message)
            expectNoEvents()
        }
    }

    @Test
    fun `login with blank password emits Error without Loading`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.onLoginClicked("test@example.com", "")

            val error = awaitItem()
            assertTrue(error is AuthUiState.Error)
            assertEquals("Password cannot be empty", (error as AuthUiState.Error).message)
            expectNoEvents()
        }
    }

    // ── onSignUpClicked ───────────────────────────────────────────────────────

    @Test
    fun `signup with valid inputs emits Loading then Success`() = runTest {
        coEvery { authRepository.signUp(any(), any(), any()) } returns
            Result.success(SignUpOutcome.Success(testUser))

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.onSignUpClicked("test@example.com", "password123", "Test User")

            assertEquals(AuthUiState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is AuthUiState.Success)
            assertEquals(testUser, (success as AuthUiState.Success).user)
        }
    }

    @Test
    fun `signup when email verification required emits Loading then EmailVerificationSent`() = runTest {
        coEvery { authRepository.signUp(any(), any(), any()) } returns
            Result.success(SignUpOutcome.EmailVerificationRequired)

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.onSignUpClicked("test@example.com", "password123", "Test User")

            assertEquals(AuthUiState.Loading, awaitItem())
            assertEquals(AuthUiState.EmailVerificationSent, awaitItem())
        }
    }

    @Test
    fun `signup failure emits Loading then Error`() = runTest {
        coEvery { authRepository.signUp(any(), any(), any()) } returns
            Result.failure(Exception("This email is already registered"))

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.onSignUpClicked("test@example.com", "password123", "Test User")

            assertEquals(AuthUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is AuthUiState.Error)
            assertEquals("This email is already registered", (error as AuthUiState.Error).message)
        }
    }

    @Test
    fun `signup with blank display name emits Error without Loading`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.onSignUpClicked("test@example.com", "password123", "")

            val error = awaitItem()
            assertTrue(error is AuthUiState.Error)
            assertEquals("Display name cannot be empty", (error as AuthUiState.Error).message)
            expectNoEvents()
        }
    }

    @Test
    fun `signup with password too short emits Error without Loading`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.onSignUpClicked("test@example.com", "abc", "Test User")

            val error = awaitItem()
            assertTrue(error is AuthUiState.Error)
            assertEquals("Password must be at least 6 characters", (error as AuthUiState.Error).message)
            expectNoEvents()
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun `validateEmail returns null for valid email`() {
        assertEquals(null, viewModel.validateEmail("user@example.com"))
    }

    @Test
    fun `validateEmail returns error for malformed email`() {
        assertTrue(viewModel.validateEmail("not-an-email") != null)
        assertTrue(viewModel.validateEmail("missing@tld") != null)
        assertTrue(viewModel.validateEmail("@nodomain.com") != null)
    }

    @Test
    fun `validateEmail returns error for blank email`() {
        assertEquals("Email cannot be empty", viewModel.validateEmail(""))
        assertEquals("Email cannot be empty", viewModel.validateEmail("   "))
    }

    @Test
    fun `validateDisplayName returns null for valid name`() {
        assertEquals(null, viewModel.validateDisplayName("Ben"))
    }

    @Test
    fun `validateDisplayName returns error for single character name`() {
        assertTrue(viewModel.validateDisplayName("B") != null)
    }

    @Test
    fun `validateDisplayName returns error for blank name`() {
        assertEquals("Display name cannot be empty", viewModel.validateDisplayName(""))
    }

    // ── onStateConsumed ───────────────────────────────────────────────────────

    @Test
    fun `onStateConsumed resets state to Idle`() = runTest {
        coEvery { authRepository.signIn(any(), any()) } returns
            Result.failure(Exception("error"))

        viewModel.uiState.test {
            awaitItem() // Idle

            viewModel.onLoginClicked("test@example.com", "password")
            awaitItem() // Loading
            awaitItem() // Error

            viewModel.onStateConsumed()
            assertEquals(AuthUiState.Idle, awaitItem())
        }
    }
}
