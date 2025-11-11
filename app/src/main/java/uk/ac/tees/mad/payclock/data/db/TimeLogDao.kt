package uk.ac.tees.mad.payclock.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.data.models.TimeLog
import uk.ac.tees.mad.payclock.data.models.TimeLogWithJob

/**
 * Data Access Object for the time_logs table.
 */
@Dao
interface TimeLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeLog(timeLog: TimeLog)

    @Update
    suspend fun updateTimeLog(timeLog: TimeLog)

    @Delete
    suspend fun deleteTimeLog(timeLog: TimeLog)

    /**
     * Fetches all time logs from the database, joined with their corresponding job name.
     */
    @Query("""
        SELECT tl.*, j.name AS jobName 
        FROM time_logs AS tl
        LEFT JOIN jobs AS j ON tl.jobId = j.id
        ORDER BY tl.startTime DESC
    """)
    fun getAllTimeLogsWithJobName(): Flow<List<TimeLogWithJob>>

    /**
     * Fetches the most recent, currently active (not ended) time log, joined with its job name.
     */
    @Query("""
        SELECT tl.*, j.name AS jobName
        FROM time_logs AS tl
        LEFT JOIN jobs AS j ON tl.jobId = j.id
        WHERE tl.endTime IS NULL
        ORDER BY tl.startTime DESC
        LIMIT 1
    """)
    fun getActiveTimeLogWithJobName(): Flow<TimeLogWithJob?>
}
