package uk.ac.tees.mad.payclock.features.jobs.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Represents a job with its specific details.
 *
 * @param id The ID of the job.
 * @param userId The ID of the user who owns the job.
 * @param name The name of the job.
 * @param hourlyRate The hourly rate of the job.
 * @param breakTimeInMinutes The break time in minutes for the job.
 * @param lastUpdated The date and time the job was last updated.
 */
@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey
    @DocumentId
    val id: String = "", // Firestore document ID
    val userId: String = "", // Foreign key to the Firebase User UID
    val name: String = "",
    val hourlyRate: Double = 0.0,
    val breakTimeInMinutes: Int = 0,
    var lastUpdated: Date? = null
)