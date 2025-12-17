package uk.ac.tees.mad.payclock.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.features.settings.data.UserPreferences
import java.util.Date

/**
 * A DAO for user preferences.
 */
@Dao
interface UserPreferencesDao {

    /**
     * Returns a Flow of the user preferences for the given user ID.
     *
     * @param userId The ID of the user.
     * @return A Flow of the user preferences for the given user ID, or null if they do not exist.
     */
    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    fun getUserPreferences(userId: String): Flow<UserPreferences?>

    /**
     * Inserts the given user preferences into the database.
     *
     * @param preferences The user preferences to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferences)

    /**
     * Updates the given user preferences in the database.
     *
     * @param preferences The user preferences to update.
     */
    @Update
    suspend fun updateUserPreferences(preferences: UserPreferences)

    /**
     * Deletes the given user preferences from the database.
     *
     * @param preferences The user preferences to delete.
     */
    @Delete
    suspend fun deleteUserPreferences(preferences: UserPreferences)

    /**
     * Returns a list of all user preferences that are marked as dirty.
     *
     * @return A list of all user preferences that are marked as dirty.
     */
    @Query("SELECT * FROM user_preferences WHERE isdirty = 1")
    suspend fun getDirtyPreferences(): List<UserPreferences>

    /**
     * Updates the last sync time for the given user ID.
     *
     * @param userId The ID of the user.
     * @param syncTime The new last sync time.
     */
    @Query("UPDATE user_preferences SET lastsynced = :syncTime WHERE userId = :userId")
    suspend fun updateLastSync(userId: String, syncTime: Date)

    /**
     * Marks the user preferences for the given user ID as dirty or not dirty.
     *
     * @param userId The ID of the user.
     * @param isDirty Whether the user preferences should be marked as dirty.
     */
    @Query("UPDATE user_preferences SET isdirty = :isDirty WHERE userId = :userId")
    suspend fun markDirty(userId: String, isDirty: Boolean)

    /**
     * Deletes the user preferences for the given user ID from the database.
     *
     * @param userId The ID of the user.
     */
    @Query("DELETE FROM user_preferences WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)
}