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

/**
 * A repository for breaks.
 *
 * @param auth The Firebase authentication instance.
 * @param firestore The Firebase Firestore instance.
 * @param breakDao The DAO for breaks.
 * @param externalScope The external coroutine scope.
 */
class BreakRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val breakDao: BreakDao,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    /**
     * A Flow that emits the list of all breaks for the current user.
     */
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

    /**
     * A Flow that emits the currently active break for the current user.
     */
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

    /**
     * Starts a new break.
     *
     * @param timeLogId The ID of the time log to associate the break with.
     */
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

    /**
     * Ends the current break.
     */
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

    /**
     * Syncs the user's breaks from Firestore to the local database.
     * This function sets up a snapshot listener on the Firestore 'breaks' collection
     * for the given user ID. Whenever the data changes in Firestore, it fetches
     * the updated list of breaks and inserts them into the local Room database.
     *
     * @param userId The ID of the user whose breaks are to be synced.
     */
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