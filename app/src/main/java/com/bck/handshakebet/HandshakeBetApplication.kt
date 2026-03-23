package com.bck.handshakebet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for HandshakeBet.
 *
 * Annotated with [HiltAndroidApp] to trigger Hilt's code generation and serve
 * as the application-level dependency injection container. All Hilt components
 * are scoped to this application's lifecycle.
 */
@HiltAndroidApp
class HandshakeBetApplication : Application()
