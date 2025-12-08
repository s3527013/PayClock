package uk.ac.tees.mad.payclock.features.timelog.data

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
    // Added nullable location fields for start and end
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    var endLatitude: Double? = null,
    var endLongitude: Double? = null,
    // New address fields
    val startAddress: String? = null,
    var endAddress: String? = null,
)
