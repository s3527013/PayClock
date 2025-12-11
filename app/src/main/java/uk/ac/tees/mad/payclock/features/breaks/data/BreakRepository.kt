package uk.ac.tees.mad.payclock.features.breaks.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.payclock.database.BreakDao
import java.util.Date

class BreakRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val breakDao: BreakDao,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val breaks: Flow<List<Breaks>> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.flatMapLatest { user ->
        if (user != null) {
            syncFirestoreData(user.uid)
            breakDao.getAllBreaks(user.uid)
        } else {
            flowOf(emptyList())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeBreak: Flow<Breaks?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.flatMapLatest { user ->
        if (user != null) {
            breakDao.getActiveBreak(user.uid)
        } else {
            flowOf(null)
        }
    }

    suspend fun startBreak(timeLogId: String) {
        val userId = auth.currentUser?.uid ?: return
        val newBreak = Breaks(
            id = firestore.collection("breaks").document().id,
            userId = userId,
            timeLogId = timeLogId,
            startTime = Date(),
            lastUpdated = Date()
        )
        breakDao.insert(newBreak)
        firestore.collection("breaks").document(newBreak.id).set(newBreak).await()
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
                    endTime = now,
                    duration = duration,
                    lastUpdated = Date()
                )
                breakDao.insert(updatedBreak)
                firestore.collection("breaks").document(breakEntry.id).set(updatedBreak).await()
            }
        }
    }

    private fun syncFirestoreData(userId: String) {
        firestore.collection("breaks")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    externalScope.launch {
                        val breaks = it.toObjects<Breaks>()
                        breakDao.insertAll(breaks)
                    }
                }
            }
    }
}