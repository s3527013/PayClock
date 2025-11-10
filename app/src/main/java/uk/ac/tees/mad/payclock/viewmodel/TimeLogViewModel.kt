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
import uk.ac.tees.mad.payclock.data.repository.TimeLogRepository
import java.time.Duration
import java.time.Instant

class TimeLogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimeLogRepository

    val allTimeLogs: StateFlow<List<TimeLogWithJob>>
    val activeTimeLog: StateFlow<TimeLogWithJob?>

    init {
        val timeLogDao = PayClockDatabase.getDatabase(application).timeLogDao()
        repository = TimeLogRepository(timeLogDao)

        allTimeLogs = repository.allTimeLogsWithJob.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeTimeLog = repository.activeTimeLogWithJob.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubributed(5000),
            initialValue = null
        )
    }

    fun startNewShift(jobId: Int) {
        viewModelScope.launch {
            if (activeTimeLog.value == null) {
                val newLog = TimeLog(
                    startTime = Instant.now(),
                    endTime = null,
                    jobId = jobId,
                    workBreak = emptyList(),
                    duration = null
                )
                repository.insert(newLog)
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
                repository.update(updatedLog)
            }
        }
    }

    fun deleteTimeLog(timeLog: TimeLog) {
        viewModelScope.launch {
            repository.delete(timeLog)
        }
    }
}
