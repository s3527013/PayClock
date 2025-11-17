package uk.ac.tees.mad.payclock.features.timelog.data

/**
 * A data class that combines a [TimeLog] with the name of its associated [uk.ac.tees.mad.payclock.features.jobs.data.Job].
 * This is used to display the job name in the UI without needing a separate query.
 */
data class TimeLogWithJob(
    val timeLog: TimeLog,
    val jobName: String?
)
