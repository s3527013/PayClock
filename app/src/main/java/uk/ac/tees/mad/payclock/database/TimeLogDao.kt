package uk.ac.tees.mad.payclock.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLog

@Dao
interface TimeLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(timeLogs: List<TimeLog>)

    @Query("SELECT * FROM time_logs WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllTimeLogs(userId: String): Flow<List<TimeLog>>

    @Query("SELECT * FROM time_logs WHERE userId = :userId AND endTime IS NULL LIMIT 1")
    fun getActiveTimeLog(userId: String): Flow<TimeLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timeLog: TimeLog)

    @Query("DELETE FROM time_logs WHERE id = :timeLogId")
    suspend fun delete(timeLogId: String)
    
    @Query("DELETE FROM time_logs WHERE jobId = :jobId")
    suspend fun deleteTimeLogsForJob(jobId: String)
}