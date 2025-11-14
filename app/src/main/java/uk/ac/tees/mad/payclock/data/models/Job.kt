package uk.ac.tees.mad.payclock.data.models

import com.google.firebase.firestore.DocumentId

/**
 * Represents a job with its specific details.
 */
data class Job(
    @DocumentId
    val id: String = "", // Firestore document ID
    val userId: String = "", // Foreign key to the Firebase User UID
    val name: String = "",
    val hourlyRate: Double = 0.0,
    val breakTimeInMinutes: Int = 0
)
