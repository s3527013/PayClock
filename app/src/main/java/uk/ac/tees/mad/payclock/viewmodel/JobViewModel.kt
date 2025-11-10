package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.db.PayClockDatabase
import uk.ac.tees.mad.payclock.data.models.Job
import uk.ac.tees.mad.payclock.data.repository.JobRepository

/**
 * ViewModel for managing the list of jobs, now backed by a Room database.
 */
class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JobRepository

    // Using StateFlow to expose the list of jobs from the database.
    // The UI will automatically update when the data changes.
    val jobs: StateFlow<List<Job>>

    init {
        // Initialize the database and repository.
        val jobDao = PayClockDatabase.getDatabase(application).jobDao()
        repository = JobRepository(jobDao)
        
        // Convert the Flow from the repository into a StateFlow for the UI.
        jobs = repository.allJobs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    /**
     * Launches a coroutine to insert a new job into the database.
     * @param job The new job to add.
     */
    fun addJob(job: Job) {
        viewModelScope.launch {
            repository.insert(job)
        }
    }

    /**
     * Launches a coroutine to remove a job from the database.
     * @param job The job to remove.
     */
    fun removeJob(job: Job) {
        viewModelScope.launch {
            repository.delete(job)
        }
    }
}
