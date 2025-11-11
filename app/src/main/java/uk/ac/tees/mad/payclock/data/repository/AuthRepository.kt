package uk.ac.tees.mad.payclock.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling authentication with Firebase.
 */
class AuthRepository {

    private val firebaseAuth: FirebaseAuth = Firebase.auth

    /**
     * Signs in a user with email and password.
     */
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            Result.success(user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a new user with email and password.
     */
    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            Result.success(user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a password reset email.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs out the current user.
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Gets the current user's ID, or null if not logged in.
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}
