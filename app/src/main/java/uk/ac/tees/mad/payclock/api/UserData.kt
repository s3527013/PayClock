package uk.ac.tees.mad.payclock.api


import com.google.gson.annotations.SerializedName

// Data class for login/signup input
data class Credentials(
    val email: String,
    val password: String
)

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
 * The generic type T can be ignored or represent the user/profile data.
 */
data class AuthResponse<T>(
    val status: Int,
    val data: T?,
    val meta: AuthMeta
)

// Example data class for the user data within the AuthResponse
// You might need this if the API returns detailed user info after login
data class UserData(
    val email: String,
    @SerializedName("first_name")
    val firstName: String?
    // ... other user details
)