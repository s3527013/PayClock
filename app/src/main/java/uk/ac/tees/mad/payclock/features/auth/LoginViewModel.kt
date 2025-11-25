package uk.ac.tees.mad.payclock.features.auth

import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    private val auth = Firebase.auth

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password cannot be empty.")
            return
        }
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    fun signInWithGoogleCredential(credential: Credential) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                when (credential) {
                    is CustomCredential -> {
                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                val googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(credential.data)
                                val firebaseCredential = GoogleAuthProvider.getCredential(
                                    googleIdTokenCredential.idToken,
                                    null
                                )
                                auth.signInWithCredential(firebaseCredential).await()
                                _loginState.value = LoginState.Success
                            } catch (e: GoogleIdTokenParsingException) {
                                _loginState.value =
                                    LoginState.Error("Received an invalid google id token response: ${e.message}")
                            }
                        } else {
                            _loginState.value =
                                LoginState.Error("Unexpected custom credential type: ${credential.type}")
                        }
                    }

                    else -> {
                        _loginState.value = LoginState.Error("Unexpected credential type.")
                    }
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Google Sign-in failed.")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
