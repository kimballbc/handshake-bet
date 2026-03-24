package com.bck.handshakebet.core.network.di

import com.bck.handshakebet.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Singleton

/**
 * Hilt module providing the Supabase client and its plugin singletons.
 *
 * Installed in [SingletonComponent] so that a single [SupabaseClient] is shared
 * across the entire application. Individual feature repositories receive only the
 * plugin they need (e.g. [Auth], [Postgrest]) via constructor injection, keeping
 * them decoupled from the full client.
 *
 * Credentials are read from [BuildConfig], which sources them from
 * `local.properties` at build time — no secrets are committed to version control.
 * Debug builds use `SUPABASE_URL_DEV` / `SUPABASE_KEY_DEV` and release builds
 * use `SUPABASE_URL_PROD` / `SUPABASE_KEY_PROD`.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides the singleton [SupabaseClient] with all required plugins installed.
     * This is the single source of truth for the Supabase connection.
     */
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    /**
     * Provides the [Auth] plugin from the shared [SupabaseClient].
     * Injected into auth-related repositories (e.g. AuthRepositoryImpl).
     */
    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient): Auth = client.auth

    /**
     * Provides the [Postgrest] plugin from the shared [SupabaseClient].
     * Injected into repositories that query or mutate database tables.
     */
    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest = client.postgrest

    /**
     * Provides the [Storage] plugin from the shared [SupabaseClient].
     * Injected into repositories that handle avatar or file storage.
     */
    @Provides
    @Singleton
    fun provideStorage(client: SupabaseClient): Storage = client.storage
}
