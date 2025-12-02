package uk.ac.tees.mad.payclock.features.jobs.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.payclock.features.timelog.data.repository.TimeLogRepository

class JobRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val timeLogRepository: TimeLogRepository
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

    suspend fun addJob(name: String, hourlyRate: Double) {
        val userId = auth.currentUser?.uid ?: return
        val newJob = Job(
            userId = userId,
            name = name,
            hourlyRate = hourlyRate
        )
        firestore.collection("jobs").add(newJob).await()
    }

    suspend fun updateJob(job: Job) {
        if (job.id.isNotBlank()) {
            firestore.collection("jobs").document(job.id).set(job).await()
        }
    }

    suspend fun removeJob(job: Job) {
        if (job.id.isNotBlank()) {
            timeLogRepository.deleteTimeLogsForJob(job.id)
            firestore.collection("jobs").document(job.id).delete().await()
        }
    }
}

