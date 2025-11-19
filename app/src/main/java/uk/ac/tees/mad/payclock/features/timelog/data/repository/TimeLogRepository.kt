package uk.ac.tees.mad.payclock.features.timelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.ac.tees.mad.payclock.features.jobs.data.Job
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLog
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLogWithJob
import java.util.Date

class TimeLogRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val userId = auth.currentUser?.uid

    val allTimeLogs: Flow<List<TimeLogWithJob>> =
        if (userId != null) {
            val timeLogsFlow = firestore.collection("timeLogs")
                .whereEqualTo("userId", userId)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects<TimeLog>() }

            val jobsFlow = firestore.collection("jobs")
                .whereEqualTo("userId", userId)
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

    val activeTimeLog: Flow<TimeLogWithJob?> = allTimeLogs.map { logs ->
        logs.find { it.timeLog.endTime == null }
    }

    suspend fun startNewShift(jobId: String) {
        val userId = auth.currentUser?.uid ?: return
        if (activeTimeLog.firstOrNull() == null) {
            val newLog = TimeLog(
                userId = userId,
                jobId = jobId,
                startTime = Date()
            )
            firestore.collection("timeLogs").add(newLog)
        }
    }

    suspend fun endCurrentShift() {
        activeTimeLog.firstOrNull()?.timeLog?.let { log ->
            if (log.id.isNotBlank()) {
                val now = Date()
                val duration = if (log.startTime != null) {
                    (now.time - log.startTime.time) / 60000 // Duration in minutes
                } else {
                    0
                }
                val updatedLog = log.copy(
                    endTime = now,
                    duration = duration
                )
                firestore.collection("timeLogs").document(log.id).set(updatedLog)
            }
        }
    }

    suspend fun deleteTimeLog(timeLog: TimeLog) {
        if (timeLog.id.isNotBlank()) {
            firestore.collection("timeLogs").document(timeLog.id).delete()
        }
    }
}