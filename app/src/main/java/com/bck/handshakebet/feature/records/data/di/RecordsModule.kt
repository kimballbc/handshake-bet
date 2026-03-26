package com.bck.handshakebet.feature.records.data.di

import com.bck.handshakebet.feature.records.data.repository.RecordsRepositoryImpl
import com.bck.handshakebet.feature.records.domain.repository.RecordsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecordsModule {

    @Binds
    @Singleton
    abstract fun bindRecordsRepository(
        impl: RecordsRepositoryImpl
    ): RecordsRepository
}
