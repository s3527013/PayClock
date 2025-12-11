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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePayClockDatabase(@ApplicationContext context: Context): PayClockDatabase {
        return PayClockDatabase.getDatabase(context)
    }

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

    // If you need to provide other DAOs
    @Provides
    fun provideTimeLogDao(database: PayClockDatabase) = database.timeLogDao()

    @Provides
    fun provideJobDao(database: PayClockDatabase) = database.jobDao()

    @Provides
    fun provideBreakDao(database: PayClockDatabase) = database.breakDao()

    @Provides
    fun provideUserPreferencesDao(database: PayClockDatabase) = database.userPreferencesDao()
}