package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.repository.AuthRepository

/**
 * Represents the different states of the login process.
 */
sealed class LoginState {
    object Idle : LoginState() // The initial state
    object Loading : LoginState() // When the login process is active
    data class Success(val token: String) : LoginState() // When login is successful
    data class Error(val message: String) : LoginState() // When an error occurs
}

/**
 * ViewModel for managing the login screen's state and logic.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    /**
     * Initiates the login process.
     *
     * @param email The user's email.
     * @param password The user's password.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.login(email, password)
            _loginState.value = result.fold(
                onSuccess = { token -> LoginState.Success(token) },
                onFailure = { exception -> LoginState.Error(exception.message ?: "An unknown error occurred") }
            )
        }
    }

    /**
     * Resets the login state back to Idle.
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
