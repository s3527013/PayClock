package uk.ac.tees.mad.payclock.data.models

import androidx.room.Embedded

/**
 * A data class that combines a [TimeLog] with the name of its associated [Job].
 * This is used to display the job name in the UI without needing a separate query.
 * Room populates this object from a JOIN query.
 */
data class TimeLogWithJob(
    // Tells Room to treat all fields of the TimeLog class as if they were fields of this class.
    @Embedded
    val timeLog: TimeLog,

    // The name of the job, fetched from the 'jobs' table.
    val jobName: String?
)
