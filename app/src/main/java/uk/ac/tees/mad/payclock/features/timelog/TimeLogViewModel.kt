package uk.ac.tees.mad.payclock.features.timelog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLog
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLogWithJob
import uk.ac.tees.mad.payclock.features.timelog.data.repository.TimeLogRepository

class TimeLogViewModel(
    private val timeLogRepository: TimeLogRepository
) : ViewModel() {

    val allTimeLogs: StateFlow<List<TimeLogWithJob>> = timeLogRepository.allTimeLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeTimeLog: StateFlow<TimeLogWithJob?> = timeLogRepository.activeTimeLog.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun startNewShift(jobId: String, startLat: Double? = null, startLng: Double? = null) {
        viewModelScope.launch {
            timeLogRepository.startNewShift(jobId, startLat, startLng)
        }
    }

    fun endCurrentShift(endLat: Double? = null, endLng: Double? = null) {
        viewModelScope.launch {
            timeLogRepository.endCurrentShift(endLat, endLng)
        }
    }

    fun deleteTimeLog(timeLog: TimeLog) {
        viewModelScope.launch {
            timeLogRepository.deleteTimeLog(timeLog)
        }
    }
}