package com.bck.handshakebet.core.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bck.handshakebet.feature.auth.ui.LoginScreen

/**
 * Top-level navigation graph for the HandshakeBet app.
 *
 * Wires all [Screen] destinations to their composable implementations using the
 * Navigation Compose 2.8+ type-safe API. As each feature is built out in
 * subsequent phases, the placeholder [Text] composables below are replaced with
 * real screen composables.
 *
 * The [NavHostController] is created and owned here, keeping [MainActivity]
 * free of all navigation concerns. If a screen needs to trigger navigation it
 * receives a lambda callback — it never holds a reference to the controller
 * directly.
 *
 * @param navController The [NavHostController] to use. Defaults to a new
 *   controller created via [rememberNavController], but can be overridden in
 *   tests or previews.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login
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
            // TODO(Phase 2): Replace with HomeScreen
            Text(text = "Home — Phase 2")
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
