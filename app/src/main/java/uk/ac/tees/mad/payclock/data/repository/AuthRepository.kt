package uk.ac.tees.mad.payclock.data.repository

import android.content.Context
import kotlinx.coroutines.delay

/**
 * Repository for handling authentication and persisting the user's login state.
 */
class AuthRepository(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)

    private companion object {
        const val AUTH_TOKEN_KEY = "auth_token"
    }

    /**
     * Simulates a login request to a backend.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @return A [Result] indicating success with a token or failure with an exception.
     */
    suspend fun login(email: String, password: String): Result<String> {
        delay(1500) // Simulate network latency
        // In a real app, you would make a network request here.
        return if (email.isNotBlank() && password == "password") { // Use "password" to test success
            val fakeToken = "fake-jwt-token-for-$email"
            saveAuthToken(fakeToken)
            Result.success(fakeToken)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    /**
     * Saves the authentication token to SharedPreferences.
     */
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(AUTH_TOKEN_KEY, token).apply()
    }

    /**
     * Retrieves the authentication token from SharedPreferences.
     */
    fun getAuthToken(): String? {
        return sharedPreferences.getString(AUTH_TOKEN_KEY, null)
    }

    /**
     * Clears the authentication token, effectively logging the user out.
     */
    fun clearAuthToken() {
        sharedPreferences.edit().remove(AUTH_TOKEN_KEY).apply()
    }
}
