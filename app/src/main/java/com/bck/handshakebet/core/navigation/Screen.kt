package com.bck.handshakebet.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations for the HandshakeBet app.
 *
 * Each destination is a [Serializable] object or data class, enabling
 * compile-time route verification via the Navigation Compose 2.8+ type-safe API.
 * Destinations requiring arguments are modelled as data classes so that
 * argument types are enforced at compile time rather than parsed from strings.
 *
 * Usage in [AppNavGraph]:
 * ```kotlin
 * composable<Screen.Home> { HomeScreen(...) }
 * composable<Screen.BetDetail> { backStackEntry ->
 *     val dest: Screen.BetDetail = backStackEntry.toRoute()
 *     BetDetailScreen(betId = dest.betId)
 * }
 * ```
 */
sealed interface Screen {

    /** Authentication screen — handles both login and sign-up. */
    @Serializable
    data object Login : Screen

    /** Main home feed showing public bets and friend activity. */
    @Serializable
    data object Home : Screen

    /** Bet creation flow. */
    @Serializable
    data object NewBet : Screen

    /** Account screen — the current user's active and past bets. */
    @Serializable
    data object Account : Screen

    /** Head-to-head win/loss/draw records between friends. */
    @Serializable
    data object Records : Screen

    /** Statistics and accolades screen. */
    @Serializable
    data object Stats : Screen

    /** User profile, avatar selection, and app settings. */
    @Serializable
    data object Profile : Screen

    /**
     * Bet detail screen for viewing or acting on a single bet.
     *
     * @param betId The unique identifier of the bet to display.
     */
    @Serializable
    data class BetDetail(val betId: String) : Screen
}
