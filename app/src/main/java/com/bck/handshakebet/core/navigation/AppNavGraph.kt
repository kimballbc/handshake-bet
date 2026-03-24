package com.bck.handshakebet.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bck.handshakebet.core.ui.components.BottomNavBar
import com.bck.handshakebet.feature.auth.ui.LoginScreen
import com.bck.handshakebet.feature.home.ui.HomeScreen

/**
 * Top-level navigation graph for the HandshakeBet app.
 *
 * Wires all [Screen] destinations to their composable implementations using the
 * Navigation Compose 2.8+ type-safe API. The [BottomNavBar] is shown for all
 * main destinations and hidden on the [Screen.Login] screen.
 *
 * The [NavHostController] is created and owned here, keeping [MainActivity]
 * free of all navigation concerns. Screens receive lambda callbacks for
 * navigation — they never hold a direct reference to the controller.
 *
 * @param navController The [NavHostController] to use. Defaults to a new
 *   controller created via [rememberNavController], but can be overridden in
 *   tests or previews.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // BottomNavBar is only visible on main feature screens (not Login).
    val showBottomBar = currentDestination?.let { dest ->
        listOf(Screen.Home, Screen.Account, Screen.Records, Screen.Stats, Screen.Profile)
            .any { dest.hasRoute(it::class) }
    } ?: false

    // Resolve the active Screen for BottomNavBar selection highlight.
    val selectedScreen: Screen = when {
        currentDestination?.hasRoute(Screen.Home::class) == true    -> Screen.Home
        currentDestination?.hasRoute(Screen.Account::class) == true -> Screen.Account
        currentDestination?.hasRoute(Screen.Records::class) == true -> Screen.Records
        currentDestination?.hasRoute(Screen.Stats::class) == true   -> Screen.Stats
        currentDestination?.hasRoute(Screen.Profile::class) == true -> Screen.Profile
        else -> Screen.Home
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    selectedDestination = selectedScreen,
                    onDestinationSelected = { screen ->
                        navController.navigate(screen) {
                            // Pop up to Home so back-stack doesn't grow unboundedly.
                            popUpTo(Screen.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNewBetClick = {
                        navController.navigate(Screen.NewBet) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // innerPadding carries the height of the BottomNavBar so content is
        // never obscured behind it. Without this the last list item clips.
        NavHost(
            navController = navController,
            startDestination = Screen.Login,
            modifier = Modifier.padding(innerPadding)
        ) {

            // ── Phase 1: Auth ─────────────────────────────────────────────────────
            composable<Screen.Login> {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Login) { inclusive = true }
                        }
                    }
                )
            }

            // ── Phase 2: Home ─────────────────────────────────────────────────────
            composable<Screen.Home> {
                HomeScreen(
                    onNavigateToBetDetail = { betId ->
                        navController.navigate(Screen.BetDetail(betId))
                    }
                )
            }

            // ── Phase 3: Bets ─────────────────────────────────────────────────────
            composable<Screen.NewBet> {
                // TODO(Phase 3): Replace with NewBetScreen
                Text(text = "New Bet — Phase 3")
            }

            composable<Screen.Account> {
                // TODO(Phase 3): Replace with AccountScreen
                Text(text = "Account — Phase 3")
            }

            composable<Screen.BetDetail> { backStackEntry ->
                val destination: Screen.BetDetail = backStackEntry.toRoute()
                // TODO(Phase 3): Replace with BetDetailScreen(betId = destination.betId)
                Text(text = "Bet Detail: ${destination.betId} — Phase 3")
            }

            // ── Phase 4: Friends ──────────────────────────────────────────────────
            // Friends surface is embedded within Home and Account screens;
            // no top-level destination needed.

            // ── Phase 5: Records + Stats ──────────────────────────────────────────
            composable<Screen.Records> {
                // TODO(Phase 5): Replace with RecordsScreen
                Text(text = "Records — Phase 5")
            }

            composable<Screen.Stats> {
                // TODO(Phase 5): Replace with StatsScreen
                Text(text = "Stats — Phase 5")
            }

            // ── Phase 6: Profile ──────────────────────────────────────────────────
            composable<Screen.Profile> {
                // TODO(Phase 6): Replace with ProfileScreen
                Text(text = "Profile — Phase 6")
            }
        }
    }
}
