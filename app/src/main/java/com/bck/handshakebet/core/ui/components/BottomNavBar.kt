package com.bck.handshakebet.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.core.navigation.Screen
import com.bck.handshakebet.ui.theme.HandshakeBetTheme

/**
 * App-wide bottom navigation bar with a centred FAB for creating new bets.
 *
 * Layout: [Home] [Feed]  [+ FAB]  [Records] [Profile]
 *
 * Reads left-to-right as: mine → discover → create → history → me.
 * Home surfaces pending actions (bets awaiting response, notifications).
 * The FAB is overlaid at the top-centre of the [NavigationBar] and navigates
 * to [Screen.NewBet]. The four flanking items handle main-screen navigation.
 *
 * This composable is stateless — all callbacks are provided by [AppNavGraph].
 *
 * @param selectedDestination   The [Screen] currently displayed in the NavHost.
 * @param onDestinationSelected Invoked when a nav item is tapped.
 * @param onNewBetClick         Invoked when the centre FAB is tapped.
 * @param modifier              Optional modifier for the outer [Box] container.
 */
@Composable
fun BottomNavBar(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    onNewBetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar {
            // ── Left: personal → discovery ────────────────────────────────
            listOf(NavItem.MY_BETS, NavItem.FEED).forEach { item ->
                NavigationBarItem(
                    icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                    label = { Text(text = item.label) },
                    selected = selectedDestination == item.screen,
                    onClick = { onDestinationSelected(item.screen) }
                )
            }

            // ── Centre gap — reserves space under the FAB ─────────────────
            Spacer(modifier = Modifier.weight(1f))

            // ── Right: history → account ──────────────────────────────────
            listOf(NavItem.RECORDS, NavItem.PROFILE).forEach { item ->
                NavigationBarItem(
                    icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                    label = { Text(text = item.label) },
                    selected = selectedDestination == item.screen,
                    onClick = { onDestinationSelected(item.screen) }
                )
            }
        }

        // ── Centre FAB — sits above the navigation bar ────────────────────
        FloatingActionButton(
            onClick = onNewBetClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(56.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Bet"
            )
        }
    }
}

/**
 * The four persistent nav destinations flanking the centre FAB.
 *
 * Order deliberately follows the user journey: Home → Feed → [+] → Records → Profile.
 * [Screen.NewBet] is handled by the FAB and is intentionally absent here.
 */
private enum class NavItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    MY_BETS(Screen.Account,  "Home",    Icons.Default.Home),
    FEED(Screen.Home,        "Feed",    Icons.Default.DynamicFeed),
    RECORDS(Screen.Records,  "Records", Icons.Default.Leaderboard),
    PROFILE(Screen.Profile,  "Profile", Icons.Default.AccountCircle)
}

@Preview(showBackground = true)
@Composable
private fun BottomNavBarPreview() {
    HandshakeBetTheme {
        BottomNavBar(
            selectedDestination = Screen.Home,
            onDestinationSelected = {},
            onNewBetClick = {}
        )
    }
}
