package uk.ac.tees.mad.payclock.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.features.breaks.data.Breaks

/**
 * A DAO for breaks.
 */
@Dao
interface BreakDao {

    /**
     * Inserts a list of breaks into the database.
     *
     * @param breaks The list of breaks to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(breaks: List<Breaks>)

    /**
     * Inserts a single break into the database.
     *
     * @param breakEntry The break to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(breakEntry: Breaks)

    /**
     * Returns a Flow of all breaks for the given user ID.
     *
     * @param userId The ID of the user.
     * @return A Flow of all breaks for the given user ID.
     */
    @Query("SELECT * FROM breaks WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllBreaks(userId: String): Flow<List<Breaks>>

    /**
     * Returns a Flow of the active break for the given user ID.
     *
     * @param userId The ID of the user.
     * @return A Flow of the active break for the given user ID, or null if there is no active break.
     */
    @Query("SELECT * FROM breaks WHERE userId = :userId AND endTime IS NULL LIMIT 1")
    fun getActiveBreak(userId: String): Flow<Breaks?>
}