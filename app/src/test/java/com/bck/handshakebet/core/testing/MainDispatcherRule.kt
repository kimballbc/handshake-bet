package com.bck.handshakebet.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit [TestWatcher] that replaces the main coroutine dispatcher with a
 * [TestDispatcher] for the duration of each test, then restores it afterwards.
 *
 * Apply this rule to any test class that contains a ViewModel (or any class
 * that launches coroutines on [Dispatchers.Main]).
 *
 * Usage:
 * ```kotlin
 * class MyViewModelTest {
 *
 *     @get:Rule
 *     val mainDispatcherRule = MainDispatcherRule()
 *
 *     @Test
 *     fun `loading state is emitted before data`() = runTest {
 *         val viewModel = MyViewModel(...)
 *         viewModel.uiState.test {
 *             assertThat(awaitItem()).isInstanceOf(UiState.Loading::class.java)
 *             assertThat(awaitItem()).isInstanceOf(UiState.Success::class.java)
 *         }
 *     }
 * }
 * ```
 *
 * @param testDispatcher The [TestDispatcher] to install as the main dispatcher.
 *   Defaults to [UnconfinedTestDispatcher], which executes coroutines eagerly
 *   without suspension — ideal for most ViewModel unit tests. Switch to
 *   [kotlinx.coroutines.test.StandardTestDispatcher] when you need precise
 *   control over coroutine execution order.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
