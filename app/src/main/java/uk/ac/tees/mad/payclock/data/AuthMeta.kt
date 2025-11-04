package uk.ac.tees.mad.payclock.data

import com.google.gson.annotations.SerializedName


/**
 * Represents the metadata block from allauth.headless responses.
 * This contains the critical session token.
 */
data class AuthMeta(
    @SerializedName("session_token")
    val sessionToken: String?,
    @SerializedName("is_authenticated")
    val isAuthenticated: Boolean
)

/**
 * Generic response wrapper for allauth.headless endpoints.
 */
data class AuthResponse<T>(
    val status: Int,
    val data: T?, // The user data, etc. (we might not need this explicitly here)
    val meta: AuthMeta
)

// Data class for login credentials
data class Credentials(
    val email: String,
    val password: String
)

// Data class for successful job list retrieval (if your API wraps it)
// If the API just returns List<Job>, you don't need a wrapper.