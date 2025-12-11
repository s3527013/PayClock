package uk.ac.tees.mad.payclock.features.jobs.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Represents a job with its specific details.
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