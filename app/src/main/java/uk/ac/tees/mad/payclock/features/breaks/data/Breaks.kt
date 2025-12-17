package uk.ac.tees.mad.payclock.features.breaks.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a break with its specific details.
 *
 * @param id The ID of the break.
 * @param timeLogId The ID of the time log the break is associated with.
 * @param userId The ID of the user who took the break.
 * @param startTime The start time of the break.
 * @param endTime The end time of the break.
 * @param duration The duration of the break in minutes.
 * @param lastUpdated The date and time the break was last updated.
 */
@Entity(tableName = "breaks")
data class Breaks(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val timeLogId: String = "",
    val userId: String = "",
    @ServerTimestamp
    val startTime: Date? = null,
    var endTime: Date? = null,
    var duration: Long? = null, // Duration in minutes
    var lastUpdated: Date? = null
)