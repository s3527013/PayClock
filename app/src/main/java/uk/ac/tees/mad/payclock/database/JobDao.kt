package uk.ac.tees.mad.payclock.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.features.jobs.data.Job

@Dao
interface JobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<Job>)

    @Query("SELECT * FROM jobs WHERE userId = :userId ORDER BY name")
    fun getAllJobs(userId: String): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE id = :jobId")
    suspend fun getJobById(jobId: String): Job?

    @Delete
    suspend fun delete(job: Job)
}