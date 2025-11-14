package uk.ac.tees.mad.payclock.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a time log for a specific job.
 */
data class TimeLog(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val jobId: String = "",
    @ServerTimestamp
    val startTime: Date? = null,
    var endTime: Date? = null,
    var duration: Long? = null, // Duration in minutes
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", null, null, null)
}
