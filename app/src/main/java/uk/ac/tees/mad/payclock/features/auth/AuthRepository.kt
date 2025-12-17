package uk.ac.tees.mad.payclock.features.auth

import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A repository for authentication.
 */
@Singleton
class AuthRepository @Inject constructor() {
    private val auth: FirebaseAuth = Firebase.auth

    /**
     * Gets the authentication state of the user.
     *
     * @return A Flow that emits the current Firebase user.
     */
    fun getAuthState(): Flow<com.google.firebase.auth.FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)

        // Send current state immediately
        trySend(auth.currentUser)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * Signs in a user with the given email and password.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @return A Result that is successful if the user was signed in, or a failure otherwise.
     */
    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs up a new user with the given email, password, and display name.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @param displayName The user's display name.
     * @return A Result that is successful if the user was signed up, or a failure otherwise.
     */
    suspend fun signUp(email: String, password: String, displayName: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            // Update display name
            val user = authResult.user
            if (user != null && displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates the user's display name.
     *
     * @param newName The new display name.
     * @return A Result that is successful if the display name was updated, or a failure otherwise.
     */
    suspend fun updateDisplayName(newName: String): Result<Unit> {
        return try {
            val user =
                auth.currentUser ?: return Result.failure(Exception("User not authenticated"))

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uploads a profile image to Firebase Storage and sets the user's photoURL.
     * Returns the download URL on success.
     *
     * @param imageUri The URI of the image to upload.
     * @return A Result that contains the download URL of the uploaded image on success, or an exception on failure.
     */
    suspend fun updateProfilePicture(imageUri: Uri): Result<String> {
        try {
            val user =
                auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            val uid = user.uid
            val storageRef =
                FirebaseStorage.getInstance().reference.child("profile_images/$uid/$uid.jpg")
            // Upload the file with metadata
            storageRef.putFile(imageUri).await()
            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            // Update user profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUrl.toUri())
                .build()
            user.updateProfile(profileUpdates).await()
            return Result.success(downloadUrl)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    /**
     * Sends a password reset email to the given email address.
     *
     * @param email The email address to send the password reset email to.
     * @return A Result that is successful if the email was sent, or a failure otherwise.
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs out the current user.
     *
     * @return A Result that is successful if the user was signed out, or a failure otherwise.
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * The currently signed-in user.
     */
    val user: com.google.firebase.auth.FirebaseUser?
        get() = auth.currentUser
}