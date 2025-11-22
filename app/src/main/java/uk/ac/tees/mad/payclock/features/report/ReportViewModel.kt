package uk.ac.tees.mad.payclock.features.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import uk.ac.tees.mad.payclock.features.jobs.data.Job
import uk.ac.tees.mad.payclock.features.jobs.data.JobRepository
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLog
import uk.ac.tees.mad.payclock.features.timelog.data.repository.TimeLogRepository
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.IsoFields
import java.time.temporal.WeekFields
import java.util.Locale

// --- Data Classes ---

data class JobReport(
    val jobName: String,
    val totalHours: Double,
    val totalEarnings: Double
)

data class TimeSeriesReportItem(
    val label: String,
    val totalHours: Double,
    val totalEarnings: Double,
)

enum class ReportType {
    Daily, Weekly, Monthly, Quarterly, Yearly
}


// --- ViewModel ---

class ReportViewModel(
    jobRepository: JobRepository,
    timeLogRepository: TimeLogRepository
) : ViewModel() {

    // --- State & Filters ---

    val jobs: StateFlow<List<Job>> = jobRepository.jobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedJobId = MutableStateFlow<String?>(null)
    val selectedJobId: StateFlow<String?> = _selectedJobId.asStateFlow()

    private val _selectedReportType = MutableStateFlow(ReportType.Daily)
    val selectedReportType: StateFlow<ReportType> = _selectedReportType.asStateFlow()

    fun selectJob(jobId: String?) {
        _selectedJobId.value = jobId
    }

    fun selectReportType(reportType: ReportType) {
        _selectedReportType.value = reportType
    }


    // --- Data Processing Flows ---

    private val completedLogsWithRates: Flow<List<Triple<TimeLog, Double, Double>>> =
        combine(jobs, timeLogRepository.allTimeLogs) { jobs, timeLogs ->
            val jobRates = jobs.associate { it.id to it.hourlyRate }
            timeLogs
                .filter { it.timeLog.endTime != null }
                .mapNotNull { logWithJob ->
                    val log = logWithJob.timeLog
                    val rate = jobRates[log.jobId] ?: return@mapNotNull null
                    val hours = (log.duration ?: 0L) / 60.0
                    val earnings = hours * rate
                    Triple(log, hours, earnings)
                }
        }

    private val filteredLogs: Flow<List<Triple<TimeLog, Double, Double>>> =
        combine(_selectedJobId, completedLogsWithRates) { jobId, logs ->
            if (jobId == null) {
                logs
            } else {
                logs.filter { it.first.jobId == jobId }
            }
        }


    // --- Report Flows ---

    val jobReports: StateFlow<List<JobReport>> =
        combine(jobs, timeLogRepository.allTimeLogs) { jobs, timeLogs ->
            jobs.map { job ->
                val logsForJob =
                    timeLogs.filter { it.timeLog.jobId == job.id && it.timeLog.endTime != null }
                val totalMinutes = logsForJob.sumOf { it.timeLog.duration ?: 0L }
                val totalHours = totalMinutes / 60.0
                val totalEarnings = totalHours * job.hourlyRate
                JobReport(
                    jobName = job.name,
                    totalHours = totalHours,
                    totalEarnings = totalEarnings
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val timeSeriesReport: StateFlow<List<TimeSeriesReportItem>> =
        combine(filteredLogs, _selectedReportType) { logs, type ->
            when (type) {
                ReportType.Daily -> groupDaily(logs)
                ReportType.Weekly -> groupWeekly(logs)
                ReportType.Monthly -> groupMonthly(logs)
                ReportType.Quarterly -> groupQuarterly(logs)
                ReportType.Yearly -> groupYearly(logs)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Grouping Functions ---

    private fun groupDaily(logs: List<Triple<TimeLog, Double, Double>>): List<TimeSeriesReportItem> {
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        return logs
            .groupBy {
                it.first.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            }
            .entries
            .sortedByDescending { it.key }
            .map { (date, logsOnDate) ->
                TimeSeriesReportItem(
                    label = date.format(formatter),
                    totalHours = logsOnDate.sumOf { it.second },
                    totalEarnings = logsOnDate.sumOf { it.third }
                )
            }
    }


    private fun groupWeekly(logs: List<Triple<TimeLog, Double, Double>>): List<TimeSeriesReportItem> {
        val weekFields = WeekFields.of(Locale.getDefault())
        return logs
            .groupBy {
                val date =
                    it.first.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                date.year to date.get(weekFields.weekOfWeekBasedYear())
            }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<Pair<Int, Int>, List<Triple<TimeLog, Double, Double>>>> { it.key.first }
                    .thenByDescending { it.key.second }
            )
            .map { (yearWeek, logsInWeek) ->
                TimeSeriesReportItem(
                    label = "Week ${yearWeek.second}, ${yearWeek.first}",
                    totalHours = logsInWeek.sumOf { it.second },
                    totalEarnings = logsInWeek.sumOf { it.third }
                )
            }
    }

    private fun groupMonthly(logs: List<Triple<TimeLog, Double, Double>>): List<TimeSeriesReportItem> {
        return logs
            .groupBy {
                val date =
                    it.first.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                date.year to date.monthValue
            }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<Pair<Int, Int>, List<Triple<TimeLog, Double, Double>>>> { it.key.first }
                    .thenByDescending { it.key.second }
            )
            .map { (yearMonth, logsInMonth) ->
                val monthName = java.time.Month.of(yearMonth.second)
                    .getDisplayName(TextStyle.FULL, Locale.getDefault())
                TimeSeriesReportItem(
                    label = "$monthName ${yearMonth.first}",
                    totalHours = logsInMonth.sumOf { it.second },
                    totalEarnings = logsInMonth.sumOf { it.third }
                )
            }
    }

    private fun groupQuarterly(logs: List<Triple<TimeLog, Double, Double>>): List<TimeSeriesReportItem> {
        return logs
            .groupBy {
                val date =
                    it.first.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                date.year to date.get(IsoFields.QUARTER_OF_YEAR)
            }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<Pair<Int, Int>, List<Triple<TimeLog, Double, Double>>>> { it.key.first }
                    .thenByDescending { it.key.second }
            )
            .map { (yearQuarter, logsInQuarter) ->
                TimeSeriesReportItem(
                    label = "Q${yearQuarter.second} ${yearQuarter.first}",
                    totalHours = logsInQuarter.sumOf { it.second },
                    totalEarnings = logsInQuarter.sumOf { it.third }
                )
            }
    }

    private fun groupYearly(logs: List<Triple<TimeLog, Double, Double>>): List<TimeSeriesReportItem> {
        return logs
            .groupBy {
                it.first.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().year
            }
            .entries
            .sortedByDescending { it.key }
            .map { (year, logsInYear) ->
                TimeSeriesReportItem(
                    label = year.toString(),
                    totalHours = logsInYear.sumOf { it.second },
                    totalEarnings = logsInYear.sumOf { it.third }
                )
            }
    }
}
