package uk.ac.tees.mad.payclock.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uk.ac.tees.mad.payclock.database.PayClockDatabase
import uk.ac.tees.mad.payclock.features.settings.data.SettingsRepository
import javax.inject.Singleton

/**
 * A Dagger Hilt module that provides database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the [PayClockDatabase] instance.
     *
     * @param context The application context.
     * @return The [PayClockDatabase] instance.
     */
    @Provides
    @Singleton
    fun providePayClockDatabase(@ApplicationContext context: Context): PayClockDatabase {
        return PayClockDatabase.getDatabase(context)
    }

    /**
     * Provides the [SettingsRepository] instance.
     *
     * @param database The [PayClockDatabase] instance.
     * @return The [SettingsRepository] instance.
     */
    @Provides
    @Singleton
    fun provideSettingsRepository(
        database: PayClockDatabase
    ): SettingsRepository {
        return SettingsRepository(
            firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance(),
            database = database
        )
    }

    /**
     * Provides the [TimeLogDao] instance.
     *
     * @param database The [PayClockDatabase] instance.
     * @return The [TimeLogDao] instance.
     */
    @Provides
    fun provideTimeLogDao(database: PayClockDatabase) = database.timeLogDao()

    /**
     * Provides the [JobDao] instance.
     *
     * @param database The [PayClockDatabase] instance.
     * @return The [JobDao] instance.
     */
    @Provides
    fun provideJobDao(database: PayClockDatabase) = database.jobDao()

    /**
     * Provides the [BreakDao] instance.
     *
     * @param database The [PayClockDatabase] instance.
     * @return The [BreakDao] instance.
     */
    @Provides
    fun provideBreakDao(database: PayClockDatabase) = database.breakDao()

    /**
     * Provides the [UserPreferencesDao] instance.
     *
     * @param database The [PayClockDatabase] instance.
     * @return The [UserPreferencesDao] instance.
     */
    @Provides
    fun provideUserPreferencesDao(database: PayClockDatabase) = database.userPreferencesDao()
}