package uk.ac.tees.mad.payclock.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.Instant

/**
 * Data class representing a time log for a specific job.
 * This is a Room entity that represents the 'time_logs' table.
 */
@Entity(
    tableName = "time_logs",
    foreignKeys = [
        ForeignKey(
            entity = Job::class,
            parentColumns = ["id"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.CASCADE // If a job is deleted, its time logs are also deleted.
        )
    ]
)
data class TimeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: Instant,
    val endTime: Instant?,
    val jobId: Int, // This now links to the Job entity's ID
    val workBreak: List<WorkBreak>,
    val duration: Duration?,
)
