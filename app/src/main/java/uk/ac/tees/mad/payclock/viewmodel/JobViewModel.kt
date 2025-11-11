package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.db.PayClockDatabase
import uk.ac.tees.mad.payclock.data.models.Job
import uk.ac.tees.mad.payclock.data.repository.JobRepository

class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JobRepository
    val jobs: StateFlow<List<Job>>

    init {
        val jobDao = PayClockDatabase.getDatabase(application).jobDao()
        repository = JobRepository(jobDao)
        jobs = repository.allJobs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    /**
     * Creates a new Job, automatically associating it with the current user.
     */
    fun addJob(name: String, hourlyRate: Double, breakTimeInMinutes: Int) {
        // Get the current user's ID from Firebase Auth.
        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {
            // Handle the case where the user is not logged in, though this shouldn't happen
            // if the screen is protected by the auth flow.
            return
        }
        viewModelScope.launch {
            val newJob = Job(
                userId = userId,
                name = name,
                hourlyRate = hourlyRate,
                breakTimeInMinutes = breakTimeInMinutes,
                isPendingSync = true // Mark for upload
            )
            repository.insert(newJob)
        }
    }

    fun removeJob(job: Job) {
        viewModelScope.launch {
            repository.delete(job)
        }
    }
}
