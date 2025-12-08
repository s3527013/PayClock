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

    fun startNewShift(jobId: String, startLat: Double? = null, startLng: Double? = null, startAddress: String? = null) {
        viewModelScope.launch {
            timeLogRepository.startNewShift(jobId, startLat, startLng, startAddress)
        }
    }

    fun endCurrentShift(endLat: Double? = null, endLng: Double? = null, endAddress: String? = null) {
        viewModelScope.launch {
            timeLogRepository.endCurrentShift(endLat, endLng, endAddress)
        }
    }

    fun deleteTimeLog(timeLog: TimeLog) {
        viewModelScope.launch {
            timeLogRepository.deleteTimeLog(timeLog)
        }
    }
}