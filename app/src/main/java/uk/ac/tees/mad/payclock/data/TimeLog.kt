package uk.ac.tees.mad.payclock.data

import java.time.Duration
import java.time.Instant

/**
 * Data class representing a time log for a specific job.
 *
 * @property startTime The time when the work started.
 * @property endTime The time when the work ended. Can be null if the work is ongoing.
 * @property job The job associated with this time log.
 * @property workBreak A list of breaks taken during the work.
 * @property duration The total duration of the work. Can be null if the work is ongoing.
 */


data class TimeLog(
    var id: String?,
    var startTime: Instant,
    var endTime: Instant?,
    var job: Job,
    var workBreak: List<WorkBreak>,
    var duration: Duration?,
)