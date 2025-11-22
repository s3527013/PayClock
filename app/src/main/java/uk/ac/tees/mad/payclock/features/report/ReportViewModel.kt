package uk.ac.tees.mad.payclock.features.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uk.ac.tees.mad.payclock.features.jobs.data.JobRepository
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLog
import uk.ac.tees.mad.payclock.features.timelog.data.repository.TimeLogRepository
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.IsoFields
import java.time.temporal.WeekFields
import java.util.Locale

// Data classes for different report types
data class JobReport(
    val jobName: String,
    val totalHours: Double,
    val totalEarnings: Double
)

data class DailyReport(
    val date: LocalDate,
    val totalHours: Double,
    val totalEarnings: Double
)

data class WeeklyReport(
    val year: Int,
    val week: Int,
    val totalHours: Double,
    val totalEarnings: Double
) {
    val weekLabel: String
        get() = "Week $week, $year"
}

data class MonthlyReport(
    val year: Int,
    val month: Int,
    val totalHours: Double,
    val totalEarnings: Double
) {
    val monthLabel: String
        get() = java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault()) + " $year"
}

data class QuarterlyReport(
    val year: Int,
    val quarter: Int,
    val totalHours: Double,
    val totalEarnings: Double
) {
    val quarterLabel: String
        get() = "Q$quarter $year"
}

data class YearlyReport(
    val year: Int,
    val totalHours: Double,
    val totalEarnings: Double
)

class ReportViewModel(
    jobRepository: JobRepository,
    timeLogRepository: TimeLogRepository
) : ViewModel() {

    // Private flow to combine and pre-process data, reducing repetition.
    private val completedLogsWithRates: Flow<List<Triple<TimeLog, Double, Double>>> =
        combine(
            jobRepository.jobs,
            timeLogRepository.allTimeLogs
        ) { jobs, timeLogs ->
            val jobRates = jobs.associate { it.id to it.hourlyRate }
            timeLogs
                .filter { it.timeLog.endTime != null }
                .map { logWithJob ->
                    val log = logWithJob.timeLog
                    val rate = jobRates[log.jobId] ?: 0.0
                    val hours = (log.duration ?: 0L) / 60.0
                    val earnings = hours * rate
                    Triple(log, hours, earnings)
                }
        }

    val jobReports: StateFlow<List<JobReport>> =
        combine(
            jobRepository.jobs,
            timeLogRepository.allTimeLogs
        ) { jobs, timeLogs ->
            jobs.map { job ->
                val logsForJob = timeLogs.filter { it.timeLog.jobId == job.id && it.timeLog.endTime != null }
                val totalMinutes = logsForJob.sumOf { it.timeLog.duration ?: 0L }
                val totalHours = totalMinutes / 60.0
                val totalEarnings = totalHours * job.hourlyRate
                JobReport(
                    jobName = job.name,
                    totalHours = totalHours,
                    totalEarnings = totalEarnings
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dailyReports: StateFlow<List<DailyReport>> =
        completedLogsWithRates.map { logs ->
            logs.groupBy { it.first.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() }
                .map { (date, logsOnDate) ->
                    DailyReport(
                        date = date,
                        totalHours = logsOnDate.sumOf { it.second },
                        totalEarnings = logsOnDate.sumOf { it.third }
                    )
                }
                .sortedByDescending { it.date }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weeklyReports: StateFlow<List<WeeklyReport>> =
        completedLogsWithRates.map { logs ->
            val weekFields = WeekFields.of(Locale.getDefault())
            logs.groupBy {
                val date = it.first.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                date.year to date.get(weekFields.weekOfWeekBasedYear())
            }
                .map { (yearWeek, logsInWeek) ->
                    WeeklyReport(
                        year = yearWeek.first,
                        week = yearWeek.second,
                        totalHours = logsInWeek.sumOf { it.second },
                        totalEarnings = logsInWeek.sumOf { it.third }
                    )
                }
                .sortedWith(compareByDescending<WeeklyReport> { it.year }.thenByDescending { it.week })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val monthlyReports: StateFlow<List<MonthlyReport>> =
        completedLogsWithRates.map { logs ->
            logs.groupBy {
                val date = it.first.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                date.year to date.monthValue
            }
                .map { (yearMonth, logsInMonth) ->
                    MonthlyReport(
                        year = yearMonth.first,
                        month = yearMonth.second,
                        totalHours = logsInMonth.sumOf { it.second },
                        totalEarnings = logsInMonth.sumOf { it.third }
                    )
                }
                .sortedWith(compareByDescending<MonthlyReport> { it.year }.thenByDescending { it.month })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val quarterlyReports: StateFlow<List<QuarterlyReport>> =
        completedLogsWithRates.map { logs ->
            logs.groupBy {
                val date = it.first.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                date.year to date.get(IsoFields.QUARTER_OF_YEAR)
            }
                .map { (yearQuarter, logsInQuarter) ->
                    QuarterlyReport(
                        year = yearQuarter.first,
                        quarter = yearQuarter.second,
                        totalHours = logsInQuarter.sumOf { it.second },
                        totalEarnings = logsInQuarter.sumOf { it.third }
                    )
                }
                .sortedWith(compareByDescending<QuarterlyReport> { it.year }.thenByDescending { it.quarter })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val yearlyReports: StateFlow<List<YearlyReport>> =
        completedLogsWithRates.map { logs ->
            logs.groupBy { it.first.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().year }
                .map { (year, logsInYear) ->
                    YearlyReport(
                        year = year,
                        totalHours = logsInYear.sumOf { it.second },
                        totalEarnings = logsInYear.sumOf { it.third }
                    )
                }
                .sortedByDescending { it.year }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
