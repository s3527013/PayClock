package uk.ac.tees.mad.payclock.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    object Success : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

class ForgotPasswordViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _state = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun requestReset(email: String) {
        viewModelScope.launch {
            _state.value = ForgotPasswordState.Loading
            // In a real app, this would trigger an email to be sent from your Django server.
            delay(1000)
            if (email.contains("@")) {
                _state.value = ForgotPasswordState.Success
            } else {
                _state.value = ForgotPasswordState.Error("Invalid email address.")
            }
        }
    }

    fun resetState() {
        _state.value = ForgotPasswordState.Idle
    }
}
