package uk.ac.tees.mad.payclock.features.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.features.jobs.data.Job
import uk.ac.tees.mad.payclock.features.jobs.data.JobRepository

/**
 * A ViewModel for the job screen.
 *
 * @param jobRepository The repository for jobs.
 */
class JobViewModel(
    private val jobRepository: JobRepository
) : ViewModel() {

    /**
     * A StateFlow that emits the list of all jobs.
     */
    val jobs: StateFlow<List<Job>> = jobRepository.jobs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    private val _activeJob = MutableStateFlow<Job?>(null)
    /**
     * A StateFlow that emits the currently active job.
     */
    val activeJob: StateFlow<Job?> = _activeJob

    /**
     * Adds a new job.
     *
     * @param name The name of the job.
     * @param hourlyRate The hourly rate of the job.
     */
    fun addJob(name: String, hourlyRate: Double) {
        viewModelScope.launch {
            jobRepository.addJob(name, hourlyRate)
        }
    }

    /**
     * Updates a job.
     *
     * @param job The job to update.
     */
    fun updateJob(job: Job) {
        viewModelScope.launch {
            jobRepository.updateJob(job)
        }
    }

    /**
     * Removes a job.
     *
     * @param job The job to remove.
     */
    fun removeJob(job: Job) {
        viewModelScope.launch {
            jobRepository.removeJob(job)
        }
    }

    /**
     * Sets the active job.
     *
     * @param job The job to set as active.
     */
    fun setActiveJob(job: Job) {
        _activeJob.value = job
    }

    /**
     * Resets the active job.
     */
    fun resetActiveJob() {
        _activeJob.value = null
    }
}
