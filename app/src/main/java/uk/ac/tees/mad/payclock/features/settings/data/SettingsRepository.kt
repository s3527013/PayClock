package uk.ac.tees.mad.payclock.features.settings.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.payclock.ui.theme.ThemeChoice

class SettingsRepository(
    private val firestore: FirebaseFirestore
) {
    suspend fun loadUserPreferences(userId: String): UserPreferences? {
        return try {
            val document = firestore.collection(UserPreferences.COLLECTION_NAME)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                document.toObject(UserPreferences::class.java)
            } else {
                val defaultPrefs = UserPreferences(
                    userId = userId,
                    themeChoice = ThemeChoice.SYSTEM.name,
                    useDynamicColor = true
                )
                saveUserPreferences(defaultPrefs)
                defaultPrefs
            }
        } catch (e: Exception) {
            throw Exception("Failed to load user preferences: ${e.message}")
        }
    }

    suspend fun saveUserPreferences(preferences: UserPreferences) {
        try {
            firestore.collection(UserPreferences.COLLECTION_NAME)
                .document(preferences.userId)
                .set(preferences.toMap(), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to save user preferences: ${e.message}")
        }
    }

    suspend fun updateThemeChoice(userId: String, themeChoice: ThemeChoice) {
        try {
            firestore.collection(UserPreferences.COLLECTION_NAME)
                .document(userId)
                .update(
                    "theme_choice", themeChoice.name,
                    "last_updated", Timestamp.now()
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to update theme: ${e.message}")
        }
    }

    suspend fun updateDynamicColor(userId: String, useDynamicColor: Boolean) {
        try {
            firestore.collection(UserPreferences.COLLECTION_NAME)
                .document(userId)
                .update(
                    "use_dynamic_color", useDynamicColor,
                    "last_updated", Timestamp.now()
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to update dynamic color setting: ${e.message}")
        }
    }

    suspend fun updateDisplayName(userId: String, displayName: String) {
        try {
            firestore.collection(UserPreferences.COLLECTION_NAME)
                .document(userId)
                .update(
                    "display_name", displayName,
                    "last_updated", Timestamp.now()
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to update display name: ${e.message}")
        }
    }
}