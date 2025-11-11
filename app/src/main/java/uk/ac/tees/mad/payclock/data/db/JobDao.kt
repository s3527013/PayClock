package uk.ac.tees.mad.payclock.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.data.models.Job

/**
 * Data Access Object for the jobs table.
 */
@Dao
interface JobDao {

    /**
     * Inserts a job into the table. If the job already exists, it replaces it.
     * @param job The job to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job)

    /**
     * Updates a job in the database.
     * @param job The job to update.
     */
    @Update
    suspend fun updateJob(job: Job)

    /**
     * Deletes a job from the database.
     * @param job The job to delete.
     */
    @Delete
    suspend fun deleteJob(job: Job)

    /**
     * Fetches all jobs from the table, ordered by name.
     * @return A Flow of a list of jobs, which will automatically update the UI on changes.
     */
    @Query("SELECT * FROM jobs ORDER BY name ASC")
    fun getAllJobs(): Flow<List<Job>>

    /**
     * Fetches a single job by its ID.
     * @param jobId The ID of the job to fetch.
     * @return A Flow of the job.
     */
    @Query("SELECT * FROM jobs WHERE id = :jobId")
    fun getJobById(jobId: Int): Flow<Job>
}
