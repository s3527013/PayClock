package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.models.Job
import uk.ac.tees.mad.payclock.data.models.TimeLog
import uk.ac.tees.mad.payclock.data.models.TimeLogWithJob
import java.util.Date

class TimeLogViewModel: ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val userId = auth.currentUser?.uid

    val allTimeLogs: StateFlow<List<TimeLogWithJob>>
    val activeTimeLog: StateFlow<TimeLogWithJob?>

    init {
        if (userId != null) {
            val timeLogsFlow = firestore.collection("time_logs")
                .whereEqualTo("userId", userId)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects<TimeLog>() }

            val jobsFlow = firestore.collection("jobs")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { it.toObjects<Job>().associateBy { job -> job.id } }

            allTimeLogs = timeLogsFlow.combine(jobsFlow) { timeLogs, jobsMap ->
                timeLogs.map { timeLog ->
                    TimeLogWithJob(
                        timeLog = timeLog,
                        jobName = jobsMap[timeLog.jobId]?.name ?: "Unknown Job"
                    )
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

            activeTimeLog = allTimeLogs.map { logs ->
                logs.find { it.timeLog.endTime == null }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        } else {
            allTimeLogs = flowOf<List<TimeLogWithJob>>(emptyList()).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
            activeTimeLog =
                flowOf<TimeLogWithJob?>(null).stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    null
                )
        }
    }

    fun startNewShift(jobId: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            if (activeTimeLog.value == null) {
                val newLog = TimeLog(
                    userId = userId,
                    jobId = jobId,
                    startTime = Date()
                )
                firestore.collection("time_logs").add(newLog)
            }
        }
    }

    fun endCurrentShift() {
        viewModelScope.launch {
            activeTimeLog.value?.timeLog?.let { log ->
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
                    firestore.collection("time_logs").document(log.id).set(updatedLog)
                }
            }
        }
    }

    fun deleteTimeLog(timeLog: TimeLog) {
        viewModelScope.launch {
            if (timeLog.id.isNotBlank()) {
                firestore.collection("time_logs").document(timeLog.id).delete()
            }
        }
    }
}
