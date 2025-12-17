package uk.ac.tees.mad.payclock.features.jobs.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.payclock.database.JobDao
import uk.ac.tees.mad.payclock.features.timelog.data.repository.TimeLogRepository
import java.util.Date

/**
 * A repository for jobs.
 *
 * @param auth The Firebase authentication instance.
 * @param firestore The Firebase Firestore instance.
 * @param timeLogRepository The repository for time logs.
 * @param jobDao The DAO for jobs.
 * @param externalScope The external coroutine scope.
 */
class JobRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val timeLogRepository: TimeLogRepository,
    private val jobDao: JobDao,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    /**
     * A Flow that emits the list of all jobs for the current user.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val jobs: Flow<List<Job>> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.flatMapLatest { user ->
        if (user != null) {
            jobDao.getAllJobs(user.uid)
        } else {
            flowOf(emptyList())
        }
    }

    /**
     * Adds a new job.
     *
     * @param name The name of the job.
     * @param hourlyRate The hourly rate of the job.
     */
    suspend fun addJob(name: String, hourlyRate: Double) {
        val userId = auth.currentUser?.uid ?: return
        val newJob = Job(
            id = firestore.collection("jobs").document().id,
            userId = userId,
            name = name,
            hourlyRate = hourlyRate,
            lastUpdated = Date()
        )
        jobDao.insertAll(listOf(newJob))
        firestore.collection("jobs").document(newJob.id).set(newJob).await()
    }

    /**
     * Updates a job.
     *
     * @param job The job to update.
     */
    suspend fun updateJob(job: Job) {
        if (job.id.isNotBlank()) {
            val updatedJob = job.copy(lastUpdated = Date())
            jobDao.insertAll(listOf(updatedJob))
            firestore.collection("jobs").document(updatedJob.id).set(updatedJob).await()
        }
    }

    /**
     * Removes a job.
     *
     * @param job The job to remove.
     */
    suspend fun removeJob(job: Job) {
        if (job.id.isNotBlank()) {
            timeLogRepository.deleteTimeLogsForJob(job.id)
            jobDao.delete(job)
            firestore.collection("jobs").document(job.id).delete().await()
        }
    }
}
