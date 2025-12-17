package uk.ac.tees.mad.payclock.features.timelog.data

import uk.ac.tees.mad.payclock.features.jobs.data.Job

/**
 * A data class that combines a [TimeLog] with the name of its associated [Job].
 * This is used to display the job name in the UI without needing a separate query.
 *
 * @property timeLog The [TimeLog] entity.
 * @property jobName The name of the job associated with the time log. Can be null if the job is not found.
 */
data class TimeLogWithJob(
    val timeLog: TimeLog,
    val jobName: String?
)
