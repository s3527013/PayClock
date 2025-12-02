package uk.ac.tees.mad.payclock.features.breaks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.features.breaks.data.BreakRepository
import uk.ac.tees.mad.payclock.features.breaks.data.Breaks

class BreakViewModel(
    private val breakRepository: BreakRepository
) : ViewModel() {
    val activeBreak: StateFlow<Breaks?> = breakRepository.activeBreak.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun startNewBreak(timeLogId: String) {
        viewModelScope.launch {
            breakRepository.startBreak(timeLogId)
        }
    }

    fun endCurrentBreak() {
        viewModelScope.launch {
            breakRepository.endBreak()
        }
    }
}