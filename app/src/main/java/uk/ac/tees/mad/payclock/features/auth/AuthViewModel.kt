package uk.ac.tees.mad.payclock.features.auth

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.core.Graph
import uk.ac.tees.mad.payclock.features.settings.data.UserPreferences

/**
 * ViewModel responsible for managing authentication state and user data.
 *
 * This ViewModel handles user sign-in, sign-up, sign-out, password reset, and profile updates.
 * It observes the authentication state from the [AuthRepository] and provides the current user,
 * authentication state, and user preferences to the UI.
 *
 * @param repository The authentication repository, which handles the underlying data operations.
 */
class AuthViewModel(
    private val repository: AuthRepository = Graph.authRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    /**
     * A [StateFlow] that emits the currently authenticated [FirebaseUser], or null if no user is signed in.
     */
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    /**
     * A [StateFlow] that emits `true` while an asynchronous operation is in progress, and `false` otherwise.
     */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    /**
     * A [StateFlow] that emits an error message string if an operation fails, or null if there is no error.
     */
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    /**
     * A [StateFlow] that emits the [UserPreferences] for the current user.
     */
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()

    /**
     * A [StateFlow] that represents the current authentication state as an [AuthState] object.
     * It maps the `currentUser` flow to discrete states: [AuthState.Authenticated], [AuthState.Unauthenticated], or [AuthState.Unknown].
     */
    val authState: StateFlow<AuthState> = currentUser.map {
        when {
            it != null -> AuthState.Authenticated
            else -> AuthState.Unauthenticated
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, AuthState.Unknown)

    init {
        repository.getAuthState().onEach { user ->
            _currentUser.value = user
            if (user != null) {
                loadUserPreferences(user.uid)
            } else {
                _userPreferences.value = null
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Loads the preferences for the specified user from the repository.
     *
     * @param userId The unique ID of the user whose preferences are to be loaded.
     */
    private fun loadUserPreferences(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val preferences = Graph.settingsRepository.loadUserPreferences(userId)
                _userPreferences.value = preferences
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load user preferences: ${e.message}"
                createDefaultPreferences(userId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Creates and saves default preferences for a new user.
     *
     * @param userId The unique ID of the user for whom to create default preferences.
     */
    private fun createDefaultPreferences(userId: String) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUser.value
                if (currentUser != null) {
                    val defaultPreferences = UserPreferences(
                        userId = userId,
                        email = currentUser.email ?: "",
                        displayName = currentUser.displayName ?: "",
                        themeChoice = "SYSTEM",
                        useDynamicColor = true
                    )
                    Graph.settingsRepository.saveUserPreferences(defaultPreferences)
                    _userPreferences.value = defaultPreferences
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create default preferences: ${e.message}"
            }
        }
    }

    /**
     * Updates the user's display name in both Firebase Authentication and Firestore.
     *
     * @param newName The new display name for the user.
     * @param onComplete A callback that is invoked with the [Result] of the operation.
     */
    fun updateDisplayName(newName: String, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.updateDisplayName(newName)
                if (result.isSuccess) {
                    val userId = _currentUser.value?.uid
                    if (userId != null) {
                        try {
                            Graph.settingsRepository.updateDisplayName(userId, newName)
                            _userPreferences.value?.let { currentPrefs ->
                                _userPreferences.value = currentPrefs.copy(displayName = newName)
                            }
                        } catch (e: Exception) {
                            _errorMessage.value = "Display name updated, but failed to sync to cloud: ${e.message}"
                            onComplete(Result.success(Unit))
                            return@launch
                        }
                    }
                    _errorMessage.value = null
                    onComplete(Result.success(Unit))
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update display name"
                    onComplete(Result.failure(result.exceptionOrNull() ?: Exception("Unknown error")))
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update display name: ${e.message}"
                onComplete(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Uploads an image and updates the user's profile picture URL.
     *
     * @param imageUriString The string representation of the image URI to upload.
     * @param callback A callback that is invoked with the [Result] containing the download URL string.
     */
    fun updateProfilePicture(imageUriString: String, callback: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateProfilePicture(imageUriString.toUri())
            callback(result)
        }
    }

    /**
     * Signs out the current user.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.signOut()
                _currentUser.value = null
                _userPreferences.value = null
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to sign out: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Signs in a user with their email and password.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @param onComplete A callback that is invoked with the [Result] of the sign-in operation.
     */
    fun signIn(email: String, password: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.signIn(email, password)
                if (result.isSuccess) {
                    _errorMessage.value = null
                }
                onComplete(result)
            } catch (e: Exception) {
                _errorMessage.value = "Sign in failed: ${e.message}"
                onComplete(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Creates a new user account with the given email, password, and display name.
     *
     * @param email The new user's email.
     * @param password The new user's password.
     * @param displayName The new user's display name.
     * @param onComplete A callback that is invoked with the [Result] of the sign-up operation.
     */
    fun signUp(
        email: String,
        password: String,
        displayName: String,
        onComplete: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.signUp(email, password, displayName)
                if (result.isSuccess) {
                    _errorMessage.value = null
                }
                onComplete(result)
            } catch (e: Exception) {
                _errorMessage.value = "Sign up failed: ${e.message}"
                onComplete(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email The user's email address.
     * @param onComplete A callback that is invoked with the [Result] of the operation.
     */
    fun resetPassword(email: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.resetPassword(email)
                if (result.isSuccess) {
                    _errorMessage.value = null
                }
                onComplete(result)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send reset email: ${e.message}"
                onComplete(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clears the current error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

/**
 * Represents the different states of user authentication.
 */
sealed class AuthState {
    /**
     * The initial state before the authentication status has been determined.
     */
    object Unknown : AuthState()
    /**
     * The state representing that a user is successfully authenticated.
     */
    object Authenticated : AuthState()
    /**
     * The state representing that no user is currently authenticated.
     */
    object Unauthenticated : AuthState()
}