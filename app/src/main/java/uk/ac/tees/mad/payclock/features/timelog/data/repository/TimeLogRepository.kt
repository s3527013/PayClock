package uk.ac.tees.mad.payclock.features.timelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.payclock.features.breaks.data.BreakRepository
import uk.ac.tees.mad.payclock.features.jobs.data.Job
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLog
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLogWithJob
import java.util.Date

class TimeLogRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val breakRepository: BreakRepository
) {

    private val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val allTimeLogs: Flow<List<TimeLogWithJob>> = authState.flatMapLatest { user ->
        if (user != null) {
            val timeLogsFlow = firestore.collection("timeLogs")
                .whereEqualTo("userId", user.uid)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects<TimeLog>() }

            val jobsFlow = firestore.collection("jobs")
                .whereEqualTo("userId", user.uid)
                .snapshots()
                .map { it.toObjects<Job>().associateBy { job -> job.id } }

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

    val activeTimeLog: Flow<TimeLogWithJob?> = allTimeLogs.map { logs ->
        logs.find { it.timeLog.endTime == null }
    }

    suspend fun startNewShift(jobId: String, startLat: Double? = null, startLng: Double? = null) {
        val userId = auth.currentUser?.uid ?: return
        if (activeTimeLog.firstOrNull() == null) {
            val newLog = TimeLog(
                userId = userId,
                jobId = jobId,
                startTime = Date(),
                startLatitude = startLat,
                startLongitude = startLng
            )
            firestore.collection("timeLogs").add(newLog)
        }
    }

    suspend fun endCurrentShift(endLat: Double? = null, endLng: Double? = null) {
        activeTimeLog.firstOrNull()?.timeLog?.let { log ->
            if (log.id.isNotBlank()) {
                // End any active break
                breakRepository.endBreak()

                val now = Date()

                // Calculate total break time for this shift
                val totalBreakTime = breakRepository.breaks.firstOrNull()
                    ?.filter { it.timeLogId == log.id && it.duration != null } // Breaks for this shift
                    ?.sumOf { it.duration ?: 0L } ?: 0L

                val duration = if (log.startTime != null) {
                    val shiftDuration =
                        (now.time - log.startTime.time) / 60000 // Duration in minutes
                    shiftDuration - totalBreakTime
                } else {
                    0
                }

                val updatedLog = log.copy(
                    endTime = now,
                    duration = duration,
                    endLatitude = endLat,
                    endLongitude = endLng
                )
                firestore.collection("timeLogs").document(log.id).set(updatedLog)
            }
        }
    }

    fun deleteTimeLog(timeLog: TimeLog) {
        if (timeLog.id.isNotBlank()) {
            firestore.collection("timeLogs").document(timeLog.id).delete()
        }
    }

    // Inside your TimeLogRepository or similar class
    suspend fun deleteTimeLogsForJob(jobId: String) {
        val userId = auth.currentUser?.uid ?: return

        // Get all Time Logs for the specific job and user
        val timeLogsQuerySnapshot = firestore.collection("timeLogs")
            .whereEqualTo("userId", userId)
            .whereEqualTo("jobId", jobId)
            .get()
            .await()

        val timeLogIds = timeLogsQuerySnapshot.documents.map { it.id }

        if (timeLogIds.isEmpty()) {
            return // No time logs found, nothing to delete
        }

        // Get all associated Breaks for the time logs
        val breaksToDelete = mutableListOf<DocumentSnapshot>()
        timeLogIds.chunked(100).forEach { chunk ->
            val breaksQuerySnapshot = firestore.collection("breaks")
                .whereEqualTo("userId", userId) // Important: Filter by user as well
                .whereIn("timeLogId", chunk)
                .get()
                .await()
            breaksToDelete.addAll(breaksQuerySnapshot.documents)
        }

        //  Perform Deletions in a single Batch (up to 500 operations)
        firestore.runBatch { batch ->
            // Delete all associated Breaks first
            breaksToDelete.forEach { document ->
                batch.delete(document.reference)
            }

            // Delete the Time Logs
            timeLogsQuerySnapshot.documents.forEach { document ->
                batch.delete(document.reference)
            }
        }
    }
}