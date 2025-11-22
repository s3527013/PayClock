package uk.ac.tees.mad.payclock.features.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import uk.ac.tees.mad.payclock.features.jobs.data.JobRepository
import uk.ac.tees.mad.payclock.features.timelog.data.repository.TimeLogRepository
import java.time.LocalDate
import java.time.ZoneId

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

class ReportViewModel(
    private val jobRepository: JobRepository,
    private val timeLogRepository: TimeLogRepository
) : ViewModel() {

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
        combine(
            jobRepository.jobs,
            timeLogRepository.allTimeLogs
        ) { jobs, timeLogs ->
            val completedLogs = timeLogs.filter { it.timeLog.endTime != null }
            val jobRates = jobs.associate { it.id to it.hourlyRate }

            completedLogs
                .groupBy { it.timeLog.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() }
                .map { (date, logsOnDate) ->
                    val totalHours = logsOnDate.sumOf { (it.timeLog.duration ?: 0L) } / 60.0
                    val totalEarnings = logsOnDate.sumOf {
                        val rate = jobRates[it.timeLog.jobId] ?: 0.0
                        val hours = (it.timeLog.duration ?: 0L) / 60.0
                        hours * rate
                    }
                    DailyReport(
                        date = date,
                        totalHours = totalHours,
                        totalEarnings = totalEarnings
                    )
                }
                .sortedByDescending { it.date }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
