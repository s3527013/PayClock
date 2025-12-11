package uk.ac.tees.mad.payclock.core

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.payclock.database.PayClockDatabase
import uk.ac.tees.mad.payclock.features.settings.data.SettingsRepository
import uk.ac.tees.mad.payclock.features.auth.AuthRepository
import uk.ac.tees.mad.payclock.features.breaks.BreakViewModel
import uk.ac.tees.mad.payclock.features.breaks.data.BreakRepository
import uk.ac.tees.mad.payclock.features.jobs.JobViewModel
import uk.ac.tees.mad.payclock.features.jobs.data.JobRepository
import uk.ac.tees.mad.payclock.features.report.ReportViewModel
import uk.ac.tees.mad.payclock.features.settings.SettingsViewModel
import uk.ac.tees.mad.payclock.features.timelog.TimeLogViewModel
import uk.ac.tees.mad.payclock.features.timelog.data.repository.TimeLogRepository

object Graph {

    lateinit var appContext: Context

    private val database by lazy {
        PayClockDatabase.getDatabase(appContext)
    }

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }


    private val firebaseFirestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    val breakRepository: BreakRepository by lazy {
        BreakRepository(firebaseAuth, firebaseFirestore, database.breakDao())
    }

    val breakViewModel: BreakViewModel by lazy {
        BreakViewModel(breakRepository)
    }

    val timeLogRepository: TimeLogRepository by lazy {
        TimeLogRepository(
            firebaseAuth,
            firebaseFirestore,
            breakRepository,
            database.timeLogDao(),
            database.jobDao()
        )
    }

    val jobRepository: JobRepository by lazy {
        JobRepository(firebaseAuth, firebaseFirestore, timeLogRepository, database.jobDao())
    }

    val jobViewModel: JobViewModel by lazy {
        JobViewModel(jobRepository)
    }

    val timeLogViewModel: TimeLogViewModel by lazy {
        TimeLogViewModel(timeLogRepository)
    }

    val reportViewModel: ReportViewModel by lazy {
        ReportViewModel(jobRepository, timeLogRepository)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(
            firebaseFirestore,
            database = database
        )
    }

    val settingsViewModel: SettingsViewModel by lazy {
        SettingsViewModel(settingsRepository, firebaseAuth)
    }

    fun provide(context: Context) {
        appContext = context
    }
}