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

class JobViewModel(
    private val jobRepository: JobRepository
) : ViewModel() {

    val jobs: StateFlow<List<Job>> = jobRepository.jobs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    private val _activeJob = MutableStateFlow<Job?>(null)
    val activeJob: StateFlow<Job?> = _activeJob

    fun addJob(name: String, hourlyRate: Double, breakTimeInMinutes: Int) {
        viewModelScope.launch {
            jobRepository.addJob(name, hourlyRate, breakTimeInMinutes)
        }
    }

    fun updateJob(job: Job) {
        viewModelScope.launch {
            jobRepository.updateJob(job)
        }
    }

    fun removeJob(job: Job) {
        viewModelScope.launch {
            jobRepository.removeJob(job)
        }
    }

    fun setActiveJob(job: Job) {
        _activeJob.value = job
    }

    fun resetActiveJob() {
        _activeJob.value = null
    }
}
