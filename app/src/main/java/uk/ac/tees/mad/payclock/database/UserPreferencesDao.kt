package uk.ac.tees.mad.payclock.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.features.settings.data.UserPreferences
import java.util.Date

@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    fun getUserPreferences(userId: String): Flow<UserPreferences?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferences)

    @Update
    suspend fun updateUserPreferences(preferences: UserPreferences)

    @Delete
    suspend fun deleteUserPreferences(preferences: UserPreferences)

    @Query("SELECT * FROM user_preferences WHERE isdirty = 1")
    suspend fun getDirtyPreferences(): List<UserPreferences>

    @Query("UPDATE user_preferences SET lastsynced = :syncTime WHERE userId = :userId")
    suspend fun updateLastSync(userId: String, syncTime: Date)

    @Query("UPDATE user_preferences SET isdirty = :isDirty WHERE userId = :userId")
    suspend fun markDirty(userId: String, isDirty: Boolean)

    @Query("DELETE FROM user_preferences WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)
}