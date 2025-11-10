package uk.ac.tees.mad.payclock.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.models.User

/**
 * ViewModel responsible for managing the state of a single User object.
 *
 * It holds the user data and exposes it to the UI in a state-aware manner,
 * allowing UI components to automatically update when the user data changes.
 */
class UserViewModel : ViewModel() {

    // Private mutable state for the user.
    // Only the ViewModel can change this.
    private val _user = mutableStateOf<User?>(null)

    // Public immutable state exposed to the UI.
    // The UI can read this, but not change it directly.
    val user: State<User?> = _user

    /**
     * Updates the current user state with a new User object.
     *
     * @param newUser The new user data to display.
     */
    fun setUser(newUser: User?) {
        _user.value = newUser
    }

    /**
     * Clears the current user data.
     */
    fun clearUser() {
        _user.value = null
    }
}
