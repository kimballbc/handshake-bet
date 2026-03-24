package com.bck.handshakebet.feature.home.data.di

import com.bck.handshakebet.feature.home.data.repository.BetRepositoryImpl
import com.bck.handshakebet.feature.home.domain.repository.BetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that wires [BetRepository] to its production implementation.
 *
 * Installing in [SingletonComponent] keeps a single [BetRepositoryImpl] alive
 * for the duration of the app process, consistent with [NetworkModule] and [AuthModule].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BetModule {

    /**
     * Binds [BetRepositoryImpl] as the concrete implementation of [BetRepository].
     */
    @Binds
    @Singleton
    abstract fun bindBetRepository(impl: BetRepositoryImpl): BetRepository
}
