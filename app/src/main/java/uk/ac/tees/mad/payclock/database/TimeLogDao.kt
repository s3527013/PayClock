package uk.ac.tees.mad.payclock.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLog

/**
 * A DAO for time logs.
 */
@Dao
interface TimeLogDao {

    /**
     * Inserts a list of time logs into the database.
     *
     * @param timeLogs The list of time logs to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(timeLogs: List<TimeLog>)

    /**
     * Returns a Flow of all time logs for the given user ID.
     *
     * @param userId The ID of the user.
     * @return A Flow of all time logs for the given user ID.
     */
    @Query("SELECT * FROM time_logs WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllTimeLogs(userId: String): Flow<List<TimeLog>>

    /**
     * Returns a Flow of the active time log for the given user ID.
     *
     * @param userId The ID of the user.
     * @return A Flow of the active time log for the given user ID, or null if there is no active time log.
     */
    @Query("SELECT * FROM time_logs WHERE userId = :userId AND endTime IS NULL LIMIT 1")
    fun getActiveTimeLog(userId: String): Flow<TimeLog?>

    /**
     * Inserts a single time log into the database.
     *
     * @param timeLog The time log to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timeLog: TimeLog)

    /**
     * Deletes the time log with the given ID from the database.
     *
     * @param timeLogId The ID of the time log to delete.
     */
    @Query("DELETE FROM time_logs WHERE id = :timeLogId")
    suspend fun delete(timeLogId: String)
    
    /**
     * Deletes all time logs for the given job ID from the database.
     *
     * @param jobId The ID of the job.
     */
    @Query("DELETE FROM time_logs WHERE jobId = :jobId")
    suspend fun deleteTimeLogsForJob(jobId: String)
}