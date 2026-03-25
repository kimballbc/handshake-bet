package com.bck.handshakebet.feature.friends.data.di

import com.bck.handshakebet.feature.friends.data.repository.FriendshipRepositoryImpl
import com.bck.handshakebet.feature.friends.domain.repository.FriendshipRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that wires [FriendshipRepository] to its production implementation.
 *
 * Installed in [SingletonComponent] to keep a single [FriendshipRepositoryImpl]
 * alive for the duration of the app process.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FriendshipModule {

    /** Binds [FriendshipRepositoryImpl] as the concrete implementation of [FriendshipRepository]. */
    @Binds
    @Singleton
    abstract fun bindFriendshipRepository(impl: FriendshipRepositoryImpl): FriendshipRepository
}
