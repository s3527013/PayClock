package uk.ac.tees.mad.payclock.features.breaks.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Date

class BreakRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    /**
     * A reactive Flow that listens for authentication changes.
     * When a user logs in, it subscribes to their breaks in Firestore.
     * When they log out, it emits an empty list.
     */
    private val user = auth.currentUser?.uid

    @OptIn(ExperimentalCoroutinesApi::class)
    val breaks: Flow<List<Breaks>> = if (user != null) {
        // User is logged in, create a query for their breaks
        firestore.collection("breaks").whereEqualTo("userId", user).snapshots()
            .map { snapshot -> snapshot.toObjects<Breaks>() }
    } else {
        // User is logged out, emit an empty list
        flowOf(emptyList())
    }

    val activeBreak: Flow<Breaks?> = breaks.map { logs ->
        logs.find { it.endTime == null }
    }

    suspend fun startBreak(timeLogId: String) {
        val userId = auth.currentUser?.uid ?: return
        val newBreak = Breaks(
            userId = userId,
            timeLogId = timeLogId,
            startTime = Date(),
        )
        firestore.collection("breaks").add(newBreak)
    }

    suspend fun endBreak() {
        activeBreak.firstOrNull()?.let { breakEntry ->
            if (breakEntry.id.isNotBlank()) {
                val now = Date()
                val duration = if (breakEntry.startTime != null) {
                    (now.time - breakEntry.startTime.time) / 60000 // Duration in minutes
                } else {
                    0
                }
                val updatedBreak = breakEntry.copy(
                    endTime = now, duration = duration
                )
                firestore.collection("breaks").document(breakEntry.id).set(updatedBreak)
            }
        }
    }
}