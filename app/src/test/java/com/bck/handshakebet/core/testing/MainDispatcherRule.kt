package com.bck.handshakebet.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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
 *   Defaults to [StandardTestDispatcher], which queues coroutines rather than
 *   running them eagerly. This lets Turbine observe intermediate states (e.g.
 *   Loading) before the final state is set. Pass the same dispatcher to
 *   [kotlinx.coroutines.test.runTest] so all coroutines share one scheduler:
 *   `runTest(mainDispatcherRule.testDispatcher) { … }`
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
