package uk.ac.tees.mad.payclock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.models.Job
import uk.ac.tees.mad.payclock.data.repository.JobRepository

class JobViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val jobRepository = JobRepository()

    private val _authState = MutableStateFlow(auth.currentUser)
    val authState: StateFlow<FirebaseUser?> = _authState

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val jobs: StateFlow<List<Job>> = authState.flatMapLatest { user ->
        if (user != null) {
            jobRepository.getJobs(user.uid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addJob(name: String, hourlyRate: Double, breakTimeInMinutes: Int) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val newJob = Job(
                userId = userId,
                name = name,
                hourlyRate = hourlyRate,
                breakTimeInMinutes = breakTimeInMinutes
            )
            jobRepository.addJob(newJob)
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
}
