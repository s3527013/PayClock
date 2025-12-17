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

/**
 * A ViewModel for the time log screen.
 *
 * @param timeLogRepository The repository for time logs.
 */
class TimeLogViewModel(
    private val timeLogRepository: TimeLogRepository
) : ViewModel() {

    /**
     * A StateFlow that emits the list of all time logs.
     */
    val allTimeLogs: StateFlow<List<TimeLogWithJob>> = timeLogRepository.allTimeLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * A StateFlow that emits the currently active time log.
     */
    val activeTimeLog: StateFlow<TimeLogWithJob?> = timeLogRepository.activeTimeLog.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Filters
    private val _selectedJobId = MutableStateFlow<String?>(null)
    /**
     * A StateFlow that emits the ID of the selected job.
     */
    val selectedJobId: StateFlow<String?> = _selectedJobId.asStateFlow()

    private val _startMillis = MutableStateFlow<Long?>(null)
    private val _endMillis = MutableStateFlow<Long?>(null)

    /**
     * A StateFlow that emits the list of filtered time logs.
     */
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

    /**
     * Selects a job to filter by.
     *
     * @param jobId The ID of the job to select.
     */
    fun selectJob(jobId: String?) {
        _selectedJobId.value = jobId
    }

    /**
     * Sets the date range to filter by.
     *
     * @param startMillis The start of the date range in milliseconds.
     * @param endMillis The end of the date range in milliseconds.
     */
    fun setDateRangeMillis(startMillis: Long?, endMillis: Long?) {
        _startMillis.value = startMillis
        _endMillis.value = endMillis
    }

    /**
     * Clears all filters.
     */
    fun clearFilters() {
        _selectedJobId.value = null
        _startMillis.value = null
        _endMillis.value = null
    }

    /**
     * Starts a new shift.
     *
     * @param jobId The ID of the job to start a shift for.
     * @param startLat The starting latitude of the shift.
     * @param startLng The starting longitude of the shift.
     * @param startAddress The starting address of the shift.
     */
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

    /**
     * Ends the current shift.
     *
     * @param endLat The ending latitude of the shift.
     * @param endLng The ending longitude of the shift.
     * @param endAddress The ending address of the shift.
     */
    fun endCurrentShift(
        endLat: Double? = null,
        endLng: Double? = null,
        endAddress: String? = null
    ) {
        viewModelScope.launch {
            timeLogRepository.endCurrentShift(endLat, endLng, endAddress)
        }
    }

    /**
     * Deletes a time log.
     *
     * @param timeLog The time log to delete.
     */
    fun deleteTimeLog(timeLog: TimeLog) {
        viewModelScope.launch {
            timeLogRepository.deleteTimeLog(timeLog)
        }
    }
}