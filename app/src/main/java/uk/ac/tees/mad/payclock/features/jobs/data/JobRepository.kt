package uk.ac.tees.mad.payclock.features.jobs.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class JobRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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

    fun addJob(name: String, rate: Double, breakTime: Int) {
        val userId = auth.currentUser?.uid ?: return
        val newJob = Job(
            name = name,
            hourlyRate = rate,
            breakTimeInMinutes = breakTime,
            userId = userId
        )
        firestore.collection("jobs").add(newJob)
    }

    suspend fun getJob(jobId: String): Job? {
        if (jobId.isBlank()) return null
        return try {
            firestore.collection("jobs").document(jobId).get().await().toObject(Job::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun updateJob(job: Job) {
        if (job.id.isNotBlank()) {
            firestore.collection("jobs").document(job.id).set(job)
        }
    }

    fun removeJob(job: Job) {
        if (job.id.isNotBlank()) {
            firestore.collection("jobs").document(job.id).delete()
        }
    }

}