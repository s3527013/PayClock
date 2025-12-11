package uk.ac.tees.mad.payclock.features.settings.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.payclock.database.PayClockDatabase
import uk.ac.tees.mad.payclock.ui.theme.ThemeChoice
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val firestore: FirebaseFirestore, private val database: PayClockDatabase
) {
    private val userPreferencesDao = database.userPreferencesDao()

    // Get user preferences from local cache
    fun getUserPreferences(userId: String): Flow<UserPreferences?> {
        return userPreferencesDao.getUserPreferences(userId).catch { e ->
                // Log error and return null
                emit(null)
            }
    }

    // Load user preferences (local cache first, then remote)
    suspend fun loadUserPreferences(userId: String): UserPreferences? {
        return try {
            // Try to get from local cache first
            val localPrefs = userPreferencesDao.getUserPreferences(userId).first()

            // Always try to sync from remote
            try {
                val remotePrefs = loadFromFirestore(userId)

                if (remotePrefs != null) {
                    // Remote data exists, update local cache
                    userPreferencesDao.insertUserPreferences(
                        remotePrefs.copy(
                            lastSynced = Date(), isDirty = false
                        )
                    )
                    return remotePrefs
                }
            } catch (e: Exception) {
                // Failed to load from remote, use local if available
            }

            // Return local if exists, otherwise create default
            localPrefs ?: createDefaultPreferences(userId)
        } catch (e: Exception) {
            null
        }
    }

    // Save user preferences (local + remote)
    suspend fun saveUserPreferences(preferences: UserPreferences): Boolean {
        return try {
            // Save to local cache (mark as dirty for sync)
            userPreferencesDao.insertUserPreferences(preferences.copy(isDirty = true))

            // Try to save to remote
            saveToFirestore(preferences)

            // Mark as synced if successful
            userPreferencesDao.markDirty(preferences.userId, false)
            userPreferencesDao.updateLastSync(preferences.userId, Date())

            true
        } catch (e: Exception) {
            false
        }
    }

    // Update theme choice
    suspend fun updateThemeChoice(userId: String, themeChoice: ThemeChoice): Boolean {
        return try {
            // Get current preferences
            val currentPrefs = userPreferencesDao.getUserPreferences(userId).first()

            if (currentPrefs != null) {
                // Update local
                val updatedPrefs = currentPrefs.copy(
                    themeChoice = themeChoice.name, lastUpdated = Timestamp.now(), isDirty = true
                )
                userPreferencesDao.updateUserPreferences(updatedPrefs)

                // Update remote
                updateOnFirestore(
                    userId, mapOf(
                        "theme_choice" to themeChoice.name, "last_updated" to Timestamp.now()
                    )
                )

                // Mark as synced
                userPreferencesDao.markDirty(userId, false)
                userPreferencesDao.updateLastSync(userId, Date())

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Update dynamic color setting
    suspend fun updateDynamicColor(userId: String, useDynamicColor: Boolean): Boolean {
        return try {
            // Get current preferences
            val currentPrefs = userPreferencesDao.getUserPreferences(userId).first()

            if (currentPrefs != null) {
                // Update local
                val updatedPrefs = currentPrefs.copy(
                    useDynamicColor = useDynamicColor, lastUpdated = Timestamp.now(), isDirty = true
                )
                userPreferencesDao.updateUserPreferences(updatedPrefs)

                // Update remote
                updateOnFirestore(
                    userId, mapOf(
                        "use_dynamic_color" to useDynamicColor, "last_updated" to Timestamp.now()
                    )
                )

                // Mark as synced
                userPreferencesDao.markDirty(userId, false)
                userPreferencesDao.updateLastSync(userId, Date())

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Update display name
    suspend fun updateDisplayName(userId: String, displayName: String): Boolean {
        return try {
            // Get current preferences
            val currentPrefs = userPreferencesDao.getUserPreferences(userId).first()

            if (currentPrefs != null) {
                // Update local
                val updatedPrefs = currentPrefs.copy(
                    displayName = displayName, lastUpdated = Timestamp.now(), isDirty = true
                )
                userPreferencesDao.updateUserPreferences(updatedPrefs)

                // Update remote
                updateOnFirestore(
                    userId, mapOf(
                        "display_name" to displayName, "last_updated" to Timestamp.now()
                    )
                )

                // Mark as synced
                userPreferencesDao.markDirty(userId, false)
                userPreferencesDao.updateLastSync(userId, Date())

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Sync all dirty records
    suspend fun syncDirtyRecords(): Int {
        return try {
            val dirtyRecords = userPreferencesDao.getDirtyPreferences()
            var syncedCount = 0

            for (record in dirtyRecords) {
                try {
                    saveToFirestore(record)
                    userPreferencesDao.markDirty(record.userId, false)
                    userPreferencesDao.updateLastSync(record.userId, Date())
                    syncedCount++
                } catch (e: Exception) {
                    // Continue with next record
                }
            }

            syncedCount
        } catch (e: Exception) {
            0
        }
    }

    // Clear local cache
    suspend fun clearLocalCache(userId: String) {
        userPreferencesDao.deleteByUserId(userId)
    }

    // Private helper methods

    private suspend fun loadFromFirestore(userId: String): UserPreferences? {
        return try {
            val document =
                firestore.collection(UserPreferences.COLLECTION_NAME).document(userId).get().await()

            if (document.exists()) {
                document.toObject(UserPreferences::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveToFirestore(preferences: UserPreferences) {
        firestore.collection(UserPreferences.COLLECTION_NAME).document(preferences.userId)
            .set(preferences.toMap(), SetOptions.merge()).await()
    }

    private suspend fun updateOnFirestore(userId: String, updates: Map<String, Any>) {
        firestore.collection(UserPreferences.COLLECTION_NAME).document(userId).update(updates)
            .await()
    }

    private suspend fun createDefaultPreferences(userId: String): UserPreferences? {
        return try {
            val defaultPrefs = UserPreferences(
                userId = userId,
                themeChoice = ThemeChoice.SYSTEM.name,
                useDynamicColor = true,
                isDirty = true
            )

            userPreferencesDao.insertUserPreferences(defaultPrefs)
            defaultPrefs
        } catch (e: Exception) {
            null
        }
    }
}