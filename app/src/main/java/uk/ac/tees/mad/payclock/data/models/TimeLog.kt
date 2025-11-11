package uk.ac.tees.mad.payclock.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Represents a time log for a specific job.
 * Now includes a userId to link it to a Firebase user.
 */
@Entity(
    tableName = "time_logs",
    foreignKeys = [
        ForeignKey(
            entity = Job::class,
            parentColumns = ["id"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index("jobId")]
)
data class TimeLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // Use a string UUID for Firebase compatibility
    val userId: String, // Foreign key to the Firebase User UID
    val startTime: Instant,
    val endTime: Instant?,
    val jobId: String, // Links to the Job entity's string ID
    val workBreak: List<WorkBreak>,
    val duration: Duration?,
)
