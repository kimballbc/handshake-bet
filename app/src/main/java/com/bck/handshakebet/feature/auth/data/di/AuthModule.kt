package com.bck.handshakebet.feature.auth.data.di

import com.bck.handshakebet.feature.auth.data.repository.AuthRepositoryImpl
import com.bck.handshakebet.feature.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds the auth domain interface to its data-layer implementation.
 *
 * Installed in [SingletonComponent] so the same [AuthRepositoryImpl] instance is
 * reused across the app's lifetime. [AuthRepositoryImpl] itself uses constructor
 * injection so Hilt wires [AuthRemoteSource] → [AuthRepositoryImpl] automatically.
 *
 * ViewModels and other callers depend on [AuthRepository], never on the impl directly.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    /**
     * Binds [AuthRepositoryImpl] as the concrete [AuthRepository] for injection.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
