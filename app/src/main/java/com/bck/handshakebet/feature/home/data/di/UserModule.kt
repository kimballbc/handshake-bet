package com.bck.handshakebet.feature.home.data.di

import com.bck.handshakebet.feature.home.data.repository.UserRepositoryImpl
import com.bck.handshakebet.feature.home.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that wires [UserRepository] to its production implementation.
 *
 * Installed in [SingletonComponent] to keep a single [UserRepositoryImpl]
 * alive for the duration of the app process, consistent with [BetModule].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

    /** Binds [UserRepositoryImpl] as the concrete implementation of [UserRepository]. */
    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
