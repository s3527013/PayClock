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

/**
 * A singleton object that provides dependencies to the application.
 */
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

    /**
     * The authentication repository.
     */
    val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    /**
     * The break repository.
     */
    val breakRepository: BreakRepository by lazy {
        BreakRepository(firebaseAuth, firebaseFirestore, database.breakDao())
    }

    /**
     * The break view model.
     */
    val breakViewModel: BreakViewModel by lazy {
        BreakViewModel(breakRepository)
    }

    /**
     * The time log repository.
     */
    val timeLogRepository: TimeLogRepository by lazy {
        TimeLogRepository(
            firebaseAuth,
            firebaseFirestore,
            breakRepository,
            database.timeLogDao(),
            database.jobDao()
        )
    }

    /**
     * The job repository.
     */
    val jobRepository: JobRepository by lazy {
        JobRepository(firebaseAuth, firebaseFirestore, timeLogRepository, database.jobDao())
    }

    /**
     * The job view model.
     */
    val jobViewModel: JobViewModel by lazy {
        JobViewModel(jobRepository)
    }

    /**
     * The time log view model.
     */
    val timeLogViewModel: TimeLogViewModel by lazy {
        TimeLogViewModel(timeLogRepository)
    }

    /**
     * The report view model.
     */
    val reportViewModel: ReportViewModel by lazy {
        ReportViewModel(jobRepository, timeLogRepository)
    }

    /**
     * The settings repository.
     */
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(
            firebaseFirestore,
            database = database
        )
    }

    /**
     * The settings view model.
     */
    val settingsViewModel: SettingsViewModel by lazy {
        SettingsViewModel(settingsRepository, firebaseAuth)
    }

    /**
     * Provides the application context to the graph.
     *
     * @param context The application context.
     */
    fun provide(context: Context) {
        appContext = context
    }
}