package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.repository.AuthRepository

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    object Success : SignUpState()
    data class Error(val message: String) : SignUpState()
}

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _state = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            _state.value = SignUpState.Loading
            // In a real app, you would make a network request to your Django server here.
            delay(1500) // Simulate network latency
            if (password.length < 8) {
                _state.value = SignUpState.Error("Password must be at least 8 characters.")
            } else {
                // On success, you would typically receive a token and save it.
                authRepository.saveAuthToken("fake-token-for-$email")
                _state.value = SignUpState.Success
            }
        }
    }

    fun resetState() {
        _state.value = SignUpState.Idle
    }
}
