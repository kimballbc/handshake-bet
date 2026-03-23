package com.bck.handshakebet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bck.handshakebet.core.navigation.AppNavGraph
import com.bck.handshakebet.ui.theme.HandshakeBetTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The single Activity for the HandshakeBet app.
 *
 * Responsibilities are intentionally minimal: register as a Hilt entry point,
 * apply the app theme, and delegate all rendering to [AppNavGraph]. Navigation
 * logic and business logic live entirely within feature ViewModels and the nav
 * graph — this class should never grow beyond this boilerplate.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HandshakeBetTheme {
                AppNavGraph()
            }
        }
    }
}
