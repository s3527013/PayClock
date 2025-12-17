package uk.ac.tees.mad.payclock.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import uk.ac.tees.mad.payclock.features.breaks.data.Breaks
import uk.ac.tees.mad.payclock.features.jobs.data.Job
import uk.ac.tees.mad.payclock.features.settings.data.UserPreferences
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLog

/**
 * The Room database for the application.
 */
@Database(
    entities = [TimeLog::class, Job::class, Breaks::class, UserPreferences::class],
    version = 3,  // Incremented version from 2 to 3
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PayClockDatabase : RoomDatabase() {

    /**
     * Returns the DAO for time logs.
     *
     * @return The DAO for time logs.
     */
    abstract fun timeLogDao(): TimeLogDao
    /**
     * Returns the DAO for jobs.
     *
     * @return The DAO for jobs.
     */
    abstract fun jobDao(): JobDao
    /**
     * Returns the DAO for breaks.
     *
     * @return The DAO for breaks.
     */
    abstract fun breakDao(): BreakDao
    /**
     * Returns the DAO for user preferences.
     *
     * @return The DAO for user preferences.
     */
    abstract fun userPreferencesDao(): UserPreferencesDao  // Add this

    companion object {
        @Volatile
        private var INSTANCE: PayClockDatabase? = null

        /**
         * Returns the singleton instance of the database.
         *
         * @param context The context.
         * @return The singleton instance of the database.
         */
        fun getDatabase(context: Context): PayClockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PayClockDatabase::class.java,
                    "pay_clock_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}