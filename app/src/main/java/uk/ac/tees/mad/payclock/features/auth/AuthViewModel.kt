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

class AuthViewModel(
    private val repository: AuthRepository = Graph.authRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()

    val authState: StateFlow<AuthState> = currentUser.map {
        when {
            it != null -> AuthState.Authenticated
            else -> AuthState.Unauthenticated
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, AuthState.Unknown)

    init {
        // Listen for auth state changes
        repository.getAuthState().onEach { user ->
            _currentUser.value = user
            if (user != null) {
                // Load user preferences when user logs in
                loadUserPreferences(user.uid)
            } else {
                // Clear preferences when user logs out
                _userPreferences.value = null
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Load user preferences from Firestore
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
                // Create default preferences if they don't exist
                createDefaultPreferences(userId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create default user preferences
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
     * Updates the user's display name in both Auth and Firestore
     */
    fun updateDisplayName(newName: String, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Update in Firebase Auth
                val result = repository.updateDisplayName(newName)

                if (result.isSuccess) {
                    // Update in Firestore if we have a user ID
                    val userId = _currentUser.value?.uid
                    if (userId != null) {
                        try {
                            Graph.settingsRepository.updateDisplayName(userId, newName)
                            // Update local preferences
                            _userPreferences.value?.let { currentPrefs ->
                                _userPreferences.value = currentPrefs.copy(displayName = newName)
                            }
                        } catch (e: Exception) {
                            // Firestore update failed, but Auth update succeeded
                            _errorMessage.value =
                                "Display name updated, but failed to sync to cloud: ${e.message}"
                            onComplete(Result.success(Unit))
                            return@launch
                        }
                    }

                    _errorMessage.value = null
                    onComplete(Result.success(Unit))
                } else {
                    _errorMessage.value =
                        result.exceptionOrNull()?.message ?: "Failed to update display name"
                    onComplete(
                        Result.failure(
                            result.exceptionOrNull() ?: Exception("Unknown error")
                        )
                    )
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
     * Uploads the image and updates user profile photo.
     * Calls the provided callback with Result<String> (download URL or error).
     */
    fun updateProfilePicture(imageUriString: String, callback: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateProfilePicture(imageUriString.toUri())
            callback(result)
        }
    }

    /**
     * Sign out the user
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
     * Sign in with email and password
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
     * Sign up with email and password
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
     * Send password reset email
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
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

sealed class AuthState {
    object Unknown : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}