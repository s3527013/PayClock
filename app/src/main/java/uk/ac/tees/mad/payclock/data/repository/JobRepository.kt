package uk.ac.tees.mad.payclock.data.repository

import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.data.db.JobDao
import uk.ac.tees.mad.payclock.data.models.Job

/**
 * Repository for managing Job data using the Room database.
 * This class abstracts the data source from the rest of the app.
 */
class JobRepository(private val jobDao: JobDao) {

    /**
     * Retrieves all jobs from the database as a Flow.
     * The Flow will automatically emit new values when the data changes.
     */
    val allJobs: Flow<List<Job>> = jobDao.getAllJobs()

    /**
     * Inserts a new job into the database.
     * This is a suspend function, so it must be called from a coroutine.
     */
    suspend fun insert(job: Job) {
        jobDao.insertJob(job)
    }

    /**
     * Deletes a job from the database.
     */
    suspend fun delete(job: Job) {
        jobDao.deleteJob(job)
    }

    /**
     * Updates a job in the database.
     */
    suspend fun update(job: Job) {
        jobDao.updateJob(job)
    }
}
