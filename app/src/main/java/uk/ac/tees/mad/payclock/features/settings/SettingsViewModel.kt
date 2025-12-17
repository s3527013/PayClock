package uk.ac.tees.mad.payclock.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.features.settings.data.SettingsRepository
import uk.ac.tees.mad.payclock.ui.theme.ThemeChoice

/**
 * A ViewModel for the settings screen.
 *
 * @param settingsRepository The repository for settings.
 * @param firebaseAuth The Firebase authentication instance.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _themeChoice = MutableStateFlow(ThemeChoice.SYSTEM)
    /**
     * A StateFlow that emits the user's theme choice.
     */
    val themeChoice: StateFlow<ThemeChoice> = _themeChoice.asStateFlow()

    private val _useDynamicColor = MutableStateFlow(true)
    /**
     * A StateFlow that emits whether the user is using dynamic color.
     */
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val prefs = settingsRepository.loadUserPreferences(userId)
                prefs?.let {
                    _themeChoice.value = ThemeChoice.valueOf(it.themeChoice)
                    _useDynamicColor.value = it.useDynamicColor
                }
            } catch (e: Exception) {
                // Handle error
                println("Failed to load preferences from Firestore: ${e.message}")
            }
        }
    }

    /**
     * Saves the user's theme choice.
     *
     * @param choice The theme choice to save.
     */
    fun saveThemeChoice(choice: ThemeChoice) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                settingsRepository.updateThemeChoice(userId, choice)
                _themeChoice.value = choice
            } catch (e: Exception) {
                // Handle error
                println("Failed to save theme choice to Firestore: ${e.message}")
            }
        }
    }

    /**
     * Saves whether the user is using dynamic color.
     *
     * @param useDynamic Whether to use dynamic color.
     */
    fun saveUseDynamicColor(useDynamic: Boolean) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                settingsRepository.updateDynamicColor(userId, useDynamic)
                _useDynamicColor.value = useDynamic
            } catch (e: Exception) {
                // Handle error
                println("Failed to save dynamic color preference to Firestore: ${e.message}")
            }
        }
    }
}