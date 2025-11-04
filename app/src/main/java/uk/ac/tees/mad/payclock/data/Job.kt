package uk.ac.tees.mad.payclock.data


/**
 * Represents a job with its specific details, updated for synchronization.
 *
 * @property id The unique identifier for the job. Use a String/UUID for client-created jobs
 * to avoid conflicts with server-assigned Int IDs. We'll stick to Int for now,
 * but future-proofing suggests a change to String.
 * @property name The name or title of the job.
 * @property hourlyRate The rate of pay per hour for this job.
 * @property breakTimeInMinutes The standard unpaid break time for a shift, in minutes.
 * @property lastModifiedTimestamp The timestamp of the last local update (used for conflict resolution).
 * @property isPendingSync Flag indicating if this job needs to be pushed to the server.
 */
data class Job(
    val id: Int, // Ideally this should be a String/UUID for client-created jobs
    val name: String,
    val hourlyRate: Double,
    val breakTimeInMinutes: Int,

    // --- New Sync Fields ---
    val lastModifiedTimestamp: Long = System.currentTimeMillis(),
    val isPendingSync: Boolean = false // True if a local change hasn't been pushed
)
