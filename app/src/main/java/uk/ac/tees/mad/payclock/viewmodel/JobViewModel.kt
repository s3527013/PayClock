package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.Job
import uk.ac.tees.mad.payclock.data.repository.JobRepository
import uk.ac.tees.mad.payclock.data.repository.JobSyncManager

/**
 * ViewModel for managing a list of job profiles with persistence and API synchronization.
 *
 * This ViewModel holds the state for a list of [Job] objects, loads them
 * from a [JobRepository], saves them back, and includes logic to sync with a network API.
 */

class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JobRepository(application)

    // Initialize the Sync Manager
    private val syncManager = JobSyncManager(repository)

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    // State to inform the UI about the network operation status
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        // Load initial data from the local repository cache.
        _jobs.value = repository.getJobs()

        // Observe changes to the state flow and save them back to the local repository.
        _jobs.onEach { jobList ->
            repository.saveJobs(jobList)
        }.launchIn(viewModelScope)

        // **Optional:** Start an initial sync on launch
        syncJobs()
    }

    // --- Local Data Manipulation (Stays the same) ---

    fun addJob(job: Job) {
        // When adding locally, mark it as pending sync
        val localJobWithSyncStatus = job.copy(
            // NOTE: Using 0 as a temporary local ID. A UUID is better.
            id = 0,
            isPendingSync = true,
            lastModifiedTimestamp = System.currentTimeMillis()
        )

        _jobs.update { currentList ->
            currentList + localJobWithSyncStatus
        }

        // Trigger a sync immediately after adding a new job
        syncJobs()
    }

    fun removeJob(job: Job) {
        _jobs.update { currentList ->
            currentList.filterNot { it.id == job.id }
        }
    }

    fun setJobs(jobList: List<Job>) {
        _jobs.value = jobList
    }

    fun clearJobs() {
        _jobs.value = emptyList()
    }

    // --- Network Synchronization ---

    /**
     * Initiates a full two-way synchronization using the Sync Manager.
     */
    fun syncJobs() {
        if (_syncStatus.value == SyncStatus.LOADING) return

        _syncStatus.value = SyncStatus.LOADING

        viewModelScope.launch {
            try {
                // Call the Sync Manager to handle the PUSH, PULL, and MERGE logic
                val finalJobs = syncManager.sync()

                // Update the ViewModel's state with the merged result
                _jobs.value = finalJobs

                _syncStatus.value = SyncStatus.SUCCESS

            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.ERROR(e.message ?: "Sync failed.")
            }
            // We omit 'finally' to let SUCCESS/ERROR state persist for the UI to read.
        }
    }
}
// SyncStatus sealed class definition remains the same

/**
 * Sealed class representing the synchronization status for UI feedback.
 */
sealed class SyncStatus {
    object IDLE : SyncStatus()
    object LOADING : SyncStatus()
    object SUCCESS : SyncStatus()
    data class ERROR(val message: String) : SyncStatus()
}