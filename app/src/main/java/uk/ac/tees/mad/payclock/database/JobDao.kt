package uk.ac.tees.mad.payclock.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.features.jobs.data.Job

/**
 * A DAO for jobs.
 */
@Dao
interface JobDao {

    /**
     * Inserts a list of jobs into the database.
     *
     * @param jobs The list of jobs to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<Job>)

    /**
     * Returns a Flow of all jobs for the given user ID.
     *
     * @param userId The ID of the user.
     * @return A Flow of all jobs for the given user ID.
     */
    @Query("SELECT * FROM jobs WHERE userId = :userId ORDER BY name")
    fun getAllJobs(userId: String): Flow<List<Job>>

    /**
     * Returns the job with the given ID.
     *
     * @param jobId The ID of the job.
     * @return The job with the given ID, or null if it does not exist.
     */
    @Query("SELECT * FROM jobs WHERE id = :jobId")
    suspend fun getJobById(jobId: String): Job?

    /**
     * Deletes the given job from the database.
     *
     * @param job The job to delete.
     */
    @Delete
    suspend fun delete(job: Job)
}