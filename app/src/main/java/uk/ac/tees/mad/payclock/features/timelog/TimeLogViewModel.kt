package uk.ac.tees.mad.payclock.features.timelog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    // Filters
    private val _selectedJobId = MutableStateFlow<String?>(null)
    val selectedJobId: StateFlow<String?> = _selectedJobId.asStateFlow()

    private val _startMillis = MutableStateFlow<Long?>(null)
    private val _endMillis = MutableStateFlow<Long?>(null)

    // Exposed filtered list
    val filteredTimeLogs: StateFlow<List<TimeLogWithJob>> =
        combine(allTimeLogs, _selectedJobId, _startMillis, _endMillis) { logs, jobId, start, end ->
            logs.filter { tlw ->
                val s = tlw.timeLog.startTime?.time ?: return@filter false
                val byJob = jobId?.let { tlw.timeLog.jobId == it } ?: true
                val afterStart = start?.let { s >= it } ?: true
                val beforeEnd = end?.let { s <= it } ?: true
                byJob && afterStart && beforeEnd
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectJob(jobId: String?) {
        _selectedJobId.value = jobId
    }

    fun setDateRangeMillis(startMillis: Long?, endMillis: Long?) {
        _startMillis.value = startMillis
        _endMillis.value = endMillis
    }

    fun clearFilters() {
        _selectedJobId.value = null
        _startMillis.value = null
        _endMillis.value = null
    }

    fun startNewShift(
        jobId: String,
        startLat: Double? = null,
        startLng: Double? = null,
        startAddress: String? = null
    ) {
        viewModelScope.launch {
            timeLogRepository.startNewShift(jobId, startLat, startLng, startAddress)
        }
    }

    fun endCurrentShift(
        endLat: Double? = null,
        endLng: Double? = null,
        endAddress: String? = null
    ) {
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