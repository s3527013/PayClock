package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.models.Job
import kotlinx.coroutines.flow.map

class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = Firebase.firestore
    private val userId = Firebase.auth.currentUser?.uid

    val jobs: StateFlow<List<Job>> = firestore.collection("jobs")
        .whereEqualTo("userId", userId)
        .snapshots()
        .map { snapshot ->
            snapshot.toObjects<Job>()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addJob(name: String, hourlyRate: Double, breakTimeInMinutes: Int) {
        if (userId == null) return
        viewModelScope.launch {
            val newJob = Job(
                userId = userId,
                name = name,
                hourlyRate = hourlyRate,
                breakTimeInMinutes = breakTimeInMinutes
            )
            firestore.collection("jobs").add(newJob)
        }
    }

    fun removeJob(job: Job) {
        if (job.id.isNotBlank()) {
            firestore.collection("jobs").document(job.id).delete()
        }
    }
}
