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

/**
 * A repository for managing user settings and preferences.
 *
 * This repository handles both local (Room) and remote (Firestore) data sources
 * to ensure data persistence and synchronization across devices.
 *
 * @param firestore The [FirebaseFirestore] instance for remote data operations.
 * @param database The [PayClockDatabase] instance for local data caching.
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val firestore: FirebaseFirestore, private val database: PayClockDatabase
) {
    /** Data Access Object for UserPreferences. */
    private val userPreferencesDao = database.userPreferencesDao()

    /**
     * Gets user preferences from the local cache as a [Flow].
     *
     * @param userId The ID of the user whose preferences are to be retrieved.
     * @return A [Flow] emitting the [UserPreferences], or null if not found or an error occurs.
     */
    fun getUserPreferences(userId: String): Flow<UserPreferences?> {
        return userPreferencesDao.getUserPreferences(userId).catch { e ->
                // Log error and return null
                emit(null)
            }
    }

    /**
     * Loads user preferences, prioritizing local cache and then falling back to remote.
     * It also attempts to sync from remote to update the local cache.
     *
     * @param userId The ID of the user.
     * @return The [UserPreferences] if found, otherwise null.
     */
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

    /**
     * Saves user preferences to both local cache and remote Firestore.
     *
     * @param preferences The [UserPreferences] to save.
     * @return `true` if successful, `false` otherwise.
     */
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

    /**
     * Updates the theme choice for a user.
     *
     * @param userId The ID of the user.
     * @param themeChoice The new [ThemeChoice].
     * @return `true` if successful, `false` otherwise.
     */
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

    /**
     * Updates the dynamic color setting for a user.
     *
     * @param userId The ID of the user.
     * @param useDynamicColor The new value for the dynamic color setting.
     * @return `true` if successful, `false` otherwise.
     */
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

    /**
     * Updates the display name for a user.
     *
     * @param userId The ID of the user.
     * @param displayName The new display name.
     * @return `true` if successful, `false` otherwise.
     */
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

    /**
     * Syncs all records marked as 'dirty' from the local cache to Firestore.
     *
     * @return The number of records successfully synced.
     */
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

    /**
     * Clears the local cache of user preferences for a specific user.
     *
     * @param userId The ID of the user whose cache should be cleared.
     */
    suspend fun clearLocalCache(userId: String) {
        userPreferencesDao.deleteByUserId(userId)
    }

    // Private helper methods

    /**
     * Loads user preferences from Firestore.
     *
     * @param userId The ID of the user.
     * @return The [UserPreferences] from Firestore, or null if not found.
     */
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

    /**
     * Saves user preferences to Firestore.
     *
     * @param preferences The [UserPreferences] to be saved.
     */
    private suspend fun saveToFirestore(preferences: UserPreferences) {
        firestore.collection(UserPreferences.COLLECTION_NAME).document(preferences.userId)
            .set(preferences.toMap(), SetOptions.merge()).await()
    }

    /**
     * Updates specific fields for a user's preferences in Firestore.
     *
     * @param userId The ID of the user.
     * @param updates A map of fields to update.
     */
    private suspend fun updateOnFirestore(userId: String, updates: Map<String, Any>) {
        firestore.collection(UserPreferences.COLLECTION_NAME).document(userId).update(updates)
            .await()
    }

    /**
     * Creates and saves default preferences for a new user.
     *
     * @param userId The ID of the new user.
     * @return The created [UserPreferences] with default values.
     */
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
