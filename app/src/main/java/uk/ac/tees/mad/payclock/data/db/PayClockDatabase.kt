package uk.ac.tees.mad.payclock.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import uk.ac.tees.mad.payclock.data.models.Job
import uk.ac.tees.mad.payclock.data.models.TimeLog

/**
 * The Room database for the application.
 */
@Database(entities = [Job::class, TimeLog::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PayClockDatabase : RoomDatabase() {

    abstract fun jobDao(): JobDao
    abstract fun timeLogDao(): TimeLogDao

    companion object {
        @Volatile
        private var INSTANCE: PayClockDatabase? = null

        fun getDatabase(context: Context): PayClockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PayClockDatabase::class.java,
                    "payclock_database"
                )
                // Wipes and rebuilds instead of migrating if no Migration object.
                // Migration is not part of this example.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
