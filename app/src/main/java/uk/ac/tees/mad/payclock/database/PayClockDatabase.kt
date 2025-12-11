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

@Database(
    entities = [TimeLog::class, Job::class, Breaks::class, UserPreferences::class],
    version = 3,  // Incremented version from 2 to 3
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PayClockDatabase : RoomDatabase() {

    abstract fun timeLogDao(): TimeLogDao
    abstract fun jobDao(): JobDao
    abstract fun breakDao(): BreakDao
    abstract fun userPreferencesDao(): UserPreferencesDao  // Add this

    companion object {
        @Volatile
        private var INSTANCE: PayClockDatabase? = null

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