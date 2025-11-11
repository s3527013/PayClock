package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.data.db.PayClockDatabase
import uk.ac.tees.mad.payclock.data.repository.SyncRepository

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var syncRepository: SyncRepository? = null

    init {
        Firebase.auth.currentUser?.uid?.let {
            initializeForUser(it)
        }
    }

    /**
     * Initializes the repository and performs the initial data sync.
     */
    private fun initializeForUser(userId: String) {
        val db = PayClockDatabase.getDatabase(getApplication())
        syncRepository = SyncRepository(db.jobDao(), db.timeLogDao(), userId)

        viewModelScope.launch {
            // Download data from Firestore on startup
            syncRepository?.syncDown()
        }
    }

    /**
     * Uploads local changes to Firestore.
     */
    fun syncUp() {
        viewModelScope.launch {
            syncRepository?.syncUp()
        }
    }

    fun logout() {
        Firebase.auth.signOut()
    }
}
