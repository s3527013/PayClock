package uk.ac.tees.mad.payclock.features.jobs.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.ac.tees.mad.payclock.features.timelog.data.repository.TimeLogRepository

class JobRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val timeLogRepository: TimeLogRepository // Added dependency
) {

    private val userId = auth.currentUser?.uid

    val jobs: Flow<List<Job>> =
        if (userId != null) {
            firestore.collection("jobs")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { it.toObjects<Job>() }
        } else {
            flowOf(emptyList())
        }

    suspend fun addJob(name: String, hourlyRate: Double, breakTimeInMinutes: Int) {
        val userId = auth.currentUser?.uid ?: return
        val newJob = Job(
            userId = userId,
            name = name,
            hourlyRate = hourlyRate,
            breakTimeInMinutes = breakTimeInMinutes
        )
        firestore.collection("jobs").add(newJob)
    }

    suspend fun updateJob(job: Job) {
        if (job.id.isNotBlank()) {
            firestore.collection("jobs").document(job.id).set(job)
        }
    }

    suspend fun removeJob(job: Job) {
        if (job.id.isNotBlank()) {
            // First, delete all time logs associated with this job
            timeLogRepository.deleteTimeLogsForJob(job.id)
            // Then, delete the job itself
            firestore.collection("jobs").document(job.id).delete()
        }
    }
}