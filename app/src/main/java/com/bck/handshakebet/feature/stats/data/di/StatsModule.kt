package com.bck.handshakebet.feature.stats.data.di

import com.bck.handshakebet.feature.stats.data.repository.StatsRepositoryImpl
import com.bck.handshakebet.feature.stats.domain.repository.StatsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StatsModule {

    @Binds
    @Singleton
    abstract fun bindStatsRepository(
        impl: StatsRepositoryImpl
    ): StatsRepository
}
