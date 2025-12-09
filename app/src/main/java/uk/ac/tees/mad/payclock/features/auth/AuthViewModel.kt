package uk.ac.tees.mad.payclock.features.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.core.Graph

class AuthViewModel(
    private val repository: AuthRepository = Graph.authRepository
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = repository.user
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val authState: StateFlow<AuthState> = repository.user.map {
        if (it != null) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, AuthState.Unknown)

    fun logout() {
        repository.logout()
    }

    /**
     * Uploads the image and updates user profile photo.
     * Calls the provided callback with Result<String> (download URL or error).
     */
    fun updateProfilePicture(imageUri: Uri, callback: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateProfilePicture(imageUri)
            callback(result)
        }
    }

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            repository.updateDisplayName(newName)
        }
    }
}

sealed class AuthState {
    object Unknown : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}
