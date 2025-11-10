package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.repository.AuthRepository

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    object Success : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

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
