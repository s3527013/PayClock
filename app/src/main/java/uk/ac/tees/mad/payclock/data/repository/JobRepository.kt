package uk.ac.tees.mad.payclock.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.ac.tees.mad.payclock.data.models.Job

class JobRepository {

    private val firestore = Firebase.firestore

    fun getJobs(userId: String): Flow<List<Job>> {
        return firestore.collection("jobs")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    document.toObject<Job>()?.copy(id = document.id)
                }
            }
    }

    suspend fun addJob(job: Job) {
        firestore.collection("jobs").add(job)
    }

    suspend fun updateJob(job: Job) {
        firestore.collection("jobs").document(job.id).set(job)
    }

    suspend fun removeJob(job: Job) {
        if (job.id.isNotBlank()) {
            firestore.collection("jobs").document(job.id).delete()
        }
    }
}
