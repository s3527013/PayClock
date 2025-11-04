package uk.ac.tees.mad.payclock.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uk.ac.tees.mad.payclock.data.TimeLog

/**
 * ViewModel for managing a list of time logs.
 *
 * This ViewModel holds the state for a list of [TimeLog] objects and exposes
 * it to the UI in a reactive way using StateFlow, suitable for Jetpack Compose.
 */
class TimeLogViewModel : ViewModel() {

    // Private mutable state flow to hold the list of time logs.
    // Only the ViewModel can modify this list.
    private val _timeLogs = MutableStateFlow<List<TimeLog>>(emptyList())

    // Public immutable state flow that the UI can observe for changes.
    val timeLogs: StateFlow<List<TimeLog>> = _timeLogs.asStateFlow()

    /**
     * Adds a new time log to the list.
     *
     * @param timeLog The new [TimeLog] to add.
     */
    fun addTimeLog(timeLog: TimeLog) {
        // Use the 'update' function for thread-safe state modification.
        _timeLogs.update { currentList ->
            currentList + timeLog
        }
    }

    /**
     * Sets the entire list of time logs, replacing the current list.
     * This is useful for initializing the data from a repository or database.
     *
     * @param timeLogs The new list of [TimeLog] objects.
     */
    fun setTimeLogs(timeLogs: List<TimeLog>) {
        _timeLogs.value = timeLogs
    }

    /**
     * Clears all time logs from the list.
     */
    fun clearTimeLogs() {
        _timeLogs.value = emptyList()
    }
}
