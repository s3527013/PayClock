package uk.ac.tees.mad.payclock.features.breaks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.features.breaks.data.BreakRepository
import uk.ac.tees.mad.payclock.features.breaks.data.Breaks

/**
 * A ViewModel for breaks.
 *
 * @param breakRepository The repository for breaks.
 */
class BreakViewModel(
    private val breakRepository: BreakRepository
) : ViewModel() {
    /**
     * A StateFlow that emits the currently active break.
     */
    val activeBreak: StateFlow<Breaks?> = breakRepository.activeBreak.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Starts a new break.
     *
     * @param timeLogId The ID of the time log to associate the break with.
     */
    fun startNewBreak(timeLogId: String) {
        viewModelScope.launch {
            breakRepository.startBreak(timeLogId)
        }
    }

    /**
     * Ends the current break.
     */
    fun endCurrentBreak() {
        viewModelScope.launch {
            breakRepository.endBreak()
        }
    }
}