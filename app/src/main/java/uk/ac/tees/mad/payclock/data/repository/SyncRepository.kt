package uk.ac.tees.mad.payclock.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.payclock.data.db.JobDao
import uk.ac.tees.mad.payclock.data.db.TimeLogDao
import uk.ac.tees.mad.payclock.data.models.Job
import uk.ac.tees.mad.payclock.data.models.TimeLog

class SyncRepository(
    private val jobDao: JobDao,
    private val timeLogDao: TimeLogDao,
    private val userId: String
) {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val jobsCollection = firestore.collection("users").document(userId).collection("jobs")
    private val timeLogsCollection =
        firestore.collection("users").document(userId).collection("time_logs")

    /**
     * Uploads all local changes (jobs and time logs) to Firestore.
     */
    suspend fun syncUp() {
        // In a real app, you would fetch only jobs with isPendingSync = true
        val localJobs = jobDao.getAllJobs().first()
        localJobs.forEach { job ->
            jobsCollection.document(job.id).set(job).await()
        }

        val localTimeLogs = timeLogDao.getAllTimeLogsWithJobName().first()
        localTimeLogs.forEach { timeLogWithJobName ->
            // Assuming 'timeLog' is the name of the property holding the TimeLog object
            timeLogsCollection.document(timeLogWithJobName.timeLog.id)
                .set(timeLogWithJobName.timeLog).await()
        }
    }

    /**
     * Downloads all data from Firestore and updates the local database.
     */
    suspend fun syncDown() {
        val remoteJobs = jobsCollection.get().await().toObjects(Job::class.java)
        remoteJobs.forEach { job ->
            jobDao.insertJob(job.copy(isPendingSync = false))
        }

        val remoteTimeLogs = timeLogsCollection.get().await().toObjects(TimeLog::class.java)
        remoteTimeLogs.forEach { timeLog ->
            timeLogDao.insertTimeLog(timeLog)
        }
    }
}
