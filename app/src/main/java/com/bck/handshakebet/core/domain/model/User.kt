package com.bck.handshakebet.core.domain.model

/**
 * Domain model representing an authenticated user.
 *
 * This is a pure Kotlin data class with no Android or Supabase dependencies,
 * making it trivially testable and usable across all feature layers.
 *
 * @property id Unique identifier from Supabase Auth.
 * @property email The user's email address.
 * @property displayName The user's chosen display name. Falls back to [email] if
 *   no display name was set at registration.
 * @property avatarName Optional name of the user's selected avatar asset.
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarName: String? = null
)
