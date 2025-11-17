package uk.ac.tees.mad.payclock.core

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.payclock.features.jobs.data.JobRepository
import uk.ac.tees.mad.payclock.features.timelog.data.repository.TimeLogRepository

object Graph {
    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firebaseFirestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val timeLogRepository: TimeLogRepository by lazy {
        TimeLogRepository(firebaseAuth, firebaseFirestore)
    }

    val jobRepository: JobRepository by lazy {
        JobRepository(firebaseAuth, firebaseFirestore)
    }
}