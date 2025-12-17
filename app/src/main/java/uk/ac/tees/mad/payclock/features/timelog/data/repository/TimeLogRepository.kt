package uk.ac.tees.mad.payclock.features.timelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.payclock.database.JobDao
import uk.ac.tees.mad.payclock.database.TimeLogDao
import uk.ac.tees.mad.payclock.features.breaks.data.BreakRepository
import uk.ac.tees.mad.payclock.features.jobs.data.Job
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLog
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLogWithJob
import java.util.Date

/**
 * Repository for managing time logs.
 *
 * This class handles all data operations related to time logs, including
 * creating, reading, updating, and deleting them. It coordinates between
 * a local Room database and a remote Firestore database to ensure data
 * persistence and synchronization.
 *
 * @param auth The Firebase Authentication instance.
 * @param firestore The Firebase Firestore instance for remote data operations.
 * @param breakRepository The repository for managing breaks.
 * @param timeLogDao The Data Access Object for time logs.
 * @param jobDao The Data Access Object for jobs.
 * @param externalScope A coroutine scope for background tasks.
 */
class TimeLogRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val breakRepository: BreakRepository,
    private val timeLogDao: TimeLogDao,
    private val jobDao: JobDao,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    init {
        externalScope.launch {
            authState.collect { user ->
                if (user != null) {
                    syncFirestoreData(user.uid)
                }
            }
        }
    }

    /**
     * A flow of all time logs for the current user, combined with their associated job names.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val allTimeLogs: Flow<List<TimeLogWithJob>> = authState.flatMapLatest { user ->
        if (user != null) {
            val timeLogsFlow = timeLogDao.getAllTimeLogs(user.uid)
            val jobsFlow = jobDao.getAllJobs(user.uid).map { it.associateBy(Job::id) }

            timeLogsFlow.combine(jobsFlow) { timeLogs, jobsMap ->
                timeLogs.map { timeLog ->
                    TimeLogWithJob(
                        timeLog = timeLog,
                        jobName = jobsMap[timeLog.jobId]?.name ?: "Unknown Job"
                    )
                }
            }
        } else {
            flowOf(emptyList())
        }
    }

    /**
     * A flow of the currently active time log (i.e., a shift that has started but not ended).
     */
    val activeTimeLog: Flow<TimeLogWithJob?> = allTimeLogs.map { logs ->
        logs.find { it.timeLog.endTime == null }
    }

    /**
     * Starts a new shift for a given job.
     *
     * @param jobId The ID of the job to start a shift for.
     * @param startLat The starting latitude (optional).
     * @param startLng The starting longitude (optional).
     * @param startAddress The starting address (optional).
     */
    suspend fun startNewShift(
        jobId: String,
        startLat: Double? = null,
        startLng: Double? = null,
        startAddress: String? = null
    ) {
        val userId = auth.currentUser?.uid ?: return
        if (activeTimeLog.firstOrNull() == null) {
            val newLog = TimeLog(
                id = firestore.collection("timeLogs").document().id,
                userId = userId,
                jobId = jobId,
                startTime = Date(),
                startLatitude = startLat,
                startLongitude = startLng,
                startAddress = startAddress,
                lastUpdated = Date()
            )
            timeLogDao.insert(newLog)
            firestore.collection("timeLogs").document(newLog.id).set(newLog).await()
        }
    }

    /**
     * Ends the current active shift.
     *
     * @param endLat The ending latitude (optional).
     * @param endLng The ending longitude (optional).
     * @param endAddress The ending address (optional).
     */
    suspend fun endCurrentShift(
        endLat: Double? = null,
        endLng: Double? = null,
        endAddress: String? = null
    ) {
        activeTimeLog.firstOrNull()?.timeLog?.let { log ->
            if (log.id.isNotBlank()) {
                breakRepository.endBreak()

                val now = Date()

                val totalBreakTime = breakRepository.breaks.firstOrNull()
                    ?.filter { it.timeLogId == log.id && it.duration != null }
                    ?.sumOf { it.duration ?: 0L } ?: 0L

                val duration = if (log.startTime != null) {
                    val shiftDuration = (now.time - log.startTime.time) / 60000
                    shiftDuration - totalBreakTime
                } else {
                    0
                }

                val updatedLog = log.copy(
                    endTime = now,
                    duration = duration,
                    endLatitude = endLat,
                    endLongitude = endLng,
                    endAddress = endAddress,
                    lastUpdated = Date()
                )
                timeLogDao.insert(updatedLog)
                firestore.collection("timeLogs").document(log.id).set(updatedLog).await()
            }
        }
    }

    /**
     * Deletes a specific time log.
     *
     * @param timeLog The time log to delete.
     */
    suspend fun deleteTimeLog(timeLog: TimeLog) {
        if (timeLog.id.isNotBlank()) {
            timeLogDao.delete(timeLog.id)
            firestore.collection("timeLogs").document(timeLog.id).delete().await()
        }
    }

    /**
     * Deletes all time logs associated with a specific job.
     *
     * @param jobId The ID of the job whose time logs are to be deleted.
     */
    suspend fun deleteTimeLogsForJob(jobId: String) {
        val userId = auth.currentUser?.uid ?: return
        timeLogDao.deleteTimeLogsForJob(jobId)

        val timeLogsQuerySnapshot = firestore.collection("timeLogs")
            .whereEqualTo("userId", userId)
            .whereEqualTo("jobId", jobId)
            .get()
            .await()

        firestore.runBatch { batch ->
            timeLogsQuerySnapshot.documents.forEach { document ->
                batch.delete(document.reference)
            }
        }
    }

    /**
     * Syncs data from Firestore to the local Room database.
     *
     * @param userId The ID of the user whose data is to be synced.
     */
    private fun syncFirestoreData(userId: String) {
        // Sync TimeLogs
        firestore.collection("timeLogs")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    externalScope.launch {
                        val timeLogs = it.toObjects<TimeLog>()
                        timeLogDao.insertAll(timeLogs)
                    }
                }
            }

        // Sync Jobs
        firestore.collection("jobs")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    externalScope.launch {
                        val jobs = it.toObjects<Job>()
                        jobDao.insertAll(jobs)
                    }
                }
            }
    }
}