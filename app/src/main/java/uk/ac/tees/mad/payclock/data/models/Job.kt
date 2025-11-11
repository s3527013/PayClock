package uk.ac.tees.mad.payclock.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a job with its specific details.
 * Now includes a userId to link it to a Firebase user.
 */
@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // Use a string UUID for Firebase compatibility
    val userId: String, // Foreign key to the Firebase User UID
    val name: String,
    val hourlyRate: Double,
    val breakTimeInMinutes: Int,
    val lastModifiedTimestamp: Long = System.currentTimeMillis(),
    val isPendingSync: Boolean = true // True for new or modified jobs
)
