package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.db.PayClockDatabase
import uk.ac.tees.mad.payclock.data.models.TimeLog
import uk.ac.tees.mad.payclock.data.models.TimeLogWithJob
import uk.ac.tees.mad.payclock.data.repository.AuthRepository
import uk.ac.tees.mad.payclock.data.repository.TimeLogRepository
import java.time.Duration
import java.time.Instant

class TimeLogViewModel(application: Application) : AndroidViewModel(application) {

    private val timeLogRepository: TimeLogRepository
    private val authRepository = AuthRepository() // To get the current user

    val allTimeLogs: StateFlow<List<TimeLogWithJob>>
    val activeTimeLog: StateFlow<TimeLogWithJob?>

    init {
        val timeLogDao = PayClockDatabase.getDatabase(application).timeLogDao()
        timeLogRepository = TimeLogRepository(timeLogDao)

        allTimeLogs = timeLogRepository.allTimeLogsWithJob.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeTimeLog = timeLogRepository.activeTimeLogWithJob.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun startNewShift(jobId: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            // Should not happen if this screen is protected by auth
            return
        }

        viewModelScope.launch {
            if (activeTimeLog.value == null) {
                val newLog = TimeLog(
                    userId = userId,
                    startTime = Instant.now(),
                    endTime = null,
                    jobId = jobId,
                    workBreak = emptyList(),
                    duration = null
                )
                timeLogRepository.insert(newLog)
            }
        }
    }

    fun endCurrentShift() {
        viewModelScope.launch {
            activeTimeLog.value?.timeLog?.let { log ->
                val now = Instant.now()
                val duration = Duration.between(log.startTime, now)
                val updatedLog = log.copy(
                    endTime = now,
                    duration = duration
                )
                timeLogRepository.update(updatedLog)
            }
        }
    }

    fun deleteTimeLog(timeLog: TimeLog) {
        viewModelScope.launch {
            timeLogRepository.delete(timeLog)
        }
    }
}
