package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.repository.AuthRepository

sealed class AuthState {
    object Unknown : AuthState() // Initial state, we don't know if the user is logged in
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

/**
 * A global ViewModel to manage the overall authentication state of the app.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    /**
     * Checks SharedPreferences for a valid token to determine the initial auth state.
     */
    private fun checkAuthStatus() {
        viewModelScope.launch {
            val token = authRepository.getAuthToken()
            if (token != null) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Clears the auth token and updates the state to Unauthenticated.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.clearAuthToken()
            _authState.value = AuthState.Unauthenticated
        }
    }
}
