package uk.ac.tees.mad.payclock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.repository.AuthRepository

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(val userId: String) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

class SignUpViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _state = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _state.value = SignUpState.Loading
            val result = authRepository.signUp(email, password)
            _state.value = result.fold(
                onSuccess = { userId -> SignUpState.Success(userId) },
                onFailure = { exception -> SignUpState.Error(exception.message ?: "An unknown error occurred") }
            )
        }
    }

    fun resetState() {
        _state.value = SignUpState.Idle
    }
}
